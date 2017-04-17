package com.nintendont.smyths.web.services

import com.nintendont.smyths.utils.http.HttpHandler
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

/**
 * Service to automatically schedule syncing of the data
 */
@Service open class JobService{
    private val httpHandler : HttpHandler = HttpHandler()

    /**
     * Scheduled at half five every day
     */
    @Scheduled(cron = "0 0/30 5 * * *")
    fun syncLinks(){
        val params: MutableList<Pair<String, Any>> = mutableListOf()
        val syncLinkResponse = httpHandler.getJson("http://localhost:8888/catalogue/sync/links", params)
        println("Sync Link response: $syncLinkResponse")
    }

    /**
     * Scheduled at half seven every day
     */
    @Scheduled(cron = "0 0/40 7 * * *")
    fun syncProducts(){
        val params: MutableList<Pair<String, Any>> = mutableListOf()
        val syncProductsResponse = httpHandler.getJson("http://localhost:8888/catalogue/sync/products", params)
        println("Sync Products response: $syncProductsResponse")
    }

    /**
     * Scheduled at half six every day
     */
    @Scheduled(cron = "0 0/50 6 * * *")
    fun syncLocations(){
        val params: MutableList<Pair<String, Any>> = mutableListOf()
        val syncLocationsResponse = httpHandler.getJson("http://localhost:8888/catalogue/sync/locations", params)
        println("Sync Locations response: $syncLocationsResponse")
    }
}