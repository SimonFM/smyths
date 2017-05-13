package com.nintendont.smyths.data.schema

import java.math.BigDecimal

data class Product (var id: String,
                    var smythsCode: Long,
                    var smythsStockCheckCode: Long? = null,
                    var name: String,
                    var price: BigDecimal,
                    var categoryId: String? = null,
                    var brandId: String? = null,
                    var url : String ){

    fun isEqual(newProductToCompare : Product) : Boolean {
        var isEqual = false

        return isEqual
    }

    fun nameIsEqual(newProductToCompare : Product) : Boolean {
        var isEqual = false

        if(!newProductToCompare.name.isNullOrBlank() && name.isNullOrBlank()){
            isEqual = false
        }
        if(newProductToCompare.name.isNullOrBlank() && !name.isNullOrBlank()){
            isEqual = true
        }
        if(!newProductToCompare.name.isNullOrBlank() && !name.isNullOrBlank()){
            isEqual = newProductToCompare.name == name
        }
        return isEqual
    }

    fun brandIdIsEqual(newProductToCompare : Product) : Boolean {
        var isEqual = false

        if(!newProductToCompare.brandId.isNullOrBlank() && brandId.isNullOrBlank()){
            isEqual = false
        }
        if(newProductToCompare.brandId.isNullOrBlank() && !brandId.isNullOrBlank()){
            isEqual = true
        }
        if(!newProductToCompare.brandId.isNullOrBlank() && !brandId.isNullOrBlank()){
            isEqual = newProductToCompare.brandId == brandId
        }
        return isEqual
    }

    fun categoryIdIsEqual(newProductToCompare : Product) : Boolean {
        var isEqual = false

        if(!newProductToCompare.categoryId.isNullOrBlank() && categoryId.isNullOrBlank()){
            isEqual = false
        }
        if(newProductToCompare.categoryId.isNullOrBlank() && !categoryId.isNullOrBlank()){
            isEqual = true
        }
        if(!newProductToCompare.categoryId.isNullOrBlank() && !categoryId.isNullOrBlank()){
            isEqual = newProductToCompare.categoryId == categoryId
        }
        return isEqual
    }

    fun urlIsEqual(newProductToCompare : Product) : Boolean {
        var isEqual = false

        if(!newProductToCompare.url.isNullOrBlank() && url.isNullOrBlank()){
            isEqual = false
        }
        if(newProductToCompare.url.isNullOrBlank() && !url.isNullOrBlank()){
            isEqual = true
        }
        if(!newProductToCompare.url.isNullOrBlank() && !url.isNullOrBlank()){
            isEqual = newProductToCompare.url == url
        }
        return isEqual
    }

    fun smythsCodeIsEqual(newProductToCompare : Product) : Boolean {
        return newProductToCompare.smythsCode == smythsCode
    }

    fun smythsStockCheckCodeIsEqual(newProductToCompare : Product) : Boolean {
        return newProductToCompare.smythsStockCheckCode == smythsStockCheckCode
    }

    fun priceIsEqual(newProductToCompare : Product) : Boolean {
        return newProductToCompare.price == price
    }
}
