package com.nintendont.smyths.data.repository

import com.nintendont.smyths.data.Brands
import com.nintendont.smyths.data.interfaces.CrudRepository
import com.nintendont.smyths.data.schema.Brand
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

interface BrandRepository : CrudRepository<Brand, Long>

@Repository("brandRepository")
@Transactional
open class SmythsBrandRepository : BrandRepository{

    override fun createTable() = SchemaUtils.create(Brands)

    override fun create(brand: Brand): Brand {
        Brands.insert(toRow(brand))
        return brand
    }

    override fun findAll(): Iterable<Brand> {
        return Brands.selectAll().map { fromRow(it) }
    }

    override fun deleteAll(): Int {
        return Brands.deleteAll()
    }

    private fun toRow(brand: Brand): Brands.(UpdateBuilder<*>) -> Unit = {
        it[name] = brand.name
        it[id] = brand.id
    }

    private fun fromRow(r: ResultRow) = Brand(r[Brands.name], r[Brands.id])
}