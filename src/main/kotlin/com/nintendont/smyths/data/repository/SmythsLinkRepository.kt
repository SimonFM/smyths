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
        val linksAsString : String = linkToUpdate.links
        try{
            val jsonObject = JSONObject(linksAsString)
            jsonObject.append("values", item.url)
            var newJson = Gson().toJson(linksAsString)
            linkToUpdate.links = newJson.toString()

            val query : Int = Links.update({ Links.id.eq(linkToUpdate.id) }, body = {
                it[links] = newJson.toString()
            })
            query.run {}
            return linkToUpdate
        } catch (e :Exception){
            return Link("", "", "")
        }
    }

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
        var link : Link = Link("", "", "")
        query.forEach {
            link = Link(it[Links.url], it[Links.id], it[Links.links])
        }
        return link
    }

    override fun deleteAll(): Int {
        return Links.deleteAll()
    }

    private fun toRow(link: Link): Links.(UpdateBuilder<*>) -> Unit = {
        it[url] = link.url
        it[id] = link.id
        it[links] = link.links
    }

    private fun fromRow(r: ResultRow) = Link(r[Links.url], r[Links.id], r[Links.links])
}
