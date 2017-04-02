package com.nintendont.smyths.app

import com.nintendont.smyths.data.repository.*
import com.nintendont.smyths.web.services.CatalogueService
import com.nintendont.smyths.web.services.CryptoService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AppConfig {

    /**
     * Service Beans
     */
    @Bean
    open fun productService() = CatalogueService()

    @Bean
    open fun cryptoService() = CryptoService()

    /**
     * Repository Beans
     */
    @Bean
    open fun productRepository() = SmythsProductRepository()

    @Bean
    open fun listTypeRepository() = SmythsListTypeRepository()

    @Bean
    open fun brandRepository() = SmythsBrandRepository()

    @Bean
    open fun categoryRepository() = SmythsCategoryRepository()

    @Bean
    open fun linkRepository() = SmythsLinkRepository()
}