package com.nintendont.smyths.web.controllers

import com.google.gson.Gson
import com.nintendont.smyths.data.schema.Product
import com.nintendont.smyths.data.schema.requests.CheckProductRequest
import com.nintendont.smyths.data.schema.requests.GetProductsRequest
import com.nintendont.smyths.data.schema.requests.SearchProductsRequest
import com.nintendont.smyths.data.schema.responses.SearchQueryResponse
import com.nintendont.smyths.web.services.ProductService
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(origins = arrayOf("http://localhost:4200"))
@RequestMapping("/product")
class ProductController {
    //
    @Autowired private lateinit var productService: ProductService

    /**
     * @author Simon
     *
     * The /product endpoint to retrieve a certain product
     */
    @PostMapping("/available")
    fun checkProduct(@RequestBody checkProductRequest: CheckProductRequest): String {
        var products : JSONObject = JSONObject()
        val productId : String? = checkProductRequest.productId
        val storeId : String? = checkProductRequest.storeId
        if(!productId.isNullOrBlank() && !storeId.isNullOrBlank()){
            products = productService.checkProductAvailability(productId.toString(), storeId.toString())
        }
        val json = Gson().toJson(products)
        return json.toString()
    }

    @PostMapping("/all")
    fun getAllProducts(@RequestBody getProductsRequest: GetProductsRequest): String {
        var productsList : Iterable<Product> = listOf()
        val lowRange : Int? = getProductsRequest.lowRange
        val highRange : Int? = getProductsRequest.highRange
        val validLowRange : Boolean = lowRange != null && lowRange >= 0
        val validHighRange : Boolean =  highRange != null && highRange > 0

        if(validLowRange && validHighRange) {
            productsList = productService.getAllProducts(lowRange as Int,  highRange as Int)
        }
        return Gson().toJson(productsList)
    }

    @PostMapping("/search")
    fun searchProducts(@RequestBody searchProductsRequest: SearchProductsRequest): String {
        val searchQuery : String? = searchProductsRequest.search
        val validSearchQuery : Boolean = !searchQuery.isNullOrBlank()
        val searchQueryResponse : SearchQueryResponse = makeSearchResponse(searchQuery, validSearchQuery)
        val response : String = Gson().toJson(searchQueryResponse)
        return response
    }

    private fun makeSearchResponse(searchQuery : String?, validSearchQuery : Boolean) : SearchQueryResponse{
        if(validSearchQuery) {
            return productService.searchForProducts(searchQuery.toString())
        }
        return SearchQueryResponse(message = "Invalid parameters for 'search'",
                                   error = "Null Query",
                                   products = JSONObject(listOf<Product>()).toString())
    }
}
