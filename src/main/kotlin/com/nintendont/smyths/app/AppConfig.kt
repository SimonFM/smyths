package com.nintendont.smyths.app

import com.nintendont.smyths.data.repository.*
import com.nintendont.smyths.web.services.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AppConfig {

    /**
     * Service Beans
     */
    @Bean open fun productService() = ProductService()
    @Bean open fun cryptoService() = CryptoService()
    @Bean open fun linkService() = LinkService()
    @Bean open fun locationService() = LocationService()
    @Bean open fun jobService() = JobService()
    @Bean open fun httpService() = HttpService()

    /**
     * Repository Beans
     */
    @Bean open fun productRepository() = SmythsProductRepository()
    @Bean open fun listTypeRepository() = SmythsListTypeRepository()
    @Bean open fun brandRepository() = SmythsBrandRepository()
    @Bean open fun categoryRepository() = SmythsCategoryRepository()
    @Bean open fun linkRepository() = SmythsLinkRepository()
    @Bean open fun locationRepository() = SmythsLocationRepository()
    @Bean open fun openingsRepository() = SmythsOpeningsRepository()
}