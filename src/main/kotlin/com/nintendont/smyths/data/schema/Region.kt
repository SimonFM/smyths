package com.nintendont.smyths.data.schema

data class Region(var regionName: String,
                  var regionImage : String,
                  var regionPos : MutableList<Location> )