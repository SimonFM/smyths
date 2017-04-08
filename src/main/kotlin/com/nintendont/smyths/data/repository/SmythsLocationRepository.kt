package com.nintendont.smyths.data.repository

import com.nintendont.smyths.data.Locations
import com.nintendont.smyths.data.interfaces.CrudRepository
import com.nintendont.smyths.data.schema.Location
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

interface LocationRepository : CrudRepository<Location, Long>

@Repository("locationRepository")
@Transactional
open class SmythsLocationRepository : LocationRepository{

    override fun createTable() = SchemaUtils.create(Locations)

    override fun create(location: Location): Location {
        Locations.insert(toRow(location))
        return location
    }

    override fun findAll(): Iterable<Location> {
        return Locations.selectAll().map { fromRow(it) }
    }

    override fun find(name : String): Location {
        val query : Query = Locations.select{Locations.name.eq(name)}
        var location : Location = Location("", "", "")
        query.forEach {
            location = Location(name = it[Locations.name], id =it[Locations.id], smythsId = it[Locations.smythsId])
        }
        return location
    }

    override fun deleteAll(): Int {
        return Locations.deleteAll()
    }

    private fun toRow(location: Location): Locations.(UpdateBuilder<*>) -> Unit = {
        it[name] = location.name
        it[id] = location.id
        it[smythsId] = location.smythsId
    }

    private fun fromRow(r: ResultRow) = Location(r[Locations.name], r[Locations.id], r[Locations.smythsId])
}