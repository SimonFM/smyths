package com.nintendont.smyths.web.services

import com.nintendont.smyths.utils.http.HttpHandler
import com.nintendont.smyths.data.repository.*
import com.nintendont.smyths.data.schema.Brand
import com.nintendont.smyths.data.schema.Category
import com.nintendont.smyths.data.schema.ListType
import com.nintendont.smyths.data.schema.Product
import com.nintendont.smyths.utils.Utils
import org.json.JSONObject
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
open class CatalogueService {

    private val httpHandler : HttpHandler = HttpHandler()
    @Autowired
    lateinit var productRepository : SmythsProductRepository
    @Autowired
    lateinit var brandRepository : SmythsBrandRepository
    @Autowired
    lateinit var listTypeRepository : SmythsListTypeRepository
    @Autowired
    lateinit var categoryRepository : SmythsCategoryRepository
    @Autowired
    lateinit var cryptoService : CryptoService

    fun checkProductAvailability(productId:String, storeId: String){
        val product : Pair<String, Any> = Pair("productId", productId)
        val store : Pair<String, Any> = Pair("storeId", storeId)
        val params : MutableList<Pair<String,Any>> = mutableListOf()
        params.add(product)
        params.add(store)
        httpHandler.post("http://www.smythstoys.com/ie/en-ie/product/productinstorestock/", params)
    }

    fun getAllProducts() : MutableSet<Product> {
        val params: MutableList<Pair<String, Any>> = mutableListOf()
        val response: Document = httpHandler.get("http://www.smythstoys.com/ie/en-ie/video-games-tablets/c-1000/nintendo-switch/", params)
        val data = response.getElementsByAttribute("data-egatype")[0]
        val dataProducts = data.children()

        val products = mutableSetOf<Product>()
        val categories = mutableSetOf<Category>()
        val lists = mutableSetOf<ListType>()
        val brands = mutableSetOf<Brand>()

        for (elem in dataProducts) {
            val productData = elem.attr("data-event")
            val json = JSONObject(productData)
            val listing : String = if (json.has("list")) json.get("list") as String else ""
            val productName : String = if (json.has("name")) json.get("name") as String else ""
            val productPriceAsString : String = if (json.has("price")) json.get("price") as String else "0"
            val productPriceAsBigDecimal : BigDecimal = Utils.stringToBigDecimal(productPriceAsString)
            val smythsProductId : String =if (json.has("id")) json.get("id") as String else ""
            val productBrand : String = if (json.has("brand")) json.get("brand") as String else ""
            val categoryName : String = if (json.has("category")) json.get("category") as String else ""

            val categoryId : String = makeUUID()
            val productId : String = makeUUID()
            val brandId : String = makeUUID()
            val listingId : String = makeUUID()

            if(listing.isNotBlank()){
                val existingType : ListType = listTypeRepository.find(listing)
                if (existingType.name.isNotBlank()){
                    lists.add(existingType)
                } else{
                    val list: ListType = ListType(name = listing, id = listingId)
                    lists.add(list)
                    listTypeRepository.create(list)
                }
            }
            if(productBrand.isNotBlank()){
                val existingBrand : Brand = brandRepository.find(productBrand)
                if (existingBrand.name.isNotBlank()){
                    brands.add(existingBrand)
                } else{
                    val brand: Brand = Brand(name = productBrand, id = brandId)
                    brands.add(brand)
                    brandRepository.create(brand)
                }
            }
            if(categoryName.isNotBlank()){
                val existingCategory : Category = categoryRepository.find(categoryName)
                if (existingCategory.name.isNotBlank()){
                    categories.add(existingCategory)
                } else {
                    val category: Category = Category(name = categoryName, id = categoryId)
                    categories.add(category)
                    categoryRepository.create(category)
                }
            }

            val existingProduct : Product = productRepository.find(smythsProductId)
            if(existingProduct.id.isNotBlank()){
                products.add(existingProduct)
            }  else {
                val product: Product = makeProduct(brandId, categoryId, listingId, productId, productName, productPriceAsBigDecimal, smythsProductId)
                productRepository.create(product)
                products.add(product)
            }
        }
        return products
    }

    private fun makeProduct(brandId: String, categoryId: String, listingId: String, productId: String,
                            productName: String, productPriceAsBigDecimal: BigDecimal, smythsProductId: String): Product {
        return Product(id = productId,
                name = productName,
                price = productPriceAsBigDecimal,
                smythsId = smythsProductId.toLong(),
                categoryId = categoryId,
                listTypeId = listingId,
                brandId = brandId)
    }

    private fun makeUUID() : String{
       return UUID.randomUUID().toString()
    }
}