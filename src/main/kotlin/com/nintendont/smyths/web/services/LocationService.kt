package com.nintendont.smyths.web.services

import com.beust.klaxon.json
import com.github.salomonbrys.kotson.toJsonArray
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
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.nintendont.smyths.data.schema.responses.GetAllLocationsResponse
import kotlin.collections.HashMap


@Service open class LocationService {

    @Autowired lateinit var httpService : HttpService
    @Autowired lateinit var locationRepository : SmythsLocationRepository
    @Autowired lateinit var openingsRepository : SmythsOpeningsRepository

    /**
    * Generates the locations from smyths.ie and stores them in the Location Table.
    * @return Set of Locations
    */
    fun generateLocationsFromJson() : GetAllLocationsResponse{
        println("Generating Locations....")
        val params: MutableList<Pair<String, Any>> = mutableListOf()
        val response: JSONObject = this.httpService.getJson(SMYTHS_LOCATIONS_URL, params)
        val getAllLocationsResponseType = object : TypeToken<GetAllLocationsResponse>() {}.type
        val locations : GetAllLocationsResponse = Gson().fromJson(response.toString(), getAllLocationsResponseType)

        locations.data.forEach { region ->
            region.regionPos.forEach { location ->
                val existingLocation : Location = locationRepository.find(location.name)
                if(existingLocation.displayName.isNullOrEmpty()){
                    val openings : Map<String, String>? = location.openings
                    val listOfIds : MutableSet<String> = mutableSetOf<String>()
                    openings?.forEach { opening ->
                        val openingsId = makeUUID()
                        val dayTimePair : Pair<String, String> = opening.toPair()
                        val openingToSave : Opening = Opening(day = dayTimePair.first, time = dayTimePair.second, id = openingsId)
                        openingsRepository.create(openingToSave)
                        listOfIds.add(openingsId)
                    }
                    location.openingsId = listOfIds.toJsonArray().toString()
                    locationRepository.create(location = location)
                } else {
                    // update
                    locationRepository.update(location)
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