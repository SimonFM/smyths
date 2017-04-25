package com.nintendont.smyths.data.schema.requests

data class SearchProductsRequest(var search : String? = null,
                                 var locationId : String? = null)