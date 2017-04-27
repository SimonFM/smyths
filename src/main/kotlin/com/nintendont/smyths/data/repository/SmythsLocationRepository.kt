package com.nintendont.smyths.data.repository

import com.nintendont.smyths.data.Locations
import com.nintendont.smyths.data.Products
import com.nintendont.smyths.data.interfaces.CrudRepository
import com.nintendont.smyths.data.schema.Location
import com.nintendont.smyths.data.schema.Opening
import com.nintendont.smyths.utils.Utils.makeEmptyLocation
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import javax.transaction.Transactional

interface LocationRepository : CrudRepository<Location, Long>

@Repository("locationRepository")
@Transactional
open class SmythsLocationRepository : LocationRepository{

    override fun createTable() = SchemaUtils.create(Locations)

    override fun update(location: Location): Location {
        val query : Int = Locations.update({ Locations.name.eq(location.name)} , body = {
            toRow(location)
        })
        val result = query.run {}
        return location
    }

    override fun create(location: Location): Location {
        Locations.insert(toRow(location))
        return location
    }

    override fun findAll(): Iterable<Location> {
        return Locations.selectAll().map { fromRow(it) }
    }

    override fun find(name : String): Location {
        val query : Query = Locations.select{Locations.name.eq(name)}
        var location : Location = makeEmptyLocation()
        query.forEach {
            location = fromRow(it)
        }
        return location
    }

    override fun deleteAll(): Int {
        return Locations.deleteAll()
    }

    private fun toRow(location: Location): Locations.(UpdateBuilder<*>) -> Unit = {
        it[name] = location.name
        it[displayName] = location.displayName
        it[storeMapUrl] = location.storeMapUrl
        it[longitude] = location.longitude
        it[latitude] = location.latitude
        it[line1] = location.line1
        it[line2] = location.line2
        it[phone] = location.phone
        it[postalCode] = location.postalCode
        it[mapImage] = location.mapImage
        it[formattedDistance] = location.formattedDistance
        it[town] = location.town
        it[country] = location.country
        it[image] = location.image
        it[url] = location.url
    }

    private fun fromRow(r: ResultRow) = Location(name=r[Locations.name], storeMapUrl = r[Locations.storeMapUrl],
            displayName = r[Locations.displayName], longitude = r[Locations.longitude], latitude = r[Locations.latitude],
            line1 = r[Locations.line1], line2 = r[Locations.line2], phone = r[Locations.phone], postalCode = r[Locations.postalCode],
            mapImage = r[Locations.mapImage], formattedDistance = r[Locations.formattedDistance], town = r[Locations.town],
            country= r[Locations.country], image = r[Locations.image], url = r[Locations.url])
}