package com.nintendont.smyths.repository

import com.nintendont.smyths.data.Categories
import com.nintendont.smyths.data.Products
import com.nintendont.smyths.schema.Category
import com.nintendont.smyths.schema.Product
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.springframework.stereotype.Repository

interface CategoryRepository: CrudRepository<Category, Long>

@Repository
open class SmythsCategoryRepository : CategoryRepository{

    override fun createTable() = SchemaUtils.create(Categories)

    override fun create(category: Category): Category {
        Categories.insert(toRow(category))
        return category
    }

    override fun findAll(): Iterable<Category> {
        return Categories.selectAll().map { fromRow(it) }
    }

    override fun deleteAll(): Int {
        return Categories.deleteAll()
    }

    private fun toRow(category: Category): Categories.(UpdateBuilder<*>) -> Unit = {
        it[name] = category.name
    }

    private fun fromRow(r: ResultRow) = Category(r[Categories.name], r[Categories.id])
}