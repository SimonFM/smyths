package com.nintendont.smyths.app

import com.nintendont.smyths.data.repository.SmythsBrandRepository
import com.nintendont.smyths.data.repository.SmythsCategoryRepository
import com.nintendont.smyths.data.repository.SmythsListTypeRepository
import com.nintendont.smyths.data.repository.SmythsProductRepository
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
}