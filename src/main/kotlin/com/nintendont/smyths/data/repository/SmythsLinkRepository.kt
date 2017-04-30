package com.nintendont.smyths.data.repository

import com.google.gson.Gson
import com.nintendont.smyths.data.Links
import com.nintendont.smyths.data.interfaces.CrudRepository
import com.nintendont.smyths.data.schema.Link
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.json.JSONObject
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

interface LinkRepository : CrudRepository<Link, Long>

@Repository("linkRepository")
@Transactional
open class SmythsLinkRepository : LinkRepository {

    override fun update(item: Link): Link {
        val linkToUpdate : Link = find(item.url)
        try{
            val query : Int = Links.update({ Links.id.eq(linkToUpdate.id) }, body = {})
            query.run {}
            return linkToUpdate
        } catch (e :Exception){
            return Link("", "", "")
        }
    }

    override fun createTable() = SchemaUtils.create(Links)

    override fun create(link: Link): Link {
        val existingLink : Link = find(link.url)
        if(existingLink.name.isEmpty()){
            Links.insert(toRow(link))
        }
        return existingLink
    }

    override fun findAll(): Iterable<Link> {
        return Links.selectAll().map { fromRow(it) }
    }

    override fun find(url : String): Link {
        val query : Query = Links.select{Links.url.eq(url)}
        var link : Link = Link("", "", "")
        query.forEach {
            link = fromRow(it)
        }
        return link
    }

    override fun deleteAll(): Int {
        return Links.deleteAll()
    }

    private fun toRow(link: Link): Links.(UpdateBuilder<*>) -> Unit = {
        it[url] = link.url
        it[id] = link.id
        it[name] = link.name
    }

    private fun fromRow(r: ResultRow) = Link(r[Links.url],
                                             r[Links.id],
                                             r[Links.name])
}
