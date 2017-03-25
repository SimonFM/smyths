package com.nintendont.smyths.repository

/**
 * Created by simon on 23/03/2017.
 */
interface CrudRepository<T, K> {
    fun createTable()
    fun create(product: T): T
    fun findAll(): Iterable<T>
    fun deleteAll(): Int
}