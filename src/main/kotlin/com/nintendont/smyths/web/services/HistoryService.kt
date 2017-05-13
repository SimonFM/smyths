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


@Service open class HistoryService {

    @Autowired lateinit var historicalProductRepository : SmythsHistoricalProductRepository


    fun getPriceAndDateForProduct(low : Int, high : Int, productId : String) : MutableList<HistoricalProduct> {
        val allHistory = getHistoryForProduct(low, high, productId)
        return allHistory
    }

    /**
     * Fetches all the locations from the database.
     */
    fun getHistoryForProduct(low : Int, high : Int, productId : String) : MutableList<HistoricalProduct> {
        println("....  Finding All Historys   ....")
        val allHistory : MutableList<HistoricalProduct> = this.historicalProductRepository.findAllInRange(low, high, productId)
        println("....  Found All Historys   ....")
        return allHistory
    }


}