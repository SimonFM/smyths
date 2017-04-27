package com.nintendont.smyths.utils

import com.google.gson.Gson
import com.nintendont.smyths.data.schema.Location
import com.nintendont.smyths.data.schema.Opening
import java.math.BigDecimal

object Utils{

    /**
     * @author Simon
     * @param number - String to convert to a BigDecimal
     * @return stringAsBigDecimal - String converted to a BigDecimal
     */
    fun stringToBigDecimal(number : String ) : BigDecimal{
        val processedString = number.replace(",".toRegex(), "")
        val stringAsBigDecimal = BigDecimal(processedString)
        return stringAsBigDecimal
    }

    fun objectToString(any: Any) : String{
        return Gson().toJson(any).toString()
    }

    fun makeEmptyLocation(): Location {
        val openings = mutableListOf<Opening>()
        return Location(name = "", storeMapUrl = "", displayName = "", line1 = "", line2 = "", postalCode = "",
                url = "", mapImage = "", image = "", country = "", town = "",
                latitude = BigDecimal.ZERO, longitude = BigDecimal.ZERO, formattedDistance = "", phone = "")
    }
}