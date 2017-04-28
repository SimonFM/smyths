package com.nintendont.smyths.data.schema

import java.math.BigDecimal

data class Location(var name: String,
                    var displayName : String,
                    var url : String,
                    var phone : String,
                    var formattedDistance : String,
                    var line1 : String,
                    var line2 : String,
                    var town : String,
                    var country : String,
                    var postalCode : String,
                    var latitude: BigDecimal,
                    var longitude: BigDecimal,
                    var openings : Map<String, String>,
                    var openingsId : String?,
                    var mapImage : String,
                    var image : String,
                    var storeMapUrl: String )