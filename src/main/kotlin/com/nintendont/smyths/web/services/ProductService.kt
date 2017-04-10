package com.nintendont.smyths.web.services

import com.nintendont.smyths.utils.http.HttpHandler
import com.nintendont.smyths.data.repository.*
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
import org.jsoup.nodes.Element

@Service
open class ProductService {

    private val httpHandler : HttpHandler = HttpHandler()
    @Autowired
    lateinit var productRepository : SmythsProductRepository
    @Autowired
    lateinit var brandRepository : SmythsBrandRepository
    @Autowired
    lateinit var listTypeRepository : SmythsListTypeRepository
    @Autowired
    lateinit var categoryRepository : SmythsCategoryRepository
    @Autowired
    lateinit var linkRepository : SmythsLinkRepository

    fun checkProductAvailability(productId:String, storeId: String) : JSONObject{
        println("Checking product availability for productId: $productId and storeId: $storeId....")

        val product : Pair<String, Any> = Pair("productId", productId)
        val store : Pair<String, Any> = Pair("storeId", storeId)
        val params : MutableList<Pair<String,Any>> = mutableListOf()
        params.add(product)
        params.add(store)
        val response: Document = httpHandler.post(SMYTHS_STOCK_CHECKER_URL, params)
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

    fun getAllProducts() : MutableSet<Product> {
        println("Generating Products....")
        val links = linkRepository.findAll()
        val products = mutableSetOf<Product>()

        for (link in links){
            val productsFromUrl = fetchForUrl(url = link.url)
            products.addAll(productsFromUrl)
        }
        println("*-*-*- Generated All Products -*-*-*")
        return products
    }

    fun fetchForUrl(url: String) : MutableSet<Product>{
        val products : MutableSet<Product> = mutableSetOf()
        println("Fetching products for url: $url")
        val response: Document = httpHandler.get("$url?${Constants.VIEW_ALL}", mutableListOf())
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

                        val categoryName : String = getCategory(json)
                        val categoryId: String = makeCategory(categoryName)

                        val smythsProductId : String = getSmythsId(json)
                        val existingProduct : Product = productRepository.find(smythsProductId)

                        val productName : String = getProductName(json)
                        val productPriceAsString : String = getProductPrice(json)
                        val productPriceAsBigDecimal : BigDecimal = Utils.stringToBigDecimal(productPriceAsString)

                        val newProduct: Product = makeNewProduct(brandId, categoryId, listingId, productName,
                                productPriceAsBigDecimal, smythsProductId,
                                smythsStockCheckerId, productUrl)
                        val productToAdd : Product = determineProduct(newProduct, existingProduct)
                        if(productToAdd.id.isNotBlank()){
                            products.add(productToAdd)
                        }
                    } else {
                        println("Not Valid JSON: $productData")
                    }
                }
            }
        }
        println("--- Finished fetching products for url: $url ---")
        return products
    }

    private fun getProductStockCheckId(url : String) : String{
        var stockCheckId : String = ""

        val productPageResponse: Document = httpHandler.get(url, mutableListOf())
        val inputs = productPageResponse.select("input#ProductId")
        for(input in inputs){
            if(stockCheckId.isBlank()){
                stockCheckId = if(input.attr("value").isNotBlank()) input.attr("value") else "-1"
            }
        }
        return if (stockCheckId.isNotBlank()) stockCheckId else "-1"
    }

    private fun determineProduct(newProduct : Product, existingProduct : Product) : Product {
        if(existingProduct.id.isNotBlank()){
            val updatePrice : Boolean = existingProduct.price != newProduct.price
            if(updatePrice){
                existingProduct.price = newProduct.price
            }
            val updateName : Boolean = existingProduct.name != newProduct.name
            if(updateName){
                existingProduct.name = newProduct.name
            }
            val updateURL : Boolean = existingProduct.url != newProduct.url
            if(updateURL){
                existingProduct.url = newProduct.url
            }
            val updateCategory : Boolean = existingProduct.categoryId != newProduct.categoryId
            if(updateCategory){
                existingProduct.categoryId = newProduct.categoryId
            }
            val updateListing : Boolean = existingProduct.listTypeId != newProduct.listTypeId
            if(updateListing){
                existingProduct.listTypeId = newProduct.listTypeId
            }
            val updateBrand : Boolean = existingProduct.brandId != newProduct.brandId
            if(updateBrand){
                existingProduct.brandId = newProduct.brandId
            }
            val updateSmythsId : Boolean = existingProduct.smythsId != newProduct.smythsId
            if(updateSmythsId){
                existingProduct.smythsId = newProduct.smythsId
            }
            val updateSmythsStockId : Boolean = existingProduct.smythsStockCheckId != newProduct.smythsStockCheckId
            if(updateSmythsStockId){
                existingProduct.smythsStockCheckId = newProduct.smythsStockCheckId
            }
            val shouldUpdate : Boolean = (updateBrand || updateCategory || updateListing || updateName
                                                      || updatePrice || updateSmythsId || updateSmythsStockId
                                                      || updateURL)
            if(shouldUpdate){
                productRepository.update(existingProduct)
                println("- Updated Product: $existingProduct")
            }
            return existingProduct
        } else {
            if(newProduct.id.isNotBlank()){
                productRepository.create(newProduct)
                println("- Created Product: $newProduct")
            }
            return newProduct
        }
    }

    private fun makeCategory(categoryName: String): String {
        var categoryId: String = makeUUID()
        if (categoryName.isNotBlank()) {
            val existingCategory: Category = categoryRepository.find(categoryName)
            if (existingCategory.name.isNotBlank()) {
                categoryId = existingCategory.id
            } else {
                val category: Category = Category(name = categoryName, id = categoryId)
                categoryRepository.create(category)
                println("- Made new category: $category....")
            }
        }
        return categoryId
    }

    private fun makeBrand(productBrand: String): String {
        var brandId: String = makeUUID()
        if (productBrand.isNotBlank()) {
            val existingBrand: Brand = brandRepository.find(productBrand)
            if (existingBrand.name.isNotBlank()) {
                brandId = existingBrand.id
            } else {
                val brand: Brand = Brand(name = productBrand, id = brandId)
                brandRepository.create(brand)
                println("- Made new brand: $brand....")
            }
        }
        return brandId
    }

    private fun makeListing(listing: String): String {
        var listingId: String = makeUUID()
        if (listing.isNotBlank()) {
            val existingType: ListType = listTypeRepository.find(listing)
            if (existingType.name.isNotBlank()) {
                listingId = existingType.id
            } else {
                val list: ListType = ListType(name = listing, id = listingId)
                listTypeRepository.create(list)
                println("- Made new listing: $list....")
            }
        }
        return listingId
    }

    private fun makeNewProduct(brandId: String, categoryId: String, listingId: String, productName: String,
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

    private fun getProductUrl(elem : Element) : String{
        val url : String = elem.select("a.product-name").attr("href")
        return if (url.isNotBlank()) url else ""
    }

    private fun getProductPrice(json: JSONObject) = if (json.has(Constants.PRICE)) json.get(Constants.PRICE) as String else "0"

    private fun getProductName(json: JSONObject) = if (json.has(Constants.PRODUCT_NAME)) json.get(Constants.PRODUCT_NAME) as String else ""

    private fun getSmythsId(json: JSONObject) = if (json.has(Constants.ID)) json.get(Constants.ID) as String else "-1"

    private fun getCategory(json: JSONObject) = if (json.has(Constants.CATEGORY_NAME)) json.get(Constants.CATEGORY_NAME) as String else ""

    private fun getBrand(json: JSONObject) = if (json.has(Constants.BRAND)) json.get(Constants.BRAND) as String else ""

    private fun getListing(json: JSONObject) = if (json.has(Constants.LIST)) json.get(Constants.LIST) as String else ""

    private fun makeUUID() : String{
       return UUID.randomUUID().toString()
    }

    private fun isValidJson(json : String): Boolean{
        try {
            val mapper = ObjectMapper()
            mapper.readTree(json)
            return true
        } catch (e: IOException) {
            return false
        }
    }
}