package com.nintendont.smyths.web.controllers

import com.google.gson.Gson
import com.nintendont.smyths.data.schema.Product
import com.nintendont.smyths.data.schema.requests.FetchProductsRequest
import com.nintendont.smyths.data.schema.responses.GenerateLinksResponse
import com.nintendont.smyths.data.schema.responses.GetAllLocationsResponse
import com.nintendont.smyths.utils.Utils
import com.nintendont.smyths.utils.Utils.objectToString
import com.nintendont.smyths.web.services.ProductService
import com.nintendont.smyths.web.services.LinkService
import com.nintendont.smyths.web.services.LocationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController @RequestMapping("/catalogue")
class CatalogueController {
    //
    @Autowired private lateinit var productService: ProductService
    @Autowired private lateinit var linkService: LinkService
    @Autowired private lateinit var locationService: LocationService

    /**
     * @author Simon
     * The /product endpoint to sync all products
     */
    @RequestMapping("/sync/products", produces = arrayOf("application/json"))
    fun getProduct() : MutableSet<Product> {
        val products = this.productService.syncAllProducts()
        return products
    }

    @RequestMapping("/sync/product", method = arrayOf(RequestMethod.POST), produces = arrayOf("application/json"))
    fun fetchProductsFromUrl(@RequestBody fetchProductsRequest: FetchProductsRequest): String {
        var products : MutableSet<Product> = mutableSetOf()
        val url : String? = fetchProductsRequest.url
        if(!url.isNullOrBlank()){
            products = this.productService.fetchForUrl(url.toString())
        }
        return objectToString(products)
    }

    /**
     * @author Simon
     *
     * The link endpoint to fetch all links
     */
    @RequestMapping("/sync/links", produces = arrayOf("application/json"))
    fun getLinks(): GenerateLinksResponse {
        val links = this.linkService.generateLinks()
        return links
    }

    /**
     * @author Simon
     *
     * The location endpoint to fetch all locations
     */
    @RequestMapping("/sync/locations", produces = arrayOf("application/json"))
    fun getLocations(): GetAllLocationsResponse {
        return this.locationService.generateLocationsFromJson()
    }
}
