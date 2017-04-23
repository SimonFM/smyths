package com.nintendont.smyths.web.controllers

import com.nintendont.smyths.data.schema.Location
import com.nintendont.smyths.data.schema.Product
import com.nintendont.smyths.data.schema.requests.CheckProductAllLocationsRequest
import com.nintendont.smyths.data.schema.requests.CheckProductRequest
import com.nintendont.smyths.data.schema.requests.GetProductsRequest
import com.nintendont.smyths.data.schema.requests.SearchProductsRequest
import com.nintendont.smyths.data.schema.responses.SearchQueryResponse
import com.nintendont.smyths.utils.Utils.objectToString
import com.nintendont.smyths.web.services.LocationService
import com.nintendont.smyths.web.services.ProductService
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController @RequestMapping("/product")
@CrossOrigin(origins = arrayOf("http://localhost:4200"))
class ProductController {
    //
    @Autowired private lateinit var productService: ProductService
    @Autowired private lateinit var locationService: LocationService

    /**
     * @author Simon
     *
     * The /product endpoint to retrieve a certain product
     */
    @PostMapping("/available")
    fun checkProduct(@RequestBody checkProductRequest: CheckProductRequest): String {
        val products : JSONObject = checkProductAvailability(checkProductRequest)
        return objectToString(products)
    }

    /**
     * @author Simon
     *
     * The /product endpoint to retrieve a certain product at all end points
     */
    @PostMapping("/available/allLocations")
    fun checkProductAllLocations(@RequestBody checkProductAllLocationsRequest : CheckProductAllLocationsRequest): String {
        val locationsAvailability : MutableList<JSONObject> = mutableListOf()
        val allLocations : MutableSet<Location> = this.locationService.getLocations()
        val productId : String? = checkProductAllLocationsRequest.productId

        if(!productId.isNullOrBlank()){
            allLocations.forEach { currentLocation : Location ->
                val checkProductRequest = CheckProductRequest(storeId = currentLocation.smythsId, productId = productId)
                val productAvailability = checkProductAvailability(checkProductRequest)
                locationsAvailability.add(productAvailability)
            }
        }
        return objectToString(locationsAvailability)
    }

    @PostMapping("/all")
    fun getAllProducts(@RequestBody getProductsRequest: GetProductsRequest): String {
        var productsList : Iterable<Product> = listOf()
        val lowRange : Int? = getProductsRequest.lowRange
        val highRange : Int? = getProductsRequest.highRange
        val validLowRange : Boolean = lowRange != null && lowRange >= 0
        val validHighRange : Boolean =  highRange != null && highRange > 0

        if(validLowRange && validHighRange) {
            productsList = this.productService.getAllProducts(lowRange as Int,  highRange as Int)
        }
        return objectToString(productsList)
    }

    @PostMapping("/search")
    fun searchProducts(@RequestBody searchProductsRequest: SearchProductsRequest): String {
        val searchQuery : String? = searchProductsRequest.search
        val validSearchQuery : Boolean = !searchQuery.isNullOrBlank()
        val searchQueryResponse : SearchQueryResponse = makeSearchResponse(searchQuery, validSearchQuery)
        return objectToString(searchQueryResponse)
    }

    private fun checkProductAvailability(checkProductRequest: CheckProductRequest) : JSONObject{
        var response = JSONObject()
        val productId : String? = checkProductRequest.productId
        val storeId : String? = checkProductRequest.storeId
        if(!productId.isNullOrBlank() && !storeId.isNullOrBlank()){
            response = this.productService.checkProductAvailability(productId.toString(), storeId.toString())
        }
        return response
    }

    private fun makeSearchResponse(searchQuery : String?, validSearchQuery : Boolean) : SearchQueryResponse{
        if(validSearchQuery) {
            return this.productService.searchForProducts(searchQuery.toString())
        }
        return SearchQueryResponse(message = "Invalid parameters for 'search'",
                                   error = "Null Query",
                                   products = objectToString(listOf<Product>()))
    }
}
