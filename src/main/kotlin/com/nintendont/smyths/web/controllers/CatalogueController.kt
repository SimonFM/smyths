package com.nintendont.smyths.web.controllers

import com.google.gson.Gson
import com.nintendont.smyths.data.schema.Product
import com.nintendont.smyths.data.schema.requests.FetchProductsRequest
import com.nintendont.smyths.web.services.ProductService
import com.nintendont.smyths.web.services.LinkService
import com.nintendont.smyths.web.services.LocationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

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

    @RequestMapping("/sync/product", method = arrayOf(RequestMethod.POST))
    fun fetchProductsFromUrl(@RequestBody fetchProductsRequest: FetchProductsRequest): String {
        var products : MutableSet<Product> = mutableSetOf()
        val url : String? = fetchProductsRequest.url
        if(!url.isNullOrBlank()){
            products = productService.fetchForUrl(url.toString())
        }
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
