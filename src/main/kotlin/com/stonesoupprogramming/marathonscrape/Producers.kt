package com.stonesoupprogramming.marathonscrape

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue

@Component
class BostonProducer(@Autowired private val runnerDataQueue : LinkedBlockingQueue<RunnerData>,
                     @Autowired private val bostonMarathonScrape: BostonMarathonScrape){

    private val logger = LoggerFactory.getLogger(BostonProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    @Async
    fun process(): CompletableFuture<String> {
        return try {
            logger.info("Starting Boston Marathon")

            listOf(2014, 2015, 2016, 2017, 2018).forEach {
                threads.add(bostonMarathonScrape.scrape(runnerDataQueue, it))
            }
            CompletableFuture.allOf(*threads.toTypedArray()).join()

            logger.info("Boston Marathon has finished")
            successResult()
        } catch (e : Exception){
            logger.error("Boston Marathon failed", e)
            failResult()
        }
    }
}

@Component
class ChicagoProducer(@Autowired private val runnerDataQueue: LinkedBlockingQueue<RunnerData>,
                      @Autowired private val chicagoMarathonScrape: ChicagoMarathonScrape){

    private val logger = LoggerFactory.getLogger(ChicagoProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    @Async
    fun process() : CompletableFuture<String> {
        return try {
            logger.info("Starting Chicago Scrape")

            val years = listOf(2014, 2015, 2016, 2017)
            years.forEach { threads.add(chicagoMarathonScrape.scrape(runnerDataQueue, it, "M")) }
            years.forEach { threads.add(chicagoMarathonScrape.scrape(runnerDataQueue, it, "W")) }
            threads.add(chicagoMarathonScrape.scrape2017(runnerDataQueue, "M"))
            threads.add(chicagoMarathonScrape.scrape2017(runnerDataQueue, "W"))

            CompletableFuture.allOf(*threads.toTypedArray()).join()

            logger.info("Chicago Marathon has finished")
            successResult()
        }  catch (e : Exception){
            logger.error("Chicago Marathon failed", e)
            failResult()
        }
    }
}

@Component
class NyMarathonProducer(@Autowired private val runnerDataQueue: LinkedBlockingQueue<RunnerData>,
                         @Autowired private val nyMarathonScraper: NyMarathonScraper) {

    private val logger = LoggerFactory.getLogger(NyMarathonProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    @Async
    fun process() : CompletableFuture<String> {
        return try {
            val urls = mapOf(2014 to "http://www.marathonguide.com/results/browse.cfm?MIDD=472141102",
                    2015 to "http://www.marathonguide.com/results/browse.cfm?MIDD=472151101",
                    2016 to "http://www.marathonguide.com/results/browse.cfm?MIDD=472161106",
                    2017 to "http://www.marathonguide.com/results/browse.cfm?MIDD=472171105")

            urls.forEach { year, url -> threads.add(nyMarathonScraper.scrape(runnerDataQueue, year, url)) }
            CompletableFuture.allOf(*threads.toTypedArray()).join()

            successResult()
        } catch (e : Exception){
            logger.error("New York Marathon failed", e)
            failResult()
        }
    }
}

@Component
class LaMarathonProducer(@Autowired private val runnerDataQueue: LinkedBlockingQueue<RunnerData>,
                         @Autowired private val laMarathonScrape: LaMarathonScrape){

    private val logger = LoggerFactory.getLogger(LaMarathonProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    @Async
    fun process() : CompletableFuture<String> {
        return try {
                        val mens2015 = listOf("https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=D&Ind=2",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=DA&Ind=3",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=E&Ind=4",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=F&Ind=5",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=G&Ind=6",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=H&Ind=7",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=I&Ind=8",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=J&Ind=9",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=K&Ind=10",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=L&Ind=11",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=M&Ind=12",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=MA&Ind=13",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=N&Ind=14",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=NA&Ind=15",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=NB&Ind=16")
            val womens2015 = listOf("https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=O&Ind=17",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=R&Ind=18",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=S&Ind=19",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=SA&Ind=20",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=U&Ind=21",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=V&Ind=22",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=W&Ind=23",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=X&Ind=24",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=Y&Ind=25",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=Z&Ind=26",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=ZA&Ind=27",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=ZB&Ind=28",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=ZC&Ind=29",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=ZD&Ind=30",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=ZE&Ind=31")
            val mens2016 = listOf("https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=D&Ind=0",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=DA&Ind=1",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=E&Ind=2",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=F&Ind=3",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=G&Ind=4",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=H&Ind=5",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=I&Ind=6",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=J&Ind=7",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=K&Ind=8",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=L&Ind=9",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=M&Ind=10",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=MA&Ind=11",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=N&Ind=12",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=NA&Ind=13",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=NB&Ind=14")
            val womens2016 = listOf("https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=OA&Ind=15",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=R&Ind=16",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=S&Ind=17",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=SA&Ind=18",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=U&Ind=19",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=V&Ind=20",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=W&Ind=21",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=X&Ind=22",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=Y&Ind=23",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=Z&Ind=24",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=ZA&Ind=25",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=ZB&Ind=26",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=ZC&Ind=27",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=ZD&Ind=28")
            val mens2017 = listOf("https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=D&Ind=0",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=DA&Ind=1",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=E&Ind=2",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=F&Ind=3",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=G&Ind=4",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=H&Ind=5",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=I&Ind=6",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=J&Ind=7",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=K&Ind=8",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=L&Ind=9",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=M&Ind=10",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=MA&Ind=11",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=N&Ind=12",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=NA&Ind=13",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=NB&Ind=14")
            val womens2017 = listOf("https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=OA&Ind=15",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=R&Ind=16",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=S&Ind=17",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=SA&Ind=18",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=U&Ind=19",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=V&Ind=20",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=W&Ind=21",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=X&Ind=22",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=Y&Ind=23",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=Z&Ind=24",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=ZA&Ind=25",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=ZB&Ind=26",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=ZC&Ind=27",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=ZD&Ind=28",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=ZE&Ind=29")
            mens2015.forEach { threads.add(laMarathonScrape.scrape(runnerDataQueue, it, 2015, "M")) }
            mens2016.forEach { threads.add(laMarathonScrape.scrape(runnerDataQueue, it, 2016, "M")) }
            mens2017.forEach { threads.add(laMarathonScrape.scrape(runnerDataQueue, it, 2017, "M")) }

            womens2015.forEach { threads.add(laMarathonScrape.scrape(runnerDataQueue, it, 2015, "W")) }
            womens2016.forEach { threads.add(laMarathonScrape.scrape(runnerDataQueue, it, 2016, "W")) }
            womens2017.forEach { threads.add(laMarathonScrape.scrape(runnerDataQueue, it, 2017, "W")) }

            threads.add(laMarathonScrape.scrape2014(runnerDataQueue))

            CompletableFuture.allOf(*threads.toTypedArray()).join()
            successResult()
        } catch (e : Exception){
            logger.error("Los Angelas Marathon Failed", e)
            failResult()
        }
    }
}

@Component
class MarineCorpsProducer(@Autowired private val runnerDataQueue: LinkedBlockingQueue<RunnerData>,
                         @Autowired private val marineCorpsScrape: MarineCorpsScrape) {

    private val logger = LoggerFactory.getLogger(NyMarathonProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    @Async
    fun process() : CompletableFuture<String> {
        return try {
            listOf(2014, 2015, 2016, 2017).forEach { threads.add(marineCorpsScrape.scrape(runnerDataQueue, it)) }

            CompletableFuture.allOf(*threads.toTypedArray()).join()

            successResult()
        } catch (e : Exception){
            logger.error("Marine Corps Marathon failed", e)
            failResult()
        }
    }
}

@Component
class SanFranciscoProducer(@Autowired private val runnerDataQueue: LinkedBlockingQueue<RunnerData>,
                          @Autowired private val sanFranciscoScrape: SanFranciscoScrape) {

    private val logger = LoggerFactory.getLogger(NyMarathonProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    @Async
    fun process() : CompletableFuture<String> {
        return try {
            threads.add(sanFranciscoScrape.scrape(runnerDataQueue, 2018, "https://www.runraceresults.com/Secure/RaceResults.cfm?ID=RCLF2018"))
            threads.add(sanFranciscoScrape.scrape(runnerDataQueue, 2017, "https://www.runraceresults.com/Secure/RaceResults.cfm?ID=RCLF2017"))
            threads.add(sanFranciscoScrape.scrape(runnerDataQueue, 2016, "https://www.runraceresults.com/Secure/RaceResults.cfm?ID=RCLF2016"))
            threads.add(sanFranciscoScrape.scrape(runnerDataQueue,2015, "https://www.runraceresults.com/Secure/RaceResults.cfm?ID=RCLF2015"))
            threads.add(sanFranciscoScrape.scrape(runnerDataQueue, 2014, "https://www.runraceresults.com/Secure/RaceResults.cfm?ID=RCLF2014"))

            CompletableFuture.allOf(*threads.toTypedArray()).join()

            successResult()
        } catch (e : Exception){
            logger.error("Marine Corps Marathon failed", e)
            failResult()
        }
    }
}