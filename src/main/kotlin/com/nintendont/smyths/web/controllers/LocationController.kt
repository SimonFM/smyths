package com.nintendont.smyths.web.controllers

import com.nintendont.smyths.utils.Utils.objectToString
import com.nintendont.smyths.web.services.LocationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController @RequestMapping("/location")
@CrossOrigin(origins = arrayOf("http://localhost:4200"))
class LocationController {
    //
    @Autowired private lateinit var locationService: LocationService

    /**
     * @author Simon
     *
     * The location endpoint to fetch all locations in the db
     */
    @GetMapping("/all")
    fun getLocations(): String {
        val links = this.locationService.getLocations()
        return objectToString(links)
    }
}
