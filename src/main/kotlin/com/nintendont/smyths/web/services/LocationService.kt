package com.nintendont.smyths.web.services

import com.nintendont.smyths.utils.http.HttpHandler
import com.nintendont.smyths.data.repository.*
import com.nintendont.smyths.data.schema.*
import com.nintendont.smyths.utils.Constants.SMYTHS_LOCATIONS_URL
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service open class LocationService {

    private val httpHandler : HttpHandler = HttpHandler()
    @Autowired lateinit var locationRepository : SmythsLocationRepository

    /**
     * Generates the locations from smyths.ie and stores them in the Location Table.
     * @return Set of Locations
     */
    fun generateLocations() : MutableSet<Location>{
        println("Generating Locations....")
        val locationsResult = mutableSetOf<Location>()
        val params: MutableList<Pair<String, Any>> = mutableListOf()
        val response: Document = httpHandler.get(SMYTHS_LOCATIONS_URL, params)
        val locationsDiv : Elements = response.select("div#store-locator")
        val locationsResultsDiv : Elements = locationsDiv.select("div.store-locator-results")
        val locationsContent : Elements = locationsResultsDiv.select("div.StoreContent")
        val allLocations : Elements = locationsContent.select("div.store-locator-list-all")
        val regions : Elements = allLocations.select("div.region-info")

        for(region in regions){
            val locations : Elements = region.select("[href]")
            for(location in locations){
                val locationName = location.text()
                val locationId = location.attr("data-storeid")
                val newLocation : Location = Location(name =locationName , id = makeUUID(), smythsId = locationId )
                val existingLocation : Location = locationRepository.find(locationName)
                if (existingLocation.name.isNotBlank()){
                    locationsResult.add(existingLocation)
                    println("Found existing Location for location: $existingLocation")
                } else{
                    locationRepository.create(newLocation)
                    locationsResult.add(newLocation)
                    println("New Location Saved: $newLocation")
                }
            }
        }
        return locationsResult
    }

    /**
     * Makes a unique identifier
     * @return Id as a string
     */
    private fun makeUUID() : String{
       return UUID.randomUUID().toString()
    }
}