package com.nintendont.smyths.data.repository

import com.nintendont.smyths.data.Openings
import com.nintendont.smyths.data.interfaces.CrudRepository
import com.nintendont.smyths.data.schema.Opening
import com.nintendont.smyths.utils.Utils.makeEmptyOpening
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

interface OpeningsRepository : CrudRepository<Opening, Long>

@Repository("openingsRepository")
@Transactional
open class SmythsOpeningsRepository : OpeningsRepository{

    override fun createTable() = SchemaUtils.create(Openings)

    override fun update(opening: Opening): Opening {
        val id = opening.id.toString()
        val query : Int = Openings.update({ Openings.id.eq(id)} , body = {
            toRow(opening)
        })
        val result = query.run {}
        return opening
    }

    override fun create(location: Opening): Opening {
        Openings.insert(toRow(location))
        return location
    }

    override fun findAll(): Iterable<Opening> {
        return Openings.selectAll().map { fromRow(it) }
    }

    override fun find(id : String): Opening {
        val query : Query = Openings.select{ Openings.id.eq(id)}
        var location : Opening = makeEmptyOpening()
        query.forEach {
            location = fromRow(it)
        }
        return location
    }

    override fun deleteAll(): Int {
        return Openings.deleteAll()
    }

    private fun toRow(opening: Opening): Openings.(UpdateBuilder<*>) -> Unit = {
        it[id] = opening.id
        it[time] = opening.time
        it[day] = opening.day
    }

    private fun fromRow(r: ResultRow) : Opening {
        return Opening(id = r[Openings.id], time = r[Openings.time], day = r[Openings.day])
    }
}