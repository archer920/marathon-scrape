package com.stonesoupprogramming.marathonscrape

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue

@Configuration
@EnableAsync(proxyTargetClass = true)
class Configuration {

    @Bean
    fun runnerDataQueue() = LinkedBlockingQueue<RunnerData>()

    @Bean
    fun asyncExecute () : ThreadPoolTaskExecutor {
        with (ThreadPoolTaskExecutor()){
            corePoolSize = Runtime.getRuntime().availableProcessors()
            setQueueCapacity(1000)
            setThreadNamePrefix("Marathon-Scraper-")
            initialize()
            return this
        }
    }

    @Bean
    fun stateCodes () = listOf("AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "DC", "FL", "GA", "HI",
            "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH",
            "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA",
            "WV", "WI", "WY", "AS", "GU", "MH", "FM", "MP", "PW", "PR", "VI")

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
        @Autowired private val applicationContext: ApplicationContext,
        @Autowired private val runnerDataRepository: RunnerDataRepository,
        @Autowired private val statusReporter: StatusReporter,
        @Autowired private val runnerDataConsumer: RunnerDataConsumer,
        @Autowired private val bostonProducer: BostonProducer,
        @Autowired private val nyMarathonProducer: NyMarathonProducer,
        @Autowired private val laMarathonProducer: LaMarathonProducer,
        @Autowired private val marineCorpsProducer: MarineCorpsProducer,
        @Autowired private val sanFranciscoProducer: SanFranciscoProducer,
        @Autowired private val berlinProducer: BerlinProducer,
        @Autowired private val chicagoProducer: ChicagoProducer) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(Application::class.java)

    override fun run(vararg args: String) {
        runnerDataConsumer.insertValues()
        statusReporter.reportStatus()

        val threads = mutableListOf<CompletableFuture<String>>()

        if(args.contains("--Boston-Marathon-Scrape")){
            threads.addAll(bostonProducer.process())
        }
        if(args.contains("--Write-Boston-Marathon")){
            writeFile(Sources.BOSTON, 2014, 2018)
        }

        if(args.contains("--Chicago-Marathon-Scrape")){
            threads.addAll(chicagoProducer.process())
        }
        if(args.contains("--Write-Chicago-Marathon")){
            writeFile(Sources.CHICAGO, 2014, 2017)
        }

        if(args.contains("--Ny-Marathon-Scrape")){
            threads.addAll(nyMarathonProducer.process())
        }
        if(args.contains("--Write-Ny-Marathon")){
            writeFile(Sources.NY_MARATHON_GUIDE, 2014, 2017)
        }

        if(args.contains("--La-Marathon-Scrape")){
            threads.addAll(laMarathonProducer.process())
        }
        if(args.contains("--Write-LA-Marathon")){
            writeFile(Sources.LA, 2014, 2017)
        }

        if(args.contains("--Marine-Corps-Scrape")){
            threads.addAll(marineCorpsProducer.process())
        }
        if(args.contains("--Write-Marine-Corps")){
            writeFile(Sources.MARINES, 2014, 2017)
        }

        if(args.contains("--San-Francisco-Scrape")){
            threads.addAll(sanFranciscoProducer.process())
        }
        if(args.contains("--Write-San-Francisco")){
            writeFile(Sources.SAN_FRANSCISO, 2014, 2018)
        }

        if(args.contains("--Berlin-Scrape")){
            threads.addAll(berlinProducer.process())
        }
        if(args.contains("--Write-Berlin")){
            writeFile(Sources.BERLIN, 2014, 2018)
        }


        CompletableFuture.allOf(*threads.toTypedArray()).join()
        this.runnerDataConsumer.signalShutdown = true
        SpringApplication.exit(applicationContext, ExitCodeGenerator { 0 })

//        if(args.contains("--Vienna-City-Marathon-Scrape")){
//            runnerDataConsumer.insertValues(queue)
//            viennaMarathonScrape.scrape(ChromeDriver(), queue, 2014, "https://www.vienna-marathon.com/?surl=cd162e16e318d263fd56d6261673fe72#goto-result")
//        }
//        if(args.contains("--Write-Vienna-City-Marathon")){
//            writeFile(Sources.VIENNA, 2014, 2018)
//        }
    }

    private fun writeFile(source : String, startYear : Int, endYear : Int){
        logger.info("Starting file export...")
        for(i in startYear..endYear){
            runnerDataRepository.findByMarathonYearAndSourceOrderByAge(i, source).distinctBy { runnerData: RunnerData -> runnerData.place }.writeToCsv("$source-$i.csv")
        }
        logger.info("Finished file export...")
    }
}