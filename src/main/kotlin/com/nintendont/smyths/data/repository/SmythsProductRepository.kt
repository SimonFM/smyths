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

    override fun update(product: Product): Product {
        val query : Int = Products.update({Products.smythsId.eq(product.smythsId)} , body = {
            it[smythsId] = product.smythsId
            it[smythsStockCheckId] = product.smythsStockCheckId
            it[name] = product.name
            it[price] = product.price
            it[categoryId] = product.categoryId
            it[listId] = product.listTypeId
            it[brandId] = product.brandId
            it[url] = product.url
        })
        val result = query.run {}
        return product
    }

    override fun findAll(): Iterable<Product> {
        return Products.selectAll().map { fromRow(it) }
    }

    fun findAllInRange(low : Int, high : Int): List<Product> {
        val allProducts : Iterable<Product> = findAll()
        val filteredList : List<Product> = allProducts.toList().subList(low, high)
        return filteredList
    }

    fun searchForProducts(searchQuery : String) : MutableSet<Product>{
        val listOfProducts : Iterable<Product> = findAll()
        val queryAsLowerCase : String = searchQuery.toLowerCase()
        val selectedProducts : MutableSet<Product> = mutableSetOf()
        //Products.select(where = {Products.name.like("$searchQuery%")}) // TODO: causes an exception for the like query.
        listOfProducts.forEach { product : Product ->
            val productNameAsLowerCase : String = product.name.toLowerCase()
            if(productNameAsLowerCase.contains(queryAsLowerCase)){
                selectedProducts.add(product)
            }
        }
        return selectedProducts
    }

    override fun find(id : String): Product {
        var product : Product = Product("", 0, 0, "", BigDecimal.ZERO , "","", "", "")
        Products.select{ Products.smythsId.eq(id.toLong())}.forEach {
            product = fromRow(it)
        }
        return product
    }

    override fun deleteAll(): Int {
        return Products.deleteAll()
    }

    private fun toRow(product: Product): Products.(UpdateBuilder<*>) -> Unit = {
        it[id] = product.id
        it[smythsId] = product.smythsId
        it[smythsStockCheckId] = product.smythsStockCheckId
        it[name] = product.name
        it[price] = product.price
        it[categoryId] = product.categoryId
        it[listId] = product.listTypeId
        it[brandId] = product.brandId
        it[url] = product.url
    }

    private fun fromRow(r: ResultRow) =
            Product( r[Products.id],
                     r[Products.smythsId],
                     r[Products.smythsStockCheckId],
                     r[Products.name],
                     r[Products.price],
                     r[Products.categoryId],
                     r[Products.listId],
                     r[Products.brandId],
                     r[Products.url])
}
