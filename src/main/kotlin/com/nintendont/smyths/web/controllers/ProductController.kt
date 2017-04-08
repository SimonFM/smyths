package com.nintendont.smyths.web.controllers

import com.google.gson.Gson
import com.nintendont.smyths.data.schema.requests.CheckProductRequest
import com.nintendont.smyths.web.services.ProductService
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController


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
}
