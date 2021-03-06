package com.nintendont.smyths.web.services

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.nintendont.smyths.utils.exceptions.SmythsException
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Whitelist
import org.springframework.stereotype.Service

@Service open class HttpService {
    /**
     * @author Simon
     * Sends a POST
     * @param url - The request sent
     * @param params - The response we got back.
     * @return A map of the response.
     */
    fun post ( url : String, params : List<Pair<String, Any?>>) : Document {
        var headers : MutableMap<String, String> = mutableMapOf()
        headers.put("Content-Type", "application/x-www-form-urlencoded")
        headers.put("Content-Length", "2")

        val (request, response, result) = url.httpPost(params)
                .header(headers)
                .responseString()

        val responseAsDocument : Document = formatResponseAsHTML(request, response, result)
        return responseAsDocument
    }

    /**
     * @author Simon
     * Sends a GET
     * @param url - The request sent
     * @param params - The response we got back.
     * @return A map of the response.
     */
    fun get (url : String, params: List<Pair<String, Any?>>): Document {
        val (request, response, result) = url.httpGet().responseString() // result is Result<String, FuelError>
        val responseAsDocument: Document = formatResponseAsHTML(request, response, result)
        return responseAsDocument
    }

    fun getJson (url : String, params: List<Pair<String, Any?>>): JSONObject {
        val (request, response, result) = url.httpGet(params).responseString() // result is Result<String, FuelError>
        val json : JSONObject = formatResponseAsJson(request, response, result)
        return json
    }

    private fun formatResponseAsHTML(request : Request, response: Response, result: Result<String, FuelError>) : Document {
        var document : Document = Document("")
        val (data, error) = result
        val success : Boolean = response.httpStatusCode == 200 && data != null && data.length >= 0

        if (success) {
            //printSuccess(request, response, result)
            document = prettyPrintHtml(data)
        } else {
            val errorMessage : String = "HTML -> REQUEST: $request with error: $error"
            printError(errorMessage)
            throw SmythsException(errorMessage, Throwable(errorMessage))
        }
        return document
    }

    private fun formatResponseAsJson(request : Request, response: Response, result: Result<String, FuelError>) : JSONObject {
        val (data, error) = result
        val success : Boolean = response.httpStatusCode == 200 && data != null && data.length >= 0
        val json : JSONObject = if(data != null) JSONObject(data) else JSONObject()

        if (success) {
            //printSuccess(request, response, result)
            println(json.toString())
        } else {
            printError("JSON -> REQUEST: $request with error: $error")
        }
        return json
    }


    /**
     * @author Simon
     * Prints the successful response from a request.
     * @param request - The request sent
     * @param response - The response we got back.
     * @param result - The result of this response.
     */
    private fun printSuccess(request: Request, response: Response, result: Result<String, FuelError>){
        println("REQUEST - $request")
        println("RESPONSE - $response")
        println("RESULT - $result")
    }

    /**
     * @author Simon
     * Prints the error from a request.
     * @param error - The error response string.
     */
    private fun printError(error :String){
        println("ERROR - $error")
    }

    /**
     * @author Simon
     * Formats and prints the html response in a pretty way ;)
     * @param data - The string to format.
     */
    private fun prettyPrintHtml(data : String?) : Document {
        val doc : Document = Jsoup.parse(data)
        doc.outputSettings().prettyPrint(false)
        val settings : Document.OutputSettings = Document.OutputSettings()
        settings.prettyPrint(false)
        val clean = Jsoup.clean(data, "", Whitelist.relaxed(), settings)
        //println(clean)
        return doc
    }
}