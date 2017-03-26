package com.nintendont.smyths.schema

import java.math.BigDecimal
import javax.persistence.*

data class Product (var name: String,
                    var price: BigDecimal,
                  //  var category: Category,
                  //  var productList: ProductList,
                 //   var brand: Brand,
                    var id: Long
        )
