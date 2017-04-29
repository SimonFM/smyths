package com.nintendont.smyths.web.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.nintendont.smyths.data.repository.SmythsLinkRepository
import com.nintendont.smyths.data.schema.*
import com.nintendont.smyths.data.schema.responses.GenerateLinksResponse
import com.nintendont.smyths.utils.Constants
import com.nintendont.smyths.utils.Constants.SMYTHS_BASE_URL
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.*

@Service open class LinkService {

    enum class categoryType {
        NAV_BAR, SUB_CATEGORY, CATEGORY
    }

    @Autowired lateinit var httpService : HttpService
    @Autowired lateinit var linkRepository : SmythsLinkRepository

    // Invalid Urls causing endless loops
    private val sonyConsoleProblemUrl : String = "http://www.smythstoys.com/ie/en-ie/video-games-tablets/c-747/playstation-4/"
    private val xboxOneProblemUrl : String = "http://www.smythstoys.com/ie/en-ie/video-games-tablets/c-751/xbox-one/"

    // Corrected values
    private val validSonyConsoleUrl : String = "http://www.smythstoys.com/ie/en-ie/video-games-tablets/c-747/playstation-4-consoles/"
    private val validXboxConsoleUrl : String = "http://www.smythstoys.com/ie/en-ie/video-games-tablets/c-751/xbox-one-consoles/"

    /**
     * Generates the Links from smyths.ie and stores them in the Links Table.
     * @return Set of Links
     */
    fun generateLinks() : GenerateLinksResponse {
        println("Generating Links....")
        val params: MutableList<Pair<String, Any>> = mutableListOf()
        val response: Document = httpService.get(Constants.SMYTHS_BASE_URL, params)
        val navBar = response.select("ul.navbar-nav")[2]
        val topCategories = navBar.select("li.col-md-15")
        val subCategories = navBar.select("li.dropdown-header")
        val categories1 = navBar.select("li.col-sm-6")
        val categories2 = navBar.select("li.col-sm-3")
        val topCategoriesLinks : Set<Link> = getCategoryLinks(topCategories, categoryType.NAV_BAR)
        val subCategoriesLinks : Set<Link> = getCategoryLinks(subCategories, categoryType.SUB_CATEGORY)
        val categoriesLinks1 : Set<Link> = getCategoryLinks(categories1, categoryType.CATEGORY)
        val categoriesLinks2 : Set<Link> = getCategoryLinks(categories2, categoryType.CATEGORY)
        println("*-*-*- Finished generating Links. -*-*-*")
        var links = categoriesLinks1.toMutableSet().union (categoriesLinks2.toMutableSet())
        links =  links.union(topCategoriesLinks.toMutableSet().union(subCategoriesLinks.toMutableSet()))
        saveLinks(links)
        val responseToSend : GenerateLinksResponse = GenerateLinksResponse(links = links,
                                                                           message = "Successfully generated Links for url",
                                                                           error = "")
        return responseToSend
    }

    private fun saveLinks(linksToSave : Set<Link>) : Boolean {
        println(".....Saving Links.....")
        linksToSave.forEach { link -> linkRepository.create(link = link) }
        println(".....Links Saved.....")
        return true
    }

    /**
     *
     */
    private fun getCategoryLinks(categories: Elements, category: categoryType) : Set<Link> {
        var links : Set<Link> = mutableSetOf<Link>()
        categories.forEach { li ->
            when(category){
                categoryType.CATEGORY -> {
                    val a = li.select("[href]")
                    val categoryLinks = makeCategoryLinks(a)
                    links = links.union(categoryLinks)
                }
                categoryType.SUB_CATEGORY -> {
                    val a = li.select("[href]")
                    val categoryLinks = makeCategoryLinks(a)
                    links = links.union(categoryLinks)
                    val specialCategories = li.parent().attributes().get("href")
                    val specialCategoryLinks: MutableSet<Link> = mutableSetOf<Link>()
                    if (specialCategories.isNotEmpty()) {
                        val newLink: Link = Link(name = li.text(), url = SMYTHS_BASE_URL + specialCategories, id = makeUUID())
                        specialCategoryLinks.add(newLink)
                    }
                    links = links.union(specialCategoryLinks)
                }
                categoryType.NAV_BAR -> {
                    val a = li.select("a.dropdown-toggle").select("[href]")
                    val navBarCategories = makeCategoryLinks(a)
                    links = links.union(navBarCategories)
                }

            }
        }
        return links
    }

    /***
     * Makes a set of links to add
     */
    private fun makeCategoryLinks( a : Elements) : MutableSet<Link> {
        val links : MutableSet<Link> = mutableSetOf<Link>()
        a.forEach {
            val linkName = it.text()
            if(linkName.isNotBlank()){
                val newLink : Link = Link(name = linkName, url = SMYTHS_BASE_URL + a.attr("href"), id = makeUUID())
                links.add(newLink)
            }
        }
        return links
    }
    /**
     * Makes a unique identifier
     * @return Id as a string
     */
    private fun makeUUID() : String{
       return UUID.randomUUID().toString()
    }
}