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
            var listing : String = ""
            var productName : String = ""
            var productPriceAsString : String = ""
            var productPriceAsBigDecimal : BigDecimal = BigDecimal(0)
            var smythsProductId : String = ""
            var productBrand : String = ""
            var categoryName : String = ""

            if(json.has("listing")){
                listing = json.get("listing") as String
            }
            if(json.has("name")){
                productName = json.get("name") as String
            }
            if(json.has("id")){
                smythsProductId = json.get("id") as String
            }
            if(json.has("price")){
                productPriceAsString = json.get("price") as String
                productPriceAsBigDecimal = Utils.stringToBigDecimal(productPriceAsString)
            }
            if(json.has("brand")){
                productBrand = json.get("brand") as String
            }
            if(json.has("category")){
                categoryName = json.get("category") as String
            }
            val categoryId : String = UUID.randomUUID().toString()
            val productId : String = UUID.randomUUID().toString()
            val brandId : String = UUID.randomUUID().toString()
            val listingId : String = UUID.randomUUID().toString()

            if(listing.isNotBlank()){

                val list: ListType = ListType(name = listing, id = listingId)
              //  product.productList = productList
                lists.add(list)
                listTypeRepository.create(list)
            }
            if(productBrand.isNotBlank()){

                val brand: Brand = Brand(name = productBrand, id = brandId)
               // product.brand = brand
                brands.add(brand)
                brandRepository.create(brand)
            }
            if(categoryName.isNotBlank()){

                val category: Category = Category(name = categoryName, id = categoryId )
               // product.category = category
                categories.add(category)
                categoryRepository.create(category)
            }
            val product: Product = Product(id = productId,
                                           name = productName,
                                           price = productPriceAsBigDecimal,
                                           smythsId = smythsProductId.toLong(),
                                           categoryId = categoryId,
                                           listTypeId = listingId,
                                           brandId = brandId)

            productRepository.create(product)
            products.add(product)
        }
        return products
    }
}