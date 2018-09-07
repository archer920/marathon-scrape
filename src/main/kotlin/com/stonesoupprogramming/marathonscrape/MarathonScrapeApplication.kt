package com.stonesoupprogramming.marathonscrape

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.extension.toMarathonSources
import com.stonesoupprogramming.marathonscrape.extension.writeToCsv
import com.stonesoupprogramming.marathonscrape.models.RunnerData
import com.stonesoupprogramming.marathonscrape.producers.AbstractBaseProducer
import com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.races.*
import com.stonesoupprogramming.marathonscrape.producers.sites.marathonguide.*
import com.stonesoupprogramming.marathonscrape.repository.RunnerDataRepository
import com.stonesoupprogramming.marathonscrape.service.StatusReporterService
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
    fun asyncExecute(): ThreadPoolTaskExecutor {
        with(ThreadPoolTaskExecutor()) {
            corePoolSize = Runtime.getRuntime().availableProcessors()
            setQueueCapacity(1000)
            setThreadNamePrefix("Marathon-Scraper-")
            initialize()
            return this
        }
    }

    @Bean
    fun canadaProvinceCodes() = listOf("AB", "BC", "MB", "NB", "NL", "NS", "NT", "NU", "ON", "PE", "QC", "SK", "YT")

    @Bean
    fun usStateCodes() = listOf("AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "DC", "FL", "GA", "HI",
            "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH",
            "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA",
            "WV", "WI", "WY", "AS", "GU", "MH", "FM", "MP", "PW", "PR", "VI")

    @Bean
    fun producers(
            @Autowired utahValleyProducer: UtahValleyProducer,
            @Autowired missoulaProducer: MissoulaProducer,
            @Autowired erieProducer: ErieProducer,
            @Autowired seamTownProducer: SeamTownProducer,
            @Autowired mohawkHudsonRiverProducer: MohawkHudsonRiverProducer,
            @Autowired stLouisProducer: StLouisProducerNumbered,
            @Autowired longBeachProducer: LongBeachProducerNumbered,
            @Autowired poweradeMonterreyProducer: PoweradeMonterreyProducerNumbered,
            @Autowired philadelphiaProducer: PhiladelphiaProducer,
            @Autowired istanbulProducer: IstanbulProducerNumbered,
            @Autowired pfChangsArizonaProducer: PfChangsArizonaProducerNumbered,
            @Autowired helsinkiProducer: HelsinkiProducerNumbered,
            @Autowired berlinProducer: BerlinProducerNumbered,
            @Autowired maritzburgProducer: MaritzburgProducerNumbered,
            @Autowired milwaukeeProducer: MilwaukeeProducerNumbered,
            @Autowired myrtleBeachProducer: MyrtleBeachProducerNumbered,
            @Autowired belfastProducer: BelfastProducerNumbered,
            @Autowired nordeaRigaProducer: NordeaRigaProducerNumbered,
            @Autowired rockRollLasVegasProducer: RockRollLasVegasProducerNumbered,
            @Autowired cottonwoodProducer: CottonwoodProducerNumbered): Map<MarathonSources, AbstractBaseProducer> =
            
            mapOf(
                    MarathonSources.UtahValley to utahValleyProducer,
                    MarathonSources.Missoula to missoulaProducer,
                    MarathonSources.Erie to erieProducer,
                    MarathonSources.Seamtown to seamTownProducer,
                    MarathonSources.Mohawk to mohawkHudsonRiverProducer,
                    MarathonSources.StLouis to stLouisProducer,
                    MarathonSources.LongBeach to longBeachProducer,
                    MarathonSources.PoweradeMonterrery to poweradeMonterreyProducer,
                    MarathonSources.Philadelphia to philadelphiaProducer,
                    MarathonSources.Istanbul to istanbulProducer,
                    MarathonSources.Milwaukee to milwaukeeProducer,
                    MarathonSources.PfChangsArizona to pfChangsArizonaProducer,
                    MarathonSources.RockRollLasVegas to rockRollLasVegasProducer,
                    MarathonSources.NoredaRiga to nordeaRigaProducer,
                    MarathonSources.Cottonwood to cottonwoodProducer,
                    MarathonSources.Berlin to berlinProducer,
                    MarathonSources.Maritzburg to maritzburgProducer,
                    MarathonSources.Helsinki to helsinkiProducer,
                    MarathonSources.MyrtleBeach to myrtleBeachProducer,
                    MarathonSources.Belfast to belfastProducer)

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
        @Autowired private val statusReporterService: StatusReporterService,
        @Autowired private val producers: Map<MarathonSources, AbstractBaseProducer>) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(Application::class.java)

    override fun run(vararg args: String) {
        args.toMarathonSources().forEach { source ->
            source?.let {
                statusReporterService.reportStatusAsync(it)
            }
        }

        process(*args)

        this.statusReporterService.shutdown = true

        writeCompleted(*args)

        logger.info("Exiting...")

        args.toMarathonSources().forEach { source ->
            source?.let {
                statusReporterService.reportStatus(it)
            }
        }
        SpringApplication.exit(applicationContext, ExitCodeGenerator { 0 })
    }

    private fun process(vararg args: String) {
        val threads = mutableListOf<CompletableFuture<String>>()
        args.toMarathonSources().forEach { it ->
            it?.let {
                producers[it]?.let { producer ->
                    threads.addAll(producer.process())
                }
            }
        }

        CompletableFuture.allOf(*threads.toTypedArray()).join()
        logger.info("Finished Web Scraping")
    }

    private fun writeCompleted(vararg args: String) {
        args.toMarathonSources().forEach { it ->
            it?.let {
                writeFile(it, it.startYear, it.endYear)
            }
        }
    }

    private fun writeFile(source: MarathonSources, startYear: Int, endYear: Int) {
        logger.info("Starting file export...")
        for (i in startYear..endYear) {
            runnerDataRepository.findByMarathonYearAndSourceOrderByAge(i, source).writeToCsv("$source-$i.csv")
        }
        logger.info("Finished file export...")
    }
}