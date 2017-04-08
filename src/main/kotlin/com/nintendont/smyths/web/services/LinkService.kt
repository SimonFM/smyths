package com.nintendont.smyths.web.services

import com.google.gson.Gson
import com.nintendont.smyths.utils.http.HttpHandler
import com.nintendont.smyths.data.repository.*
import com.nintendont.smyths.data.schema.*
import com.nintendont.smyths.utils.Constants
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
open class LinkService {

    private val httpHandler : HttpHandler = HttpHandler()
    @Autowired
    lateinit var linkRepository : SmythsLinkRepository

    fun generateLinks() : MutableSet<Link>{
        println("Generating Links....")
        val links = mutableSetOf<Link>()
        val params: MutableList<Pair<String, Any>> = mutableListOf()
        val response: Document = httpHandler.get(Constants.SMYTHS_BASE_URL, params)
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
                    val link: Link = Link(url = url, id = makeUUID(), links = subLinks)
                    linkRepository.create(link)
                    links.add((link))
                    println("New Link Saved: $link")
                }
            }
        }
        return links
    }

    private fun generateSubLinks(url : String, startingPage : Int) : String {
        println("Generating sub links for url: $url....")

        var page : Int = startingPage
        var hasMoreLinksToGet : Boolean = true
        val subLinks  = mutableSetOf<Link>()
        while(hasMoreLinksToGet){
            val urlToGet = "$url?${Constants.VIEW_ALL}&${Constants.PAGE}=$page"
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

    private fun makeUUID() : String{
       return UUID.randomUUID().toString()
    }
}