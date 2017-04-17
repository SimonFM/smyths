package com.nintendont.smyths.data

import org.jetbrains.exposed.sql.*

object Products : Table() {
    var id = text("id").primaryKey()
    var smythsId = long("smythsId")
    var smythsStockCheckId = long("smythsStockCheckId")
    var name = varchar("name", 400) // changed to varchar for 'LIKE'
    var price = decimal("price", 10, 2)
    var categoryId = (text("categoryId") references Categories.id).nullable()
    var listId = (text("listId") references ListTypes.id).nullable()
    var brandId = (text("brandId") references Brands.id).nullable()
    var url = text("url")
}

object Categories : Table() {
    var id = text("id").primaryKey()
    var name = text("name")
}

object ListTypes : Table() {
    var id  = text("id").primaryKey()
    var name = text("name")
}

object Brands : Table() {
    var id  = text("id").primaryKey()
    var name = text("name")
}

object Links : Table() {
    var id  = text("id").primaryKey()
    var url = text("url")
    var links = text("links")
}

object Locations : Table() {
    var id = text("id").primaryKey()
    var name = text("name")
    var smythsId = text("smythsId")
}