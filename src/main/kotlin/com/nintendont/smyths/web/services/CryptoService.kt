package com.nintendont.smyths.web.services

import org.springframework.stereotype.Service
import java.security.MessageDigest



/**
 * Created by simon on 30/03/2017.
 */
@Service("cryptoService")
open class CryptoService{

    fun generateIdFrom(text : String) : String{
        val md = MessageDigest.getInstance("SHA-256")
        md.update(text.toByteArray())
        val digest = md.digest()

        return String(digest)
    }
}