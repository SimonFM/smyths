package com.nintendont.smyths.web.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.nintendont.smyths.data.repository.SmythsLinkRepository
import com.nintendont.smyths.data.schema.*
import com.nintendont.smyths.utils.Constants
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.*

@Service open class LinkService {

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
    fun generateLinks() : MutableSet<Link>{
        println("Generating Links....")
        val links = mutableSetOf<Link>()
        val params: MutableList<Pair<String, Any>> = mutableListOf()
        val response: Document = httpService.get(Constants.SMYTHS_BASE_URL, params)
        val menuItems = response.select("li.menu-item")
        val popupItems = menuItems.select("[aria-haspopup=\"false\"]")
        val listOfLinks = popupItems.select("[href]")
        for (e in listOfLinks){
            val url : String = e.attr("abs:href")
            if(url.isNotBlank()){
                var existingLink : Link = this.linkRepository.find(url)
                val subLinks : String = generateSubLinks(url, 1)
                if (existingLink.url.isNotBlank()){
                    if(subLinks.equals(existingLink.links)){
                        val tempLink = existingLink
                        tempLink.links = subLinks
                        existingLink = this.linkRepository.update(tempLink)
                    }
                    if(existingLink.id.isNotBlank()){
                        links.add((existingLink))
                        println("Found existing link for url: $existingLink")
                    }
                } else{
                    val link: Link = Link(url = url, id = makeUUID(), links = subLinks)
                    this.linkRepository.create(link)
                    links.add((link))
                    println("New Link Saved: $link")
                }
            }
        }
        println("*-*-*- Finished generating Links. -*-*-*")
        return links
    }

    /**
     * Generates the sub links for every link in the database.
     * @return Json string of the urls.
     */
    private fun generateSubLinks(url : String, startingPage : Int) : String {
        println("Generating sub links for url: $url....")

        var page : Int = startingPage
        var hasMoreLinksToGet : Boolean = true
        val subLinks  = mutableSetOf<Link>()
        while(hasMoreLinksToGet){
            val filteredUrl = filterUrl(url)
            val urlToGet = "$filteredUrl?${Constants.VIEW_ALL}&${Constants.PAGE}=$page"
            val params : MutableList<Pair<String,Any>> = mutableListOf()

            val response : Document = this.httpService.get(urlToGet, params)

            if (response.getElementsByAttribute(Constants.DATA_EGATYPE).isNotEmpty()){
                val data = response.getElementsByAttribute(Constants.DATA_EGATYPE)[0]
                val dataProducts = data.children()
                if(dataProducts.isNotEmpty()){
                    val subLink : Link = Link(url = urlToGet, id = makeUUID(), links = "")
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
        println("******* Finished generating sub links for url: $url *******")
        return if(isValidJson(newJson)) newJson.toString() else ""
    }

    private fun filterUrl(url : String) : String {
        var urlToReturn = url

        when (url) {
            this.sonyConsoleProblemUrl -> urlToReturn = this.validSonyConsoleUrl
            this.xboxOneProblemUrl -> urlToReturn = this.validXboxConsoleUrl
        }
        return urlToReturn
    }

    /**
     * Checks whether or not json is valid
     * @param json - the json to test
     * @return True if valid, False if not
     */
    private fun isValidJson(json : String): Boolean{
        try {
            val mapper = ObjectMapper()
            mapper.readTree(json)
            return true
        } catch (e: IOException) {
            return false
        }
    }
    /**
     * Makes a unique identifier
     * @return Id as a string
     */
    private fun makeUUID() : String{
       return UUID.randomUUID().toString()
    }
}