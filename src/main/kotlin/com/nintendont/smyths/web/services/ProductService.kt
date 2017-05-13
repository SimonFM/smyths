package com.nintendont.smyths.web.services

import com.nintendont.smyths.data.schema.*
import com.nintendont.smyths.data.schema.responses.CheckProductResponse
import com.nintendont.smyths.utils.Constants.SMYTHS_STOCK_CHECKER_URL
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*
import com.nintendont.smyths.data.repository.*
import com.nintendont.smyths.data.schema.responses.SearchQueryResponse
import com.nintendont.smyths.utils.Constants
import com.nintendont.smyths.utils.Constants.SMYTHS_SEARCH_URL
import com.nintendont.smyths.utils.Utils
import com.nintendont.smyths.utils.Utils.makeEmptyProduct
import com.nintendont.smyths.utils.Utils.objectToString
import com.nintendont.smyths.utils.exceptions.HttpServiceException
import org.joda.time.DateTime
import org.json.JSONObject
import org.jsoup.Jsoup

// TODO: Multi threaded syncing.

@Service open class ProductService {

    @Autowired lateinit var httpService : HttpService
    @Autowired lateinit var productRepository : SmythsProductRepository
    @Autowired lateinit var brandRepository : SmythsBrandRepository
    @Autowired lateinit var listTypeRepository : SmythsListTypeRepository
    @Autowired lateinit var categoryRepository : SmythsCategoryRepository
    @Autowired lateinit var linkRepository : SmythsLinkRepository
    @Autowired lateinit var historicalProductRepository : SmythsHistoricalProductRepository

    /* When a product url has a product, but has failed for some reason
    * we need to store that url and try again later.
    * Possible retries include:
    *        - Read Time Out
    *        - Invalid JSON (usually " for a toy height)
    */
    private val failedProductsUrls : MutableList<String> = mutableListOf()

    /**
     * Method to query the database for products similar to the query
     * @param query - The query we're going to ask the DB for
     * @return A SearchQueryResponse
     */
    fun searchFromSmythsWebsite(query : String, locationId : String?) : SearchQueryResponse {
        println("------ Searching for products with name like $query -------")
        val searchUrl : MutableList<String> = mutableListOf<String>()
        val products = fetchProductsFromUrl("$SMYTHS_SEARCH_URL$query", true)
        val productsAsListOfString : MutableList<String> = mutableListOf()
        products.forEach { productsAsListOfString.add(objectToString(it)) }
       // val status : MutableList<CheckProductResponse> = checkAllProductAvailability(products, locationId)
        val searchQueryResponse : SearchQueryResponse = SearchQueryResponse(message = "Successfully retrieved Products",
                                                                            error = "None",
                                                                            status = mutableListOf(),
                                                                            products = products.toMutableList())
        println("------ Finished searching for products with name like $query -------")
        return searchQueryResponse
    }

    /**
     * Method to query the database for products similar to the query
     * @param query - The query we're going to ask the DB for
     * @return A SearchQueryResponse
     */
    fun searchForProductsInRepo(query : String, locationId : String?) : SearchQueryResponse {
        println("------ Searching for products with name like $query -------")
        val products = this.productRepository.searchForProducts(query)
        val productsAsListOfString : MutableList<String> = mutableListOf()
        products.forEach { productsAsListOfString.add(objectToString(it)) }
        val status : MutableList<CheckProductResponse> = checkAllProductAvailability(products, locationId)
        val searchQueryResponse : SearchQueryResponse = SearchQueryResponse(message = "Successfully retrieved Products",
                                                                            error = "None", status = status,
                                                                            products = products.toMutableList())
        println("------ Finished searching for products with name like $query -------")
        return searchQueryResponse
    }

    fun checkAllProductAvailability(products : MutableSet<Product>, locationId: String?) : MutableList<CheckProductResponse>{
        val status : MutableList<CheckProductResponse> = mutableListOf<CheckProductResponse>()

        if(!locationId.isNullOrEmpty()){
            products.forEach {
                val productStatusInLocation = checkProductAvailability(it.smythsStockCheckCode.toString(), locationId.toString())
                status.add(productStatusInLocation)
            }
        }
        return status
    }

    /***
     * Method to ask Smyths.ie if a product is in stock or not.
     * @param productId - The id of the product we want to ask about
     * @param storeId - The storeId we want ask in.
     * @return Response from Smyths in a nice to read json format
     */
    fun checkProductAvailability(productId: String, storeId: String) : CheckProductResponse {
        println("_____ Checking product availability for productId: $productId and storeId: $storeId ____")

        val product : Pair<String, Any> = Pair("productId", productId)
        val store : Pair<String, Any> = Pair("storeId", storeId)
        val params : MutableList<Pair<String,Any>> = mutableListOf()
        params.add(product)
        params.add(store)
        val response: Document = this.httpService.post(SMYTHS_STOCK_CHECKER_URL, params)
        val inStoreStatusElement : Elements = response.select("div#inStoreStatus")
        val inStoreStatus : String = inStoreStatusElement.text()

        val canClickCollectElement : Elements = response.select("div#canClickCollect")
        var canClickCollect : Boolean = false
        if(canClickCollectElement.text().isNotBlank()){
            canClickCollect = canClickCollectElement.text().toLowerCase().toBoolean()
        }

        val estimatedTimeElement : Elements = response.select("span.pre-order")
        val inStock : Elements = response.select("span.in-stock")
        val message : String = if (estimatedTimeElement.size > 0) estimatedTimeElement.text() else inStock.text()
        val checkProductResponse : CheckProductResponse = CheckProductResponse(message = message,
                                                                               inStoreStatus = inStoreStatus,
                                                                               locationId = storeId,
                                                                               productId = productId,
                                                                               canCollect = canClickCollect)

        println("___ Finished product availability for productId: $productId and storeId: $storeId ___")
        return checkProductResponse
    }

    /***
     * Syncs all the products from the links table
     * @return a Set of Products.
     */
    fun syncAllProducts() : MutableSet<Product> {
        println("*-*-*- Started Syncing All Products -*-*-*")
        val links = this.linkRepository.findAll()
        productRepository.deleteAll()
        val products : MutableSet<Product> = mutableSetOf<Product>()

        for (link in links){
            val productsFromUrl : MutableSet<Product> = fetchProductsFromUrl(url = link.url, isSearch = false)
            products.addAll(productsFromUrl)
        }
        println("*-*-*- Finished Syncing All Products -*-*-*")
        println("*-*-*- Attempting Retry for failed Products -*-*-*")
        val retryProducts : MutableSet<Product> = fetchProducts(this.failedProductsUrls)
        products.addAll(retryProducts)
        failedProductsUrls.clear()
        println("*-*-*- Finished Retry for failed Products -*-*-*")
        return products
    }

    private fun fetchProducts(urls : MutableList<String>) : MutableSet<Product>{
        val products : MutableSet<Product> = mutableSetOf<Product>()
        for (url in urls){
            val productsFromUrl : MutableSet<Product> = fetchProductsFromUrl(url = url, isSearch = false)
            products.addAll(productsFromUrl)
        }
        return products
    }

    /**
     * Returns all the products from specified ranges of indexes
     * @param start - The beginning index.
     * @param end - The last index we want back.
     * @return Iterable Product Structure.
     * TODO: Make this a Set return.
     * TODO: Handle out of bounds indexes.
     */
    fun getAllProducts( start: Int, end : Int) : Iterable<Product> {
        println("*-*-*- Starting range ($start -> $end) Products -*-*-*")
        val products = this.productRepository.findAllInRange(start, end)

        println("*-*-*- Ending range ($start -> $end) Products -*-*-*")
        return products
    }

    /***
     * Fetches a Set of products for a given url.
     * @param url - The Url that our product is on. The corresponds to the base page the product was found on.
     * @return all the products on that url.
     */
    fun fetchProductsFromUrl(url: String, isSearch: Boolean) : MutableSet<Product> {
        var products : MutableSet<Product> = mutableSetOf<Product>()
        println("--- Fetching products for url: $url ---")
        try{
            val categoryPage: Document = this.httpService.get(url, mutableListOf())
            val parsedProducts : MutableSet<Product> = parseProductsFromHTML(categoryPage, url, isSearch)
            products = products.union(parsedProducts).toMutableSet()
        } catch (failedRequest: HttpServiceException){
            println(failedRequest)
            addToFailedProductsUrl(url)
        }
        println("--- Finished fetching products for url: $url ---")
        return products
    }

    /***
     * Loads more content from a category page.
     * @param url - Used for loading more content.
     * @return A set of all the products.
     */
    private fun loadMoreProducts(url : String, isSearch : Boolean) : MutableSet<Product>{
        val moreProducts : MutableSet<Product> = mutableSetOf()

        var hasMoreResults : Boolean = true
        var page : Int = 1
        while(hasMoreResults){
            val loadMoreUrl : String = "$url/load-more?q=%3AieBestsellerRating&page=$page"
            val moreProductsPage: JSONObject = this.httpService.getJson(loadMoreUrl, mutableListOf())
            if(moreProductsPage.has("hasMoreResults")) {
                hasMoreResults = moreProductsPage.getBoolean("hasMoreResults")
                if(hasMoreResults){
                    val divs = moreProductsPage.getString("htmlContent")
                    val productsHTMLToParse : Document = Jsoup.parse(divs)
                    val products : MutableSet<Product> = parseProductsFromHTML(productsHTMLToParse, "", isSearch)
                    moreProducts.addAll(products)
                    page++
                }
            } else {
                hasMoreResults = false
            }
        }
        return moreProducts
    }

    /***
     * Returns all the products from a given HTML Document
     * @param page - The document to parse
     * @param url - Used for loading more content.
     * @return A set of all the products.
     */
    private fun parseProductsFromHTML(page: Document, url : String, isSearch : Boolean) : MutableSet<Product>{
        var products : MutableSet<Product> = mutableSetOf()
        val itemPanels : Elements = page.select("div.item-panel")
        itemPanels.forEach { panel ->
            val productName : String = panel.attr("data-name")
            val productCode : String = panel.attr("data-code")
            val productPrice : BigDecimal =  Utils.stringToBigDecimal(panel.attr("data-price"))
            val productUrl : String = "${Constants.SMYTHS_BASE_URL}${panel.children().last().attr("href")}"
            val productCategoryId : String = makeCategory(panel.attr("data-category"))
            val categoryIdToUse : String? = if(productCategoryId.isNullOrBlank()) null else productCategoryId
            val productBrandId : String = makeBrand(panel.attr("data-brand"))
            val brandIdToUse : String? = if(productBrandId.isNullOrBlank()) null else productBrandId

            val productListingId : String = ""

            val existingProduct : Product = productRepository.find(name = productName)
            val newProduct : Product = makeNewProduct(brandId = brandIdToUse,
                                                      categoryId = categoryIdToUse,
                                                      productName = productName,
                                                      productPrice = productPrice,
                                                      smythsCode = productCode,
                                                      smythsStockCheckCode= "-1",
                                                      url = productUrl)
            val sameProductName = existingProduct.name == newProduct.name
            val sameProductPrice = existingProduct.price == newProduct.price
            var productToCommitToHistory = makeEmptyProduct()
            if(existingProduct.name.isNotBlank() && sameProductName && sameProductPrice){
                products.add(existingProduct)
                productToCommitToHistory = existingProduct
            } else if(existingProduct.name.isBlank()){
                productRepository.create(newProduct)
                products.add(newProduct)
                productToCommitToHistory = newProduct
            }
            if (productToCommitToHistory.id.isNotBlank()){
                addHistoricalRecord(productToCommitToHistory)
            }
            if(url.isNotBlank() && !isSearch){
                val moreProducts : MutableSet<Product> = loadMoreProducts(url = url, isSearch = isSearch)
                products = products.union(moreProducts).toMutableSet()
            }
        }
        return products
    }

    /**
     * Adds a product to the history.
     */
    private fun addHistoricalRecord(product : Product) {
        val historicalProduct = HistoricalProduct( id = makeUUID(),
                                                   name = product.name,
                                                   price = product.price,
                                                   productId = product.id,
                                                   smythsCode = product.smythsCode,
                                                   smythsStockCheckCode = product.smythsStockCheckCode,
                                                   categoryId = product.categoryId,
                                                   brandId = product.brandId,
                                                   url = product.url,
                                                   date = DateTime.now() )
        historicalProductRepository.create(historicalProduct)
    }

    /**
     * Adds the urls of the products it failed to sync so we can retry later.
     */
    private fun addToFailedProductsUrl(urlToRetry: String){
        if(urlToRetry.isNotBlank() && !this.failedProductsUrls.contains(urlToRetry)){
            this.failedProductsUrls.add(urlToRetry)
            println("Added $urlToRetry to failedProduct Set")
        }
    }

    /**
     * Get's the product status for a url.
     * @param url - The url we wish to parse.
     * @return The status of the product.
     */
    private fun getProductStockCheckId(url : String) : String {
        var stockCheckId : String = ""

        val productPageResponse: Document = this.httpService.get(url, mutableListOf())
        val inputs = productPageResponse.select("input#ProductId")
        for(input in inputs){
            if(stockCheckId.isBlank()){
                stockCheckId = if(input.attr("value").isNotBlank()) input.attr("value") else "-1"
            }
        }
        return if (stockCheckId.isNotBlank()) stockCheckId else "-1"
    }

    /**
     * Makes a new category with the given name or returns the existing one.
     * @param categoryName - The name of the category
     * @return New or Existing Category
     */
    private fun makeCategory(categoryName: String): String {
        var categoryId: String = ""
        if (categoryName.isNotBlank()) {
            categoryId = makeUUID()
            val existingCategory: Category = this.categoryRepository.find(categoryName)
            if (existingCategory.name.isNotBlank()) {
                categoryId = existingCategory.id
            } else {
                val category: Category = Category(name = categoryName, id = categoryId)
                this.categoryRepository.create(category)
                println("- Made new category: $category....")
            }
        }
        return categoryId
    }

    /**
     * Makes a new brand with the given name or returns the existing one.
     * @param productBrand - The name of the brand
     * @return New or Existing brand
     */
    private fun makeBrand(productBrand: String): String {
        var brandId: String = ""
        if (productBrand.isNotBlank()) {
            brandId = makeUUID()
            val existingBrand: Brand = this.brandRepository.find(productBrand)
            if (existingBrand.name.isNotBlank()) {
                brandId = existingBrand.id
            } else {
                val brand: Brand = Brand(name = productBrand, id = brandId)
                this.brandRepository.create(brand)
                println("- Made new brand: $brand....")
            }
        }
        return brandId
    }

    /**
     * Makes a new Product with the given parameters.
     * @param categoryId - The id of the category
     * @param brandId - The id of the brand it is linked with
     * @param productName - The name of the product
     * @param productPrice - The price of the product
     * @param smythsCode - The id smyths have in their db for the product.
     * @param smythsStockCheckCode - The id needed in order to make the stock check call.
     * @param url - The url the product is on.
     * @return New or Existing Category
     */
    private fun makeNewProduct(brandId: String?, categoryId: String?, productName: String, productPrice: BigDecimal,
                               smythsCode: String, smythsStockCheckCode: String, url : String): Product {
        val productId : String = makeUUID()
        try {
            return Product( id = productId,
                            name = productName,
                            price = productPrice,
                            smythsCode = smythsCode.toLong(),
                            smythsStockCheckCode = smythsStockCheckCode.toLong(),
                            categoryId = categoryId,
                            brandId = brandId,
                            url = url )
        } catch (e : Exception){
            return makeEmptyProduct()
        }
    }

    /**
     * Makes a unique identifier
     * @return Id as a string
     */
    private fun makeUUID() : String {
       return UUID.randomUUID().toString()
    }
}