package com.nintendont.smyths.app

import com.nintendont.smyths.repository.SmythsProductRepository
import com.nintendont.smyths.services.ProductService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AppConfig {
    @Bean
    open fun productService() = ProductService()

    @Bean
    open fun productRepository() = SmythsProductRepository()
}