package com.nintendont.smyths.data.repository

import com.nintendont.smyths.data.Brands
import com.nintendont.smyths.data.Links
import com.nintendont.smyths.data.ListTypes
import com.nintendont.smyths.data.interfaces.CrudRepository
import com.nintendont.smyths.data.schema.ListType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

interface ListTypeRepository : CrudRepository<ListType, Long>

@Repository("listTypeRepository")
@Transactional
open class SmythsListTypeRepository : ListTypeRepository{

    override fun update(list: ListType): ListType {
        val query : Int = ListTypes.update({ ListTypes.id.eq(list.id) }, body = {
            it[name] = list.name
            it[id] = list.id
        })
        query.run {}
        return list
    }

    override fun createTable() = SchemaUtils.create(ListTypes)

    override fun create(listType : ListType): ListType {
        ListTypes.insert(toRow(listType))
        return listType
    }

    override fun findAll(): Iterable<ListType> {
        return ListTypes.selectAll().map { fromRow(it) }
    }

    override fun find(name : String): ListType {
        val query : Query = ListTypes.select{ ListTypes.name.eq(name)}
        var listType : ListType = ListType("", "")
        query.forEach {
            listType = ListType(it[ListTypes.name], it[ListTypes.id])
        }
        return listType
    }

    override fun deleteAll(): Int {
        return ListTypes.deleteAll()
    }

    private fun toRow(listType: ListType): ListTypes.(UpdateBuilder<*>) -> Unit = {
        it[name] = listType.name
        it[id] = listType.id
    }

    private fun fromRow(r: ResultRow) = ListType(r[ListTypes.name], r[ListTypes.id])
}