package com.nintendont.smyths.data.schema.responses

import com.nintendont.smyths.data.schema.Product

data class SearchQueryResponse(var message : String,
                               var error : String,
                               var products : MutableList<Product>,
                               var status : MutableList<CheckProductResponse>)