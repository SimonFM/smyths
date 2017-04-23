package com.nintendont.smyths.web.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

/**
 * Service to automatically schedule syncing of the data
 */
@Service open class JobService{
    @Autowired lateinit var httpService : HttpService

    /**
     * Scheduled at half five every day
     */
    @Scheduled(cron = "0 0/30 5 * * *")
    fun syncLinks(){
        val params: MutableList<Pair<String, Any>> = mutableListOf()
        val syncLinkResponse = httpService.getJson("http://localhost:8888/catalogue/sync/links", params)
        println("Sync Link response: $syncLinkResponse")
    }

    /**
     * Scheduled at half seven every day
     */
    @Scheduled(cron = "0 0/40 7 * * *")
    fun syncProducts(){
        val params: MutableList<Pair<String, Any>> = mutableListOf()
        val syncProductsResponse = httpService.getJson("http://localhost:8888/catalogue/sync/products", params)
        println("Sync Products response: $syncProductsResponse")
    }

    /**
     * Scheduled at half six every day
     */
    @Scheduled(cron = "0 0/50 6 * * *")
    fun syncLocations(){
        val params: MutableList<Pair<String, Any>> = mutableListOf()
        val syncLocationsResponse = httpService.getJson("http://localhost:8888/catalogue/sync/locations", params)
        println("Sync Locations response: $syncLocationsResponse")
    }
}