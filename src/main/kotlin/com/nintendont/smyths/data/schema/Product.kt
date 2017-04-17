package com.nintendont.smyths.data.schema

import java.math.BigDecimal

data class Product (var id: String,
                    var smythsId : Long,
                    var smythsStockCheckId : Long,
                    var name: String,
                    var price: BigDecimal,
                    var categoryId: String? = null,
                    var listTypeId: String? = null,
                    var brandId: String? = null,
                    var url : String )
