package com.nintendont.smyths.web.services

import com.nintendont.smyths.data.repository.*
import com.nintendont.smyths.data.schema.*
import com.nintendont.smyths.utils.Constants.SMYTHS_LOCATIONS_URL
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nintendont.smyths.data.schema.responses.GetAllLocationsResponse


@Service open class LocationService {

    @Autowired lateinit var httpService : HttpService
    @Autowired lateinit var locationRepository : SmythsLocationRepository

    /**
    * Generates the locations from smyths.ie and stores them in the Location Table.
    * @return Set of Locations
    */
    fun generateLocationsFromJson() : GetAllLocationsResponse{
        println("Generating Locations....")
        val locationsResult = mutableSetOf<Location>()
        val params: MutableList<Pair<String, Any>> = mutableListOf()
        val response: JSONObject = this.httpService.getJson(SMYTHS_LOCATIONS_URL, params)
        val allLocationsResponse = object : TypeToken<GetAllLocationsResponse>() {}.type
        val locations : GetAllLocationsResponse = Gson().fromJson(response.toString(), allLocationsResponse)

        locations.data.forEach { region ->
            region.regionPos.forEach { location ->
                val existingLocation : Location = locationRepository.find(location.name)
                if(existingLocation.displayName.isNullOrEmpty()){
                    locationRepository.create(location = location)
                } else {
                    // update
                }
            }
        }
        return locations
    }

    /***
     * @author Simon
     * Fetches all the locations from the database.
     */
    fun getLocations() : MutableSet<Location> {
        println("....  Finding All Locations   ....")
        val allLocations = this.locationRepository.findAll()
        println("....  Found All Locations   ....")
        return allLocations.toMutableSet()
    }

    /**
     * Makes a unique identifier
     * @return Id as a string
     */
    private fun makeUUID() : String{
       return UUID.randomUUID().toString()
    }
}