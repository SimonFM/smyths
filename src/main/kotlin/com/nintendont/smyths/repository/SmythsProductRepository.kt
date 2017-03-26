package com.nintendont.smyths.repository

import com.nintendont.smyths.data.Products
import com.nintendont.smyths.schema.Product
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

interface ProductRepository: CrudRepository<Product, Long>

@Repository("productRepo")
@Transactional
open class SmythsProductRepository : ProductRepository{

    override fun createTable() = SchemaUtils.create(Products)

    override fun create(product: Product): Product {
        Products.insert(toRow(product))
        return product
    }

    override fun findAll(): Iterable<Product> {
        return Products.selectAll().map { fromRow(it) }
    }

    override fun deleteAll(): Int {
        return Products.deleteAll()
    }

    private fun toRow(product: Product): Products.(UpdateBuilder<*>) -> Unit = {
        it[name] = product.name
        it[price] = product.price
        it[id] = product.id
    }

    private fun fromRow(r: ResultRow) =
            Product(r[Products.name], r[Products.price], r[Products.id])//, r[Products.id])
}
