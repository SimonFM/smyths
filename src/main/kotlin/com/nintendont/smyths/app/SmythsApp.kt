package com.nintendont.smyths.app

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.nintendont.smyths.data.repository.*
import com.nintendont.smyths.web.services.ProductService
import org.jetbrains.exposed.spring.SpringTransactionManager
import org.postgis.geojson.PostGISModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@SpringBootApplication
@ComponentScan("com.nintendont.smyths")
open class SmythsApp {
    @Bean
    open fun objectMapper(): ObjectMapper {
        val mapper: ObjectMapper = Jackson2ObjectMapperBuilder().modulesToInstall(PostGISModule()).build()
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        return mapper
    }

    @Bean
    open fun transactionManager(dataSource: DataSource) = SpringTransactionManager(dataSource)

    @Bean // PersistenceExceptionTranslationPostProcessor with proxyTargetClass=false, see https://github.com/spring-projects/spring-boot/issues/1844
    open fun persistenceExceptionTranslationPostProcessor() = PersistenceExceptionTranslationPostProcessor()

    @Bean
    open fun init(productRepository: SmythsProductRepository, listTypeRepository: SmythsListTypeRepository,
                  categoryRepository: SmythsCategoryRepository, brandRepository: SmythsBrandRepository,
                  linkRepository: SmythsLinkRepository, locationRepository: SmythsLocationRepository) = CommandLineRunner {
        productRepository.createTable()
        listTypeRepository.createTable()
        categoryRepository.createTable()
        brandRepository.createTable()
        linkRepository.createTable()
        locationRepository.createTable()
    }
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            SpringApplication.run(SmythsApp::class.java, *args)
        }
    }
}