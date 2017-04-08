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
    @RequestMapping("/sync/products")
    fun getProduct(): String {
        val products = catalogueService.getAllProducts()
        val json = Gson().toJson(products)
        return json.toString()
    }

    @RequestMapping("/sync/links")
    fun getLinks(): String {
        val links = catalogueService.generateLinks()
        val json = Gson().toJson(links)
        return json.toString()
    }
}
