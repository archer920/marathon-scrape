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
    fun canadaProvinceCodes() = listOf("AB", "BC", "MB", "NB", "NL", "NS", "NT", "NU", "ON", "PE", "QC", "SK", "YT")

    @Bean
    fun usStateCodes () = listOf("AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "DC", "FL", "GA", "HI",
            "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH",
            "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA",
            "WV", "WI", "WY", "AS", "GU", "MH", "FM", "MP", "PW", "PR", "VI")

    @Bean
    fun producers(@Autowired stockholmProducer: StockholmProducer,
                  @Autowired tcsAmsterdamProducer: TcsAmsterdamProducer,
                  @Autowired santiagoProducer: SantiagoProducer,
                  @Autowired copenhagenProducer: CopenhagenProducer,
                  @Autowired genevaProducer: GenevaProducer,
                  @Autowired rheinEnergieProducer: RheinEnergieProducer,
                  @Autowired bournemouthProducer: BournemouthProducer,
                  @Autowired stJudeProducer: StJudeProducer,
                  @Autowired indianapolisProducer: IndianapolisProducer,
                  @Autowired munchenProducer: MunchenProducer,
                  @Autowired fargoProducer: FargoProducer,
                  @Autowired brightonProducer: BrightonProducer,
                  @Autowired vancouverProducer: VancouverProducer,
                  @Autowired surfCityProducer: SurfCityProducer,
                  @Autowired liverpoolProducer: LiverpoolProducer,
                  @Autowired sanDiegoProducer: SanDiegoProducer) : Map<MarathonSources, BaseProducer> {

        return mapOf(MarathonSources.Stockholm to stockholmProducer,
                MarathonSources.Amsterdam to tcsAmsterdamProducer,
                MarathonSources.Santiago to santiagoProducer,
                MarathonSources.Copenhagen to copenhagenProducer,
                MarathonSources.Geneva to genevaProducer,
                MarathonSources.RheinEnergie to rheinEnergieProducer,
                MarathonSources.Bournemouth to bournemouthProducer,
                MarathonSources.Memphis to stJudeProducer,
                MarathonSources.Indianapolis to indianapolisProducer,
                MarathonSources.Munchen to munchenProducer,
                MarathonSources.Fargo to fargoProducer,
                MarathonSources.Brighton to brightonProducer,
                MarathonSources.Vancouver to vancouverProducer,
                MarathonSources.SurfCity to surfCityProducer,
                MarathonSources.Liverpool to liverpoolProducer,
                MarathonSources.SanDiego to sanDiegoProducer)
    }
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
        @Autowired private val producers: Map<MarathonSources, BaseProducer>) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(Application::class.java)

    override fun run(vararg args: String) {
        args.toMarathonSources().forEach {
            it?.let {
                statusReporter.reportStatus(it)
            }
        }

        process(*args)

        this.statusReporter.shutdown = true

        writeCompleted(*args)

        logger.info("Exiting...")

        SpringApplication.exit(applicationContext, ExitCodeGenerator { 0 })
    }

    private fun process(vararg args : String){
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

    private fun writeCompleted(vararg args: String){
        args.toMarathonSources().forEach { it ->
            it?.let {
                writeFile(it, it.startYear, it.endYear)
            }
        }
    }

    private fun writeFile(source : MarathonSources, startYear : Int, endYear : Int){
        logger.info("Starting file export...")
        for(i in startYear..endYear){
            runnerDataRepository.findByMarathonYearAndSourceOrderByAge(i, source).writeToCsv("$source-$i.csv")
        }
        logger.info("Finished file export...")
    }
}