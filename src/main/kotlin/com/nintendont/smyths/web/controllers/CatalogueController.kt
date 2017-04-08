package com.nintendont.smyths.web.controllers

import com.google.gson.Gson
import com.nintendont.smyths.web.services.ProductService
import com.nintendont.smyths.web.services.LinkService
import com.nintendont.smyths.web.services.LocationService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.Mapping

@RestController
@RequestMapping("/catalogue")
class CatalogueController {
    //
    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var linkService: LinkService

    @Autowired
    private lateinit var locationService: LocationService

    /**
     * @author Simon
     *
     * The /product endpoint to fetch all products
     */
    @RequestMapping("/sync/products")
    fun getProduct(): String {
        val products = productService.getAllProducts()
        val json = Gson().toJson(products)
        return json.toString()
    }

    /**
     * @author Simon
     *
     * The link endpoint to fetch all links
     */
    @RequestMapping("/sync/links")
    fun getLinks(): String {
        val links = linkService.generateLinks()
        val json = Gson().toJson(links)
        return json.toString()
    }

    /**
     * @author Simon
     *
     * The location endpoint to fetch all locations
     */
    @RequestMapping("/sync/locations")
    fun getLocations(): String {
        val links = locationService.generateLocations()
        val json = Gson().toJson(links)
        return json.toString()
    }
}
