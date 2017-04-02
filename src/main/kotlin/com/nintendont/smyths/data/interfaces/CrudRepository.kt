package com.nintendont.smyths.data.interfaces

/**
 * Created by simon on 23/03/2017.
 */
interface CrudRepository<T, K> {
    fun createTable()
    fun create(item: T): T
    fun findAll(): Iterable<T>
    fun find(name : String): T
    fun deleteAll(): Int
}