package com.nintendont.smyths.utils

import java.math.BigDecimal

object Utils{

    fun stringToBigDecimal(number : String ) : BigDecimal{
        val str = number.replace(",".toRegex(), "")
        val bd = BigDecimal(str)
        return bd
    }
}