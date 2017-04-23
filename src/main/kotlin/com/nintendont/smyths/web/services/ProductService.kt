package com.nintendont.smyths.web.services

import com.nintendont.smyths.data.schema.*
import com.nintendont.smyths.data.schema.responses.CheckProductResponse
import com.nintendont.smyths.utils.Constants
import com.nintendont.smyths.utils.Constants.SMYTHS_STOCK_CHECKER_URL
import com.nintendont.smyths.utils.Utils
import org.json.JSONObject
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*
import java.io.IOException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.nintendont.smyths.data.repository.*
import com.nintendont.smyths.data.schema.responses.SearchQueryResponse
import com.nintendont.smyths.utils.exceptions.HttpServiceException
import org.jsoup.nodes.Element
// TODO: Multi threaded syncing.

@Service open class ProductService {

    @Autowired lateinit var httpService : HttpService
    @Autowired lateinit var productRepository : SmythsProductRepository
    @Autowired lateinit var brandRepository : SmythsBrandRepository
    @Autowired lateinit var listTypeRepository : SmythsListTypeRepository
    @Autowired lateinit var categoryRepository : SmythsCategoryRepository
    @Autowired lateinit var linkRepository : SmythsLinkRepository

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
    fun searchForProducts(query : String) : SearchQueryResponse {
        println("------ Searching for products with name like $query -------")
        val products = this.productRepository.searchForProducts(query)
        val productsAsJson : String =  Gson().toJson(products).toString()
        val searchQueryResponse : SearchQueryResponse = SearchQueryResponse(message = "Successfully retrieved Products",
                                                                            error = "None",
                                                                            products = productsAsJson)
        println("------ Finished searching for products with name like $query -------")
        return searchQueryResponse
    }

    /***
     * Method to ask Smyths.ie if a product is in stock or not.
     * @param productId - The id of the product we want to ask about
     * @param storeId - The storeId we want ask in.
     * @return Response from Smyths in a nice to read json format
     */
    fun checkProductAvailability(productId: String, storeId: String) : JSONObject {
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
        val canClickCollect : Boolean = canClickCollectElement.text() == "True"

        val estimatedTimeElement : Elements = response.select("span.pre-order")
        val inStock : Elements = response.select("span.in-stock")
        val message : String = if (estimatedTimeElement.size > 0) estimatedTimeElement.text() else inStock.text()
        val checkProductResponse : CheckProductResponse = CheckProductResponse(message = message, inStoreStatus = inStoreStatus, canCollect = canClickCollect)

        println("___ Finished product availability for productId: $productId and storeId: $storeId ___")
        return JSONObject(checkProductResponse)
    }

    /***
     * Syncs all the products from the links table
     * @return a Set of Products.
     */
    fun syncAllProducts() : MutableSet<Product> {
        println("*-*-*- Started Syncing All Products -*-*-*")
        val links = this.linkRepository.findAll()
        var products : MutableSet<Product> = mutableSetOf<Product>()

        for (link in links){
            val productsFromUrl : MutableSet<Product> = fetchForUrl(url = link.url)
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
            val productsFromUrl : MutableSet<Product> = fetchForUrl(url = url)
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
    fun fetchForUrl(url: String) : MutableSet<Product> {
        val products : MutableSet<Product> = mutableSetOf()
        println("Fetching products for url: $url")
        try{
            val response: Document = this.httpService.get("$url?${Constants.VIEW_ALL}", mutableListOf())
            val containsProducts : Boolean = response.getElementsByAttribute(Constants.DATA_EGATYPE).size > 0
            if (containsProducts){
                val data = response.getElementsByAttribute(Constants.DATA_EGATYPE)[0]
                val pageOfProducts = data.children()
                for (elem in pageOfProducts) {
                    val productData = elem.attr(Constants.DATA_EVENT)
                    if(productData.isNotBlank()){
                        if(isValidJson(productData.toString())){
                            val json = JSONObject(productData)

                            val productUrl : String = getProductUrl(elem)
                            val smythsStockCheckerId : String = getProductStockCheckId(productUrl)

                            val listing : String = getListing(json)
                            val listingId: String = makeListing(listing)

                            val productBrand : String = getBrand(json)
                            val brandId: String = makeBrand(productBrand)
                            val brandIdToUse : String? = if(brandId.isNullOrBlank()) null else brandId
                            val categoryName : String = getCategory(json)
                            val categoryId: String = makeCategory(categoryName)

                            val smythsProductId : String = getSmythsId(json)
                            val existingProduct : Product = this.productRepository.find(smythsProductId)

                            val productName : String = getProductName(json)
                            val productPriceAsString : String = getProductPrice(json)
                            val productPriceAsBigDecimal : BigDecimal = Utils.stringToBigDecimal(productPriceAsString)

                            val newProduct: Product = makeNewProduct(brandIdToUse, categoryId,
                                    listingId, productName,
                                    productPriceAsBigDecimal, smythsProductId,
                                    smythsStockCheckerId, productUrl)
                            val productToAdd : Product = determineProduct(newProduct, existingProduct)
                            if(productToAdd.id.isNotBlank()){
                                products.add(productToAdd)
                            }
                        } else {
                            println("Not Valid JSON: $productData")
                            addToFailedProductsUrl(url)
                        }
                    }
                }
            }
        } catch (failedRequest: HttpServiceException){
            addToFailedProductsUrl(url)
        }
        println("--- Finished fetching products for url: $url ---")
        return products
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

    /***
     * Checks to see if a product is fresh or stale when we do a sync products call.
     * @param newProduct - The product we already have
     * @param existingProduct - The product we found online
     * @return new or existing product.
     */
    private fun determineProduct(newProduct : Product, existingProduct : Product) : Product {
        if(existingProduct.id.isNotBlank()){
            val updatePrice : Boolean =  existingProduct.price.compareTo(newProduct.price) != 0
            if(updatePrice){
                existingProduct.price = newProduct.price
            }
            val updateName : Boolean = compareProductStrings(existingProduct.name, newProduct.name)
            if(updateName){
                existingProduct.name = newProduct.name
            }
            val updateURL : Boolean = compareProductStrings(existingProduct.url, newProduct.url)
            if(updateURL){
                existingProduct.url = newProduct.url
            }
            val updateCategory : Boolean = compareProductStrings(existingProduct.categoryId, newProduct.categoryId)
            if(updateCategory){
                existingProduct.categoryId = newProduct.categoryId
            }
            val updateListing : Boolean = compareProductStrings(existingProduct.listTypeId, newProduct.listTypeId)
            if(updateListing){
                existingProduct.listTypeId = newProduct.listTypeId
            }
            val updateBrand : Boolean = compareProductStrings(existingProduct.brandId, newProduct.brandId)
            if(updateBrand){
                existingProduct.brandId = newProduct.brandId
            }
            val updateSmythsId : Boolean = existingProduct.smythsId.compareTo(newProduct.smythsId) != 0
            if(updateSmythsId){
                existingProduct.smythsId = newProduct.smythsId
            }
            val updateSmythsStockId : Boolean = existingProduct.smythsStockCheckId.compareTo(newProduct.smythsStockCheckId) != 0
            if(updateSmythsStockId){
                existingProduct.smythsStockCheckId = newProduct.smythsStockCheckId
            }
            val shouldUpdate : Boolean = (updateBrand || updateCategory || updateListing || updateName
                                                      || updatePrice || updateSmythsId || updateSmythsStockId
                                                      || updateURL)
            if(shouldUpdate){
                this.productRepository.update(existingProduct)
                println("- Updated Product: $existingProduct")
            }
            return existingProduct
        } else {
            if(newProduct.id.isNotBlank()){
                this.productRepository.create(newProduct)
                println("- Created Product: $newProduct")
            }
            return newProduct
        }
    }

    /**
     * Compares two strings. Needed as == was finicky...
     * @param existingProduct - data to check on the existing product
     * @param newProduct - data to check on the new product
     * @return True if the same, False if different
     */
    private fun compareProductStrings(existingProduct: String?, newProduct: String?) : Boolean {
        return !existingProduct.equals(newProduct)
    }

    /**
     * Makes a new category with the given name or returns the existing one.
     * @param categoryName - The name of the category
     * @return New or Existing Category
     */
    private fun makeCategory(categoryName: String): String {
        var categoryId: String = makeUUID()
        if (categoryName.isNotBlank()) {
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
     * Makes a new ListType with the given name or returns the existing one.
     * @param listing - The name of the ListType
     * @return New or Existing ListType
     */
    private fun makeListing(listing: String): String {
        var listingId: String = makeUUID()
        if (listing.isNotBlank()) {
            val existingType: ListType = this.listTypeRepository.find(listing)
            if (existingType.name.isNotBlank()) {
                listingId = existingType.id
            } else {
                val list: ListType = ListType(name = listing, id = listingId)
                this.listTypeRepository.create(list)
                println("- Made new listing: $list....")
            }
        }
        return listingId
    }

    /**
     * Makes a new Product with the given parameters.
     * @param categoryId - The id of the category
     * @param brandId - The id of the brand it is linked with
     * @param listingId - The id of the listing id it is linked with
     * @param productName - The name of the product
     * @param productPriceAsBigDecimal - The price of the product
     * @param smythsProductId - The id smyths have in their db for the product.
     * @param smythsStockCheckId - The id needed in order to make the stock check call.
     * @param url - The url the product is on.
     * @return New or Existing Category
     */
    private fun makeNewProduct(brandId: String?, categoryId: String?, listingId: String?, productName: String,
                               productPriceAsBigDecimal: BigDecimal, smythsProductId: String, smythsStockCheckId: String,
                               url : String): Product {
        val productId : String = makeUUID()
        try {
            return Product(id = productId,
                    name = productName,
                    price = productPriceAsBigDecimal,
                    smythsId = smythsProductId.toLong(),
                    smythsStockCheckId = smythsStockCheckId.toLong(),
                    categoryId = categoryId,
                    listTypeId = listingId,
                    brandId = brandId,
                    url = url)
        } catch (e : Exception){
            return Product("", 0, 0, "", BigDecimal.ZERO , "","", "", "")
        }
    }

    /**
     * Makes a new category with the given name or returns the existing one.
     * @param categoryName - The name of the category
     * @return New or Existing Category
     */
    private fun getProductUrl(elem : Element) : String {
        val url : String = elem.select("a.product-name").attr("href")
        return if (url.isNotBlank()) url else ""
    }


    //TODO: Make these in to a builder class.
    /**
     * Gets the product Price from the JSON
     * @param json - the json for the product.
     * @return Empty string or the price.
     */
    private fun getProductPrice(json: JSONObject) = if (json.has(Constants.PRICE)) json.get(Constants.PRICE) as String else "0"

    /**
     * Gets the product Name from the JSON
     * @param json - the json for the product.
     * @return Empty string or the Name.
     */
    private fun getProductName(json: JSONObject) = if (json.has(Constants.PRODUCT_NAME)) json.get(Constants.PRODUCT_NAME) as String else ""

    /**
     * Gets the product smythId from the JSON
     * @param json - the json for the product.
     * @return -1 as a String or the smythsId.
     */
    private fun getSmythsId(json: JSONObject) = if (json.has(Constants.ID)) json.get(Constants.ID) as String else "-1"

    /**
     * Gets the category name from the JSON
     * @param json - the json for the product.
     * @return Empty string or the category name.
     */
    private fun getCategory(json: JSONObject) = if (json.has(Constants.CATEGORY_NAME)) json.get(Constants.CATEGORY_NAME) as String else ""

    /**
     * Gets the product Brand from the JSON
     * @param json - the json for the product.
     * @return Empty string or the Brand.
     */
    private fun getBrand(json: JSONObject) = if (json.has(Constants.BRAND)) json.get(Constants.BRAND) as String else ""

    /**
     * Gets the product ListType name from the JSON
     * @param json - the json for the product.
     * @return Empty string or the ListType name.
     */
    private fun getListing(json: JSONObject) = if (json.has(Constants.LIST)) json.get(Constants.LIST) as String else ""

    /**
     * Makes a unique identifier
     * @return Id as a string
     */
    private fun makeUUID() : String {
       return UUID.randomUUID().toString()
    }

    /**
     * Checks whether or not json is valid
     * @param json - the json to test
     * @return True if valid, False if not
     */
    private fun isValidJson(json : String): Boolean {
        try {
            val mapper = ObjectMapper()
            mapper.readTree(json)
            return true
        } catch (e: IOException) {
            return false
        }
    }
}