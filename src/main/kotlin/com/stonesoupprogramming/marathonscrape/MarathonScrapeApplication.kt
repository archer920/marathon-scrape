package com.stonesoupprogramming.marathonscrape

import org.openqa.selenium.chrome.ChromeDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
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
import java.util.concurrent.LinkedBlockingQueue

@Configuration
@EnableAsync
class Configuration {

    @Bean
    fun jquery () = BufferedReader(InputStreamReader(Configuration::class.java.getResourceAsStream("/js/jquery-3.3.1.js"))).readText()

    @Bean
    fun asyncExecute () : ThreadPoolTaskExecutor {
        with (ThreadPoolTaskExecutor()){
            corePoolSize = Runtime.getRuntime().availableProcessors()
            maxPoolSize = Runtime.getRuntime().availableProcessors()
            setQueueCapacity(500)
            setThreadNamePrefix("Automotive-CMS")
            initialize()
            return this
        }
    }

    @Bean
    @Qualifier("NY-SCRAPERS")
    fun nyScrapers(@Autowired first: NyWebScraper,
                   @Autowired second: NyWebScraper,
                   @Autowired third: NyWebScraper,
                   @Autowired fourth: NyWebScraper): List<NyWebScraper> {
        return listOf(first, second, third, fourth)
    }

    @Bean
    @Qualifier("NY-CONSUMERS")
    fun nyConsumers(@Autowired first : NyConsumer,
                    @Autowired second: NyConsumer,
                    @Autowired third: NyConsumer,
                    @Autowired fourth: NyConsumer) = listOf(first, second, third, first)

}

@SpringBootApplication
class MarathonScrapeApplication

fun main(args: Array<String>) {
    runApplication<MarathonScrapeApplication>(*args)
}

@Component
class Application(
        @Autowired private val runnerDataRepository: RunnerDataRepository,
        @Autowired @Qualifier("NY-SCRAPERS") private val nyScrapers: List<NyWebScraper>,
        @Autowired @Qualifier("NY-CONSUMERS") private val nyConsumers : List<NyConsumer>) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(Application::class.java)

    override fun run(vararg args: String) {
        if(args.contains("--NYRR")){
            val queue2014 = LinkedBlockingQueue<RunnerData>()
            nyScrapers[0].scrape(ChromeDriver(), queue2014,2014, "https://results.nyrr.org/event/M2014/finishers")
            nyConsumers[0].insertValues(queue2014)
            Thread.sleep(10000)

            val queue2015 = LinkedBlockingQueue<RunnerData>()
            nyScrapers[1].scrape(ChromeDriver(), queue2015, 2015, "https://results.nyrr.org/event/M2015/finishers")
            nyConsumers[1].insertValues(queue2015)
            Thread.sleep(10000)

            val queue2016 = LinkedBlockingQueue<RunnerData>()
            nyScrapers[2].scrape(ChromeDriver(), queue2016, 2016, "https://results.nyrr.org/event/M2016/finishers")
            nyConsumers[2].insertValues(queue2016)
            Thread.sleep(10000)

            val queue2017 = LinkedBlockingQueue<RunnerData>()
            nyScrapers[3].scrape(ChromeDriver(), queue2017, 2017, "https://results.nyrr.org/event/M2017/finishers")
            nyConsumers[3].insertValues(queue2017)
        }
        if(args.contains("--Write-NYRR-CSV")){
            logger.info("Starting file export...")
            runnerDataRepository.queryForExport(Sources.NY).writeToCsv("${Sources.NY}.csv")
            logger.info("Finished file export...")
        }
    }
}