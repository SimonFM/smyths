package com.nintendont.smyths.app

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.nintendont.smyths.data.repository.*
import org.jetbrains.exposed.spring.SpringTransactionManager
import org.postgis.geojson.PostGISModule
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@SpringBootApplication
@ComponentScan("com.nintendont.smyths.*")
//@EnableTransactionManagement
@EnableScheduling

open class SmythsApp {

    @Bean open fun objectMapper(): ObjectMapper {
        val mapper: ObjectMapper = Jackson2ObjectMapperBuilder().modulesToInstall(PostGISModule()).build()
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        return mapper
    }

    @Bean open fun transactionManager(dataSource: DataSource) = SpringTransactionManager(dataSource)

    @Bean open fun persistenceExceptionTranslationPostProcessor() = PersistenceExceptionTranslationPostProcessor()

    @Bean open fun init(productRepository: SmythsProductRepository, listTypeRepository: SmythsListTypeRepository,
                        categoryRepository: SmythsCategoryRepository, brandRepository: SmythsBrandRepository,
                        linkRepository: SmythsLinkRepository, locationRepository: SmythsLocationRepository) = CommandLineRunner {

        listTypeRepository.createTable()
        categoryRepository.createTable()
        brandRepository.createTable()
        linkRepository.createTable()
        locationRepository.createTable()
        productRepository.createTable()
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            SpringApplication.run(SmythsApp::class.java, *args)
        }
    }
}