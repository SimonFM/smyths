package com.nintendont.smyths.web.controllers

import com.nintendont.smyths.data.schema.Location
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
    @GetMapping("/all", produces = arrayOf("application/json"))
    fun getLocations(): MutableSet<Location> {
        return this.locationService.getLocations()
    }
}
