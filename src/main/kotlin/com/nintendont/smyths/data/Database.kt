package com.nintendont.smyths.data

import org.jetbrains.exposed.sql.*

object Products : Table() {
    var id = text("id").primaryKey()
    var smythsCode = long("smythsCode")
    var smythsStockCheckCode = long("smythsStockCheckCode").nullable()
    var name = varchar("name", 400) // changed to varchar for 'LIKE'
    var price = decimal("price", 10, 2)
    var categoryId = (text("categoryId") references Categories.id).nullable()
    var brandId = (text("brandId") references Brands.id).nullable()
    var url = text("url")
}

object HistoricalProducts : Table() {
    var id = text("id").primaryKey()
    var productId = text("productId") references Products.id
    var smythsCode = long("smythsCode")
    var smythsStockCheckCode = long("smythsStockCheckCode").nullable()
    var name = varchar("name", 400) // changed to varchar for 'LIKE'
    var price = decimal("price", 10, 2)
    var categoryId = (text("categoryId") references Categories.id).nullable()
    var brandId = (text("brandId") references Brands.id).nullable()
    var url = text("url")
    var dateCreated = date("dateCreated")
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
    var name = text("name")
}

object Openings : Table(){
    var id = text("openingsId").primaryKey()
    var day = text("day")
    var time = text("time")
}

object Locations : Table() {
    var name = text("name").primaryKey()
    var url = text("url")
    var displayName = text("displayName")
    var phone = text("phone")
    var formattedDistance = text("formattedDistance")
    var line1 = text("line1")
    var line2 = text("line2")
    var town = text("town")
    var country = text("country")
    var postalCode = text("postalCode")
    var latitude = decimal("latitude", 10, 8)
    var longitude = decimal("longitude", 10, 8)
    var openingsId = (text("openingsId")).nullable()
    var mapImage = text("mapImage")
    var image = text("image")
    var storeMapUrl = text("storeMapUrl")
}