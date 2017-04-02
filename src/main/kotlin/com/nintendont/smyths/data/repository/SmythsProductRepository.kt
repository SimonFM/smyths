package com.nintendont.smyths.data.repository

import com.nintendont.smyths.data.Products
import com.nintendont.smyths.data.interfaces.CrudRepository
import com.nintendont.smyths.data.schema.Product
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import javax.transaction.Transactional

interface ProductRepository: CrudRepository<Product, Long>

@Repository("productRepository")
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

    override fun find(id : String): Product {
        val query : Query = Products.select{ Products.smythsId.eq(id.toLong())}
        var product : Product = Product("", 0, "", BigDecimal.ZERO ,"", "", "")
        query.forEach {
            product = Product(it[Products.id], it[Products.smythsId], it[Products.name],
                            it[Products.price], it[Products.categoryId], it[Products.listId], it[Products.brandId])
        }
        return product
    }

    override fun deleteAll(): Int {
        return Products.deleteAll()
    }

    private fun toRow(product: Product): Products.(UpdateBuilder<*>) -> Unit = {
        it[id] = product.id
        it[smythsId] = product.smythsId
        it[name] = product.name
        it[price] = product.price
        it[categoryId] = product.categoryId
        it[listId] = product.listTypeId
        it[brandId] = product.brandId
    }

    private fun fromRow(r: ResultRow) =
            Product( r[Products.id],r[Products.smythsId], r[Products.name],
                     r[Products.price], r[Products.categoryId], r[Products.listId], r[Products.brandId])
}
