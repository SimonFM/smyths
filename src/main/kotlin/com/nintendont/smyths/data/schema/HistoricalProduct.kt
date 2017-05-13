package com.nintendont.smyths.data.schema

import org.joda.time.DateTime
import java.math.BigDecimal

data class HistoricalProduct (var id: String,
                              var productId: String,
                              var smythsCode: Long,
                              var smythsStockCheckCode: Long? = null,
                              var name: String,
                              var price: BigDecimal,
                              var categoryId: String? = null,
                              var brandId: String? = null,
                              var url : String,
                              var date : DateTime)
