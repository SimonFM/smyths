package com.nintendont.smyths.data.repository

import com.nintendont.smyths.data.Categories
import com.nintendont.smyths.data.Products
import com.nintendont.smyths.data.interfaces.CrudRepository
import com.nintendont.smyths.data.schema.Category
import com.nintendont.smyths.data.schema.Product
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

interface CategoryRepository: CrudRepository<Category, Long>

@Repository("categoryRepository")
@Transactional
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
        it[id] = category.id
    }

    private fun fromRow(r: ResultRow) = Category(r[Categories.name], r[Categories.id])
}