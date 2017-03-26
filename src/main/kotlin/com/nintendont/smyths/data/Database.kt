package com.nintendont.smyths.data

import org.jetbrains.exposed.sql.*
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

object Products : Table() {
    var name = text("name")
    var price = decimal("price", 10, 2)
    var id = long("id").primaryKey()
    //var category: Category = Category("", 0)
    //var productList: ProductList = ProductList("", 0)
    //var brand: Brand = Brand("")
}

object Categories : Table() {
        var name = text("name")
        @Id @GeneratedValue(strategy = GenerationType.AUTO)var id = long("id")
}