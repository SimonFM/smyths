package com.nintendont.smyths.hello

import com.google.gson.Gson
import com.nintendont.smyths.services.ProductService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.annotation.Autowired

@RestController
class HelloController {
    //
    @Autowired
    private lateinit var productService : ProductService
    /**
     * @author Simon
     *
     * Made by Spring boot
     */
    @RequestMapping("/")
    fun index(): String {

        return "Greetings from Spring Boot!"
    }

    /**
     * @author Simon
     *
     * The /product endpoint to retrieve a certain product
     */
    @RequestMapping("/product")
    fun getProduct(): String {
//        poductService.getAllProducts("22496", "22")
        val products = productService.getAllProducts()
        val json = Gson().toJson(products)

        return json.toString()
    }
}
