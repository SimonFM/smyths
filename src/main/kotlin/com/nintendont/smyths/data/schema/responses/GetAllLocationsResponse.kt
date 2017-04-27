package com.nintendont.smyths.data.schema.responses

import com.nintendont.smyths.data.schema.Region


data class GetAllLocationsResponse(var total : Int,
                                   var countryImage : String,
                                   var data : MutableList<Region>)