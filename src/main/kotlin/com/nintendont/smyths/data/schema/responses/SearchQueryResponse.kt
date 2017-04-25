package com.nintendont.smyths.data.schema.responses

data class SearchQueryResponse(var message : String,
                               var error : String,
                               var products : String,
                               var status : MutableList<String>)