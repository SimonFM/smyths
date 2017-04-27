package com.nintendont.smyths.data.schema

import java.math.BigDecimal

data class Region(var regionName: String,
                  var regionImage : String,
                  var regionPos : MutableList<Location> )