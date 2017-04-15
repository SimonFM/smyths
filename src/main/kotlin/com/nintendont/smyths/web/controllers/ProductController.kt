package com.nintendont.smyths.web.controllers

import com.google.gson.Gson
import com.nintendont.smyths.data.schema.Product
import com.nintendont.smyths.data.schema.requests.CheckProductRequest
import com.nintendont.smyths.data.schema.requests.GetProductsRequest
import com.nintendont.smyths.web.services.ProductService
import org.apache.catalina.servlet4preview.http.HttpServletRequest
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/product")
class ProductController {
    //
    @Autowired
    private lateinit var productService: ProductService

    /**
     * @author Simon
     *
     * The /product endpoint to retrieve a certain product
     */
    @CrossOrigin(origins = arrayOf("http://localhost:4200"))
    @RequestMapping("/check", method = arrayOf(RequestMethod.POST))
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

    @CrossOrigin(origins = arrayOf("http://localhost:4200"))
    @RequestMapping(value = "/getProducts", method = arrayOf(RequestMethod.POST))
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
}
