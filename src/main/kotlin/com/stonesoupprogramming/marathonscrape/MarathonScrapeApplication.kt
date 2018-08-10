package com.stonesoupprogramming.marathonscrape

import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader

@Configuration
class Configuration {

    @Bean
    fun jquery () = BufferedReader(InputStreamReader(Configuration::class.java.getResourceAsStream("/js/jquery-3.3.1.js"))).readText()

    @Bean(destroyMethod = "close")
    fun chromeDriver() : ChromeDriver {
        return with(ChromeOptions()){
            addArguments("--disable-web-security")
            ChromeDriver(this)
        }
    }
}

@SpringBootApplication
class MarathonScrapeApplication

fun main(args: Array<String>) {
    runApplication<MarathonScrapeApplication>(*args)
}

@Component
class Application(@Autowired private val nyWebScraper: NyWebScraper) : CommandLineRunner {

    override fun run(vararg args: String) {
        val records = nyWebScraper.scrape()
        records.writeToCsv("NYRR.csv")
    }
}