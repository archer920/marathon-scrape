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
@EnableAsync(proxyTargetClass = true)
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
    fun nyScrapers(@Autowired first: NyWebScraper,
                   @Autowired second: NyWebScraper,
                   @Autowired third: NyWebScraper,
                   @Autowired fourth: NyWebScraper): List<WebScraper> {
        return listOf(first, second, third, fourth)
    }

    @Bean
    fun consumers(@Autowired first : RunnerDataConsumer,
                    @Autowired second: RunnerDataConsumer,
                    @Autowired third: RunnerDataConsumer,
                    @Autowired fourth: RunnerDataConsumer) = listOf(first, second, third, first)

}

@SpringBootApplication
class MarathonScrapeApplication

fun main(args: Array<String>) {
    runApplication<MarathonScrapeApplication>(*args)
}

@Component
class Application(
        @Autowired private val runnerDataRepository: RunnerDataRepository,
        @Autowired @Qualifier("nyScrapers") private val nyScrapers: List<WebScraper>,
        @Autowired @Qualifier("berlinMarathonScraper") private val berlinMarathonScraper: WebScraper,
        @Autowired @Qualifier("viennaMarathonScrape") private val viennaMarathonScrape: WebScraper,
        @Autowired @Qualifier("bostonMarathonScrape") private val bostonMarathonScrape: WebScraper,
        @Autowired @Qualifier("consumers") private val runnerDataConsumers : List<RunnerDataConsumer>) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(Application::class.java)

    override fun run(vararg args: String) {
        val queue = LinkedBlockingQueue<RunnerData>()

        if(args.contains("--NYRR")){
            nyScrapers[0].scrape(ChromeDriver(), queue,2014, "https://results.nyrr.org/event/M2014/finishers")
            runnerDataConsumers[0].insertValues(queue)
            Thread.sleep(10000)

            nyScrapers[1].scrape(ChromeDriver(), queue, 2015, "https://results.nyrr.org/event/M2015/finishers")
            runnerDataConsumers[1].insertValues(queue)
            Thread.sleep(10000)

            nyScrapers[2].scrape(ChromeDriver(), queue, 2016, "https://results.nyrr.org/event/M2016/finishers")
            runnerDataConsumers[2].insertValues(queue)
            Thread.sleep(10000)

            nyScrapers[3].scrape(ChromeDriver(), queue, 2017, "https://results.nyrr.org/event/M2017/finishers")
            runnerDataConsumers[3].insertValues(queue)
        }
        if(args.contains("--Write-NYRR-CSV")){
            writeFile(Sources.NY, 2014, 2017)
        }
        if(args.contains("--Berlin-Marathon-Scrape")){
            val url = "https://www.bmw-berlin-marathon.com/en/facts-and-figures/results-archive.html"
            berlinMarathonScraper.scrape(ChromeDriver(), queue, 2014, url)
            runnerDataConsumers.forEach { it.insertValues(queue) }
        }
        if(args.contains("--Write-Berlin-Marathon-Scrape")) {
            writeFile(Sources.BERLIN, 2014, 2017)
        }
        if(args.contains("--Vienna-City-Marathon-Scrape")){
            viennaMarathonScrape.scrape(ChromeDriver(), queue, 2014, "https://www.vienna-marathon.com/?surl=cd162e16e318d263fd56d6261673fe72#goto-result")
            runnerDataConsumers.forEach { it.insertValues(queue) }
        }
        if(args.contains("--Write-Vienna-City-Marathon")){
            writeFile(Sources.VIENNA, 2014, 2018)
        }
        if(args.contains("--Boston-Marathon-Scrape")){
            bostonMarathonScrape.scrape(ChromeDriver(), queue, 2014, "http://registration.baa.org/cfm_Archive/iframe_ArchiveSearch.cfm")
            //runnerDataConsumers.forEach { it.insertValues(queue) }
        }
    }

    private fun writeFile(source : String, startYear : Int, endYear : Int){
        logger.info("Starting file export...")
        for(i in startYear..endYear){
            runnerDataRepository.findByMarathonYearAndSourceOrderByAge(i, source).writeToCsv("$source-$i.csv")
        }
        logger.info("Finished file export...")
    }
}