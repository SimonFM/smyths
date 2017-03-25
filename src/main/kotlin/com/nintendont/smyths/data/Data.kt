package com.nintendont.smyths.data

import javax.persistence.*


@Entity @Table(name = "brand")
data class Brand (var name: String = "",
                  @Id @GeneratedValue(strategy = GenerationType.AUTO) var id : Long = 0)

@Entity @Table(name = "productList")
data class ProductList (var name: String,
                        @Id @GeneratedValue(strategy = GenerationType.AUTO) var id : Long = 0)
//@Entity
//data class Product (var id : String, var name: String, var price: Float, var category: Category, var list : ProductList)

//@Entity @Table(name = "product")
//data class Product (var name: String = "",
//                    var price: Float = 0.0f,
//                    var category: Category = Category("", 0),
//                    var productList: ProductList = ProductList("", 0),
//                    var brand: Brand = Brand(""),
//                    @Id @GeneratedValue(strategy = GenerationType.AUTO) var id : Long = 0)




//@Repository
//interface ProductListRepository : PagingAndSortingRepository<ProductList, Long>{
//    fun findByName(name: String): List<ProductList>
//}
//
//@Repository
//interface CategoryRepository : PagingAndSortingRepository<Category, Long>{
//    fun findByName(name: String): List<Category>
//}
//
//@Repository
//interface BrandRepository : PagingAndSortingRepository<Brand, Long>{
//    fun findByName(name: String): List<Brand>
//}