package com.nintendont.smyths.data.repository

import com.nintendont.smyths.data.HistoricalProducts
import com.nintendont.smyths.data.interfaces.CrudRepository
import com.nintendont.smyths.data.schema.HistoricalProduct
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.joda.time.DateTime
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import javax.transaction.Transactional

interface HistoricalProductRepository: CrudRepository<HistoricalProduct, Long>

@Repository("historicalProductRepository")
@Transactional
open class SmythsHistoricalProductRepository : HistoricalProductRepository{

    override fun createTable() = SchemaUtils.create(HistoricalProducts)

    override fun create(product: HistoricalProduct): HistoricalProduct {
        if(find(product.name).name.isNullOrBlank()){
            HistoricalProducts.insert(toRow(product))
        }
        return product
    }

    override fun update(product: HistoricalProduct): HistoricalProduct {
        val query : Int = HistoricalProducts.update({HistoricalProducts.smythsCode.eq(product.smythsCode)} , body = {
            it[smythsCode] = product.smythsCode
            it[smythsStockCheckCode] = product.smythsStockCheckCode
            it[name] = product.name
            it[price] = product.price
            it[categoryId] = product.categoryId
           // it[listId] = product.listTypeId
            it[brandId] = product.brandId
            it[url] = product.url
        })
        val result = query.run {}
        return product
    }

    override fun findAll(): MutableSet<HistoricalProduct> {
        return HistoricalProducts.selectAll().map { fromRow(it) }.toMutableSet()
    }

    fun findAllInRange(low : Int, high : Int, productName : String): MutableList<HistoricalProduct> {
        val allProducts : MutableSet<HistoricalProduct> = searchForProducts(productName)
        var filteredList : MutableList<HistoricalProduct> =  mutableListOf()
        val size = filteredList.size
        if(size in high..low){
            filteredList = allProducts.toMutableList().subList(low, high)
        } else {
            filteredList = allProducts.toMutableList()
        }
        return filteredList
    }

    fun searchForProducts(searchQuery : String) : MutableSet<HistoricalProduct>{
        var selectedProducts : MutableSet<HistoricalProduct> = mutableSetOf()
        //if(searchQuery.isNullOrBlank()){
            selectedProducts = findAll().filter { it.name.toLowerCase().contains(searchQuery.toLowerCase()) }.toMutableSet()
       // } else {
       //     HistoricalProducts.select{ HistoricalProducts.productId eq searchQuery }.forEach {
       //         selectedProducts.add(fromRow(it))
      //      }
      //  }
        return selectedProducts
    }

    override fun find(name : String): HistoricalProduct {
        var product : HistoricalProduct = HistoricalProduct("", "",  0, 0, "", BigDecimal.ZERO , "","","", DateTime.now())
        HistoricalProducts.select{ HistoricalProducts.name.eq(name)}.forEach {
            product = fromRow(it)
        }
        return product
    }

    override fun deleteAll(): Int {
        return HistoricalProducts.deleteAll()
    }

    private fun toRow(product: HistoricalProduct): HistoricalProducts.(UpdateBuilder<*>) -> Unit = {
        it[id] = product.id
        it[productId] = product.productId
        it[smythsCode] = product.smythsCode
        it[smythsStockCheckCode] = product.smythsStockCheckCode
        it[name] = product.name
        it[price] = product.price
        it[categoryId] = product.categoryId
       // it[listId] = product.listTypeId
        it[brandId] = product.brandId
        it[url] = product.url
        it[dateCreated] = product.date
    }

    private fun fromRow(r: ResultRow) =
            HistoricalProduct( id = r[HistoricalProducts.id],
                     smythsCode = r[HistoricalProducts.smythsCode],
                     productId =  r[HistoricalProducts.productId],
                     smythsStockCheckCode = r[HistoricalProducts.smythsStockCheckCode],
                     name = r[HistoricalProducts.name],
                     price = r[HistoricalProducts.price],
                     categoryId = r[HistoricalProducts.categoryId],
                     brandId =r[HistoricalProducts.brandId],
                     url = r[HistoricalProducts.url],
                     date = r[HistoricalProducts.dateCreated])
}
