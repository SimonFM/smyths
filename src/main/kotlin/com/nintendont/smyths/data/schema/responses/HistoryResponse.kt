package com.nintendont.smyths.data.schema.responses

import com.nintendont.smyths.data.schema.HistoricalProduct

/**
 * Created by simon on 07/05/2017.
 */
data class HistoryResponse(var message : String,
                           var histories : MutableList<HistoricalProduct>)