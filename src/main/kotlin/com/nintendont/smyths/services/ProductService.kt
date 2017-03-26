package com.nintendont.smyths.services

import com.nintendont.smyths.data.Brand
import com.nintendont.smyths.data.ProductList
import com.nintendont.smyths.http.HttpHandler
import com.nintendont.smyths.repository.ProductRepository
import com.nintendont.smyths.repository.SmythsProductRepository
import com.nintendont.smyths.schema.Category
import com.nintendont.smyths.schema.Product
import com.nintendont.smyths.utils.Utils
import org.json.JSONObject
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.math.BigDecimal
import javax.transaction.Transactional

@Service
open class ProductService {

    private val httpHandler : HttpHandler = HttpHandler()
    @Autowired
    lateinit var productRepository : SmythsProductRepository

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
        val productLists = mutableSetOf<ProductList>()
        val brands = mutableSetOf<Brand>()

        for (elem in dataProducts) {
            val productData = elem.attr("data-event")
            val json = JSONObject(productData)
            var listing : String = ""
            var productName : String = ""
            var productPriceAsString : String = ""
            var productPriceAsBigDecimal : BigDecimal = BigDecimal(0)
            var productId : String = ""
            var productBrand : String = ""
            var categoryName : String = ""

            if(json.has("listing")){
                listing = json.get("listing") as String
            }
            if(json.has("name")){
                productName = json.get("name") as String
            }
            if(json.has("id")){
                productId = json.get("id") as String
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

            val product: Product = Product(name = productName, price = productPriceAsBigDecimal, id = productId.toLong())
            if(listing.isNotBlank()){
                val productList: ProductList = ProductList(name = listing)
              //  product.productList = productList
                productLists.add(productList)
            }
            if(productBrand.isNotBlank()){
                val brand: Brand = Brand(name = productBrand)
               // product.brand = brand
                brands.add(brand)
            }
            if(categoryName.isNotBlank()){
                val category: Category = Category(name = categoryName)
               // product.category = category
               categories.add(category)
            }
            productRepository.create(product)
            products.add(product)
        }
        return products
    }
}