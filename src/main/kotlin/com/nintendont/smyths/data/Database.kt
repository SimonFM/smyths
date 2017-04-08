package com.nintendont.smyths.data

import org.jetbrains.exposed.sql.*

object Products : Table() {
    var id = text("id").primaryKey()
    var smythsId = long("smythsId")
    var name = text("name")
    var price = decimal("price", 10, 2)
    var categoryId = text("categoryId")
    var listId = text("listId")
    var brandId = text("brandId")
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