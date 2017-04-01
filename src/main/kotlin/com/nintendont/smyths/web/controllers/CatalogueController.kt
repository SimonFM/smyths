package com.nintendont.smyths.web.controllers

import com.google.gson.Gson
import com.nintendont.smyths.web.services.CatalogueService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.Mapping

@RestController
@RequestMapping("/catalogue")
class CatalogueController {
    //
    @Autowired
    private lateinit var catalogueService: CatalogueService

    /**
     * @author Simon
     *
     * The /product endpoint to retrieve a certain product
     */
    @RequestMapping("/sync")
    fun getProduct(): String {
//        poductService.getAllProducts("22496", "22")
        val products = catalogueService.getAllProducts()
        val json = Gson().toJson(products)

        return json.toString()
    }
}
