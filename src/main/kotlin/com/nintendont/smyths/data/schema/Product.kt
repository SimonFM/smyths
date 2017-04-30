package com.nintendont.smyths.data.schema

import java.math.BigDecimal

data class Product (var id: String,
                    var smythsCode: Long,
                    var smythsStockCheckCode: Long? = null,
                    var name: String,
                    var price: BigDecimal,
                    var categoryId: String? = null,
                    var brandId: String? = null,
                    var url : String )
