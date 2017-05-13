package com.nintendont.smyths.data.schema.requests

data class SearchProductHistoryRequest(var low : Int = 0,
                                       var high : Int = 1000,
                                       var search : String? = null)