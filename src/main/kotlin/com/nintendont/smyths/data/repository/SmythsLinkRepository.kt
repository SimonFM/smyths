package com.nintendont.smyths.data.repository

import com.nintendont.smyths.data.Links
import com.nintendont.smyths.data.interfaces.CrudRepository
import com.nintendont.smyths.data.schema.Link
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

interface LinkRepository : CrudRepository<Link, Long>

@Repository("linkRepository")
@Transactional
open class SmythsLinkRepository : LinkRepository {

    override fun createTable() = SchemaUtils.create(Links)

    override fun create(link: Link): Link {
        Links.insert(toRow(link))
        return link
    }

    override fun findAll(): Iterable<Link> {
        return Links.selectAll().map { fromRow(it) }
    }

    override fun find(url : String): Link {
        val query : Query = Links.select{Links.url.eq(url)}
        var link : Link = Link("", "")
        query.forEach {
            link = Link(it[Links.url], it[Links.id])
        }
        return link
    }

    override fun deleteAll(): Int {
        return Links.deleteAll()
    }

    private fun toRow(link: Link): Links.(UpdateBuilder<*>) -> Unit = {
        it[url] = link.url
        it[id] = link.id
    }

    private fun fromRow(r: ResultRow) = Link(r[Links.url], r[Links.id])
}