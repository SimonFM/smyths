package com.nintendont.smyths.data.schema.responses

import com.nintendont.smyths.data.schema.Link

data class GenerateLinksResponse(var message : String,
                                 var error : String,
                                 var links : Set<Link>)