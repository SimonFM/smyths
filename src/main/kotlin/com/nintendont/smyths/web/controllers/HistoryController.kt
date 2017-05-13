package com.nintendont.smyths.web.controllers

import com.nintendont.smyths.data.schema.HistoricalProduct
import com.nintendont.smyths.data.schema.requests.SearchProductHistoryRequest
import com.nintendont.smyths.data.schema.responses.HistoryResponse
import com.nintendont.smyths.web.services.HistoryService
import org.json.JSONArray
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController @RequestMapping("/history")
@CrossOrigin(origins = arrayOf("http://localhost:4200", "*"))
class HistoryController {
    //
    @Autowired private lateinit var historyService: HistoryService

    /**
     * The history endpoint to fetch all histories in the db
     */
    @PostMapping("/product/all", produces = arrayOf("application/json"))
    fun getLocations(@RequestBody searchProductHistoryRequest: SearchProductHistoryRequest): MutableList<HistoricalProduct> {
        val productId : String? = searchProductHistoryRequest.search
        val low : Int = searchProductHistoryRequest.low
        val high : Int = searchProductHistoryRequest.high
        return this.historyService.getHistoryForProduct(low, high, productId.toString() )
    }

    /**
     * The history endpoint to fetch all histories in the db
     */
    @PostMapping("/product/search", produces = arrayOf("application/json"))
    fun getHistoriesFor(@RequestBody searchProductHistoryRequest: SearchProductHistoryRequest): HistoryResponse {
        val productName : String? = searchProductHistoryRequest.search
        val low : Int = searchProductHistoryRequest.low
        val high : Int = searchProductHistoryRequest.high
        val histories = this.historyService.getPriceAndDateForProduct(low, high, productName.toString() )
        val response = HistoryResponse(message = "Here are the histories", histories = histories)
        return response
    }
}
