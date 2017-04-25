package com.nintendont.smyths.data.schema.responses

data class CheckProductResponse(var message : String,
                                var inStoreStatus : String,
                                var locationId : String,
                                var productId : String,
                                var canCollect : Boolean)