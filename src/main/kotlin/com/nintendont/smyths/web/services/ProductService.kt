package com.nintendont.smyths.web.services

import com.google.gson.Gson
import com.nintendont.smyths.utils.http.HttpHandler
import com.nintendont.smyths.data.repository.*
import com.nintendont.smyths.data.schema.*
import com.nintendont.smyths.utils.Constants
import com.nintendont.smyths.utils.Utils
import org.json.JSONObject
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
open class ProductService {

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
    lateinit var linkRepository : SmythsLinkRepository

    fun checkProductAvailability(productId:String, storeId: String){
        val product : Pair<String, Any> = Pair("productId", productId)
        val store : Pair<String, Any> = Pair("storeId", storeId)
        val params : MutableList<Pair<String,Any>> = mutableListOf()
        params.add(product)
        params.add(store)
        httpHandler.post("${Constants.SMYTHS_BASE_URL}/product/productinstorestock/", params)
    }

    fun generateLinks() : MutableSet<Link>{
        println("Generating Links....")
        val links = mutableSetOf<Link>()
        val params: MutableList<Pair<String, Any>> = mutableListOf()
        val response: Document = httpHandler.get("${Constants.SMYTHS_BASE_URL}", params)
        val menuItems = response.select("li.menu-item")
        val popupItems = menuItems.select("[aria-haspopup=\"false\"]")
        val listOfLinks = popupItems.select("[href]")
        for (e in listOfLinks){
            val url : String = e.attr("abs:href")
            if(url.isNotBlank()){
                val existingLink : Link = linkRepository.find(url)
                if (existingLink.url.isNotBlank()){
                    links.add((existingLink))
                    println("Found existing link for url: $existingLink")
                } else{
                    val subLinks = generateSubLinks(url, 1)
                    val link: Link = Link(url = url, id = makeUUID(), links = "$subLinks")
                    linkRepository.create(link)
                    links.add((link))
                    println("New Link Saved: $link")
                }
            }
        }
        return links
    }

    fun getAllProducts() : MutableSet<Product> {
        println("Generating Products....")
        val params: MutableList<Pair<String, Any>> = mutableListOf()
        val links = linkRepository.findAll()
        val products = mutableSetOf<Product>()
        val categories = mutableSetOf<Category>()
        val lists = mutableSetOf<ListType>()
        val brands = mutableSetOf<Brand>()

        for (link in links){
            println("Fetching products for url: ${link.url}")
            val response: Document = httpHandler.get("${link.url}?${Constants.VIEW_ALL}", params)
            if (response.getElementsByAttribute(Constants.DATA_TYPE).size > 0){
                val data = response.getElementsByAttribute(Constants.DATA_TYPE)[0]
                val dataProducts = data.children()
                for (elem in dataProducts) {
                    val productData = elem.attr(Constants.DATA_EVENT)
                    if(productData != null){
                        try {
                            val json = JSONObject(productData)
                            val listing : String = if (json.has(Constants.LIST)) json.get(Constants.LIST) as String else ""
                            val productName : String = if (json.has(Constants.PRODUCT_NAME)) json.get(Constants.PRODUCT_NAME) as String else ""
                            val productPriceAsString : String = if (json.has(Constants.PRICE)) json.get(Constants.PRICE) as String else "0"
                            val productPriceAsBigDecimal : BigDecimal = Utils.stringToBigDecimal(productPriceAsString)
                            val smythsProductId : String =if (json.has(Constants.ID)) json.get(Constants.ID) as String else ""
                            val productBrand : String = if (json.has(Constants.BRAND)) json.get(Constants.BRAND) as String else ""
                            val categoryName : String = if (json.has(Constants.CATEGORY_NAME)) json.get(Constants.CATEGORY_NAME) as String else ""

                            var listingId : String = makeUUID()
                            if(listing.isNotBlank()){
                                val existingType : ListType = listTypeRepository.find(listing)
                                if (existingType.name.isNotBlank()){
                                    lists.add(existingType)
                                    listingId = existingType.id
                                } else{
                                    val list: ListType = ListType(name = listing, id = listingId)
                                    lists.add(list)
                                    listTypeRepository.create(list)
                                    println("- Made new listing: $list....")
                                }
                            }

                            var brandId : String = makeUUID()
                            if(productBrand.isNotBlank()){
                                val existingBrand : Brand = brandRepository.find(productBrand)
                                if (existingBrand.name.isNotBlank()){
                                    brands.add(existingBrand)
                                    brandId = existingBrand.id
                                } else{
                                    val brand: Brand = Brand(name = productBrand, id = brandId)
                                    brands.add(brand)
                                    brandRepository.create(brand)
                                    println("- Made new brand: $brand....")
                                }
                            }

                            var categoryId : String = makeUUID()
                            if(categoryName.isNotBlank()){
                                val existingCategory : Category = categoryRepository.find(categoryName)
                                if (existingCategory.name.isNotBlank()){
                                    categories.add(existingCategory)
                                    categoryId = existingCategory.id
                                } else {
                                    val category: Category = Category(name = categoryName, id = categoryId)
                                    categories.add(category)
                                    categoryRepository.create(category)
                                    println("- Made new category: $category....")
                                }
                            }

                            var productId : String = makeUUID()
                            val existingProduct : Product = productRepository.find(smythsProductId)
                            if(existingProduct.id.isNotBlank()){
                                products.add(existingProduct)
                            }  else {
                                val product: Product = makeProduct(brandId, categoryId, listingId, productId, productName, productPriceAsBigDecimal, smythsProductId)
                                productRepository.create(product)
                                println("- Made new product: $product....")
                                products.add(product)
                            }
                        } catch (e: Exception) {
                            println(e.printStackTrace())
                        }
                    }
                }
            }
        }
        return products
    }

    private fun generateSubLinks(url : String, startingPage : Int) : String {
        println("Generating sub links for url: $url....")

        var page : Int = startingPage
        var hasMoreLinksToGet : Boolean = true
        val subLinks  = mutableSetOf<Link>()
        while(hasMoreLinksToGet){
            var urlToGet = "$url?${Constants.VIEW_ALL}&${Constants.PAGE}=$page"
            val params : MutableList<Pair<String,Any>> = mutableListOf()

            val response : Document = httpHandler.get(urlToGet, params)

            if (response.getElementsByAttribute(Constants.DATA_TYPE).isNotEmpty()){
                val data = response.getElementsByAttribute(Constants.DATA_TYPE)[0]
                val dataProducts = data.children()
                if(dataProducts.isNotEmpty()){
                    val subLink : Link = Link(url = url, id = makeUUID(), links = "")
                    subLinks.add(subLink)
                    page++
                    println("Adding New page for $url, pageCount : $page")
                } else {
                    hasMoreLinksToGet = false
                }
                println("$url: page count $page & has more links? $hasMoreLinksToGet")
            } else {
                hasMoreLinksToGet = false
            }
        }
        val newJson = Gson().toJson(subLinks)
        return newJson.toString()
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