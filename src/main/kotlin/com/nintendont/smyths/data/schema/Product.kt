package com.nintendont.smyths.data.schema

import java.math.BigDecimal

data class Product (var id: String, var smythsId : Long, var smythsStockCheckId : Long,  var name: String, var price: BigDecimal,
                    var categoryId: String, var listTypeId: String, var brandId: String, var url : String )
