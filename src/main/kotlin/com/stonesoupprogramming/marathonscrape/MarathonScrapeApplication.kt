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
        @Autowired private val viennaProducer: ViennaProducer,
        @Autowired private val medtronicProducer: MedtronicProducer,
        @Autowired private val disneyMarathonProducer: DisneyMarathonProducer,
        @Autowired private val ottawaMarathonProducer: OttawaMarathonProducer,
        @Autowired private val budapestProducer: BudapestProducer,
        @Autowired private val chicagoProducer: ChicagoProducer) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(Application::class.java)

    object Args {
        const val BUDAPEST = "--budapest"
        const val OTTAWA = "--ottawa"
        const val SAN_FRANSCISCO = "--san-francisco"
        const val MEDTRONIC = "--medtronic"
        const val MARINES = "--marine-corps"
        const val VIENNA = "--vienna"
        const val BERLIN = "--berlin"
        const val BOSTON = "--boston"
        const val CHICAGO = "--chicago"
        const val NEW_YORK = "--new-york"
        const val LOS_ANGELES = "--los-angeles"
        const val DISNEY = "--disney"
    }
    override fun run(vararg args: String) {
        val consumer = runnerDataConsumer.insertValues()
        val status = statusReporter.reportStatus()

        process(*args)

        this.runnerDataConsumer.signalShutdown = true
        this.statusReporter.shutdown = true

        CompletableFuture.allOf(status, consumer)
        writeCompleted(*args)

        println("Press any key to quit")
        readLine()

        SpringApplication.exit(applicationContext, ExitCodeGenerator { 0 })
    }

    private fun process(vararg args : String){
        val threads = mutableListOf<CompletableFuture<String>>()

        if(args.contains(Args.BUDAPEST)){
            threads.addAll(budapestProducer.process())
        }
        if(args.contains(Args.DISNEY)){
            threads.addAll(disneyMarathonProducer.process())
        }
        if(args.contains(Args.LOS_ANGELES)){
            threads.addAll(laMarathonProducer.process())
        }
        if(args.contains(Args.NEW_YORK)){
            threads.addAll(nyMarathonProducer.process())
        }
        if(args.contains(Args.CHICAGO)){
            threads.addAll(chicagoProducer.process())
        }
        if(args.contains(Args.BOSTON)){
            threads.addAll(bostonProducer.process())
        }
        if(args.contains(Args.BERLIN)){
            threads.addAll(berlinProducer.process())
        }
        if(args.contains(Args.VIENNA)){
            threads.addAll(viennaProducer.process())
        }
        if(args.contains(Args.MARINES)){
            threads.addAll(marineCorpsProducer.process())
        }
        if(args.contains(Args.MEDTRONIC)){
            threads.addAll(medtronicProducer.process())
        }
        if(args.contains(Args.SAN_FRANSCISCO)){
            threads.addAll(sanFranciscoProducer.process())
        }
        if(args.contains(Args.OTTAWA)){
            threads.addAll(ottawaMarathonProducer.process())
        }

        CompletableFuture.allOf(*threads.toTypedArray()).join()
    }

    private fun writeCompleted(vararg args: String){
        if(args.contains(Args.BUDAPEST)){
            writeFile(Sources.BUDAPEST, 2014, 2017)
        }
        if(args.contains(Args.OTTAWA)){
            writeFile(Sources.OTTAWA, 2014, 2018)
        }
        if(args.contains(Args.SAN_FRANSCISCO)){
            writeFile(Sources.SAN_FRANSCISO, 2014, 2018)
        }
        if(args.contains(Args.MEDTRONIC)){
            writeFile(Sources.MEDTRONIC, 2014, 2017)
        }
        if(args.contains(Args.MARINES)){
            writeFile(Sources.MARINES, 2014, 2017)
        }
        if(args.contains(Args.VIENNA)){
            writeFile(Sources.VIENNA, 2014, 2018)
        }
        if(args.contains(Args.BERLIN)){
            writeFile(Sources.BERLIN, 2014, 2018)
        }
        if(args.contains(Args.BOSTON)){
            writeFile(Sources.BOSTON, 2014, 2018)
        }
        if(args.contains(Args.CHICAGO)){
            writeFile(Sources.CHICAGO, 2014, 2017)
        }
        if(args.contains(Args.NEW_YORK)){
            writeFile(Sources.NY_MARATHON_GUIDE, 2014, 2017)
        }
        if(args.contains(Args.LOS_ANGELES)){
            writeFile(Sources.LA, 2014, 2017)
        }
        if(args.contains(Args.DISNEY)){
            writeFile(Sources.DISNEY, 2014, 2018)
        }
    }

    private fun writeFile(source : String, startYear : Int, endYear : Int){
        logger.info("Starting file export...")
        for(i in startYear..endYear){
            runnerDataRepository.findByMarathonYearAndSourceOrderByAge(i, source).distinctBy { runnerData: RunnerData -> runnerData.place }.writeToCsv("$source-$i.csv")
        }
        logger.info("Finished file export...")
    }
}