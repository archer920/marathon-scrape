package com.stonesoupprogramming.marathonscrape

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue

@Component
class BostonProducer(@Autowired private val runnerDataQueue : LinkedBlockingQueue<RunnerData>,
                     @Autowired private val bostonMarathonScrape: BostonMarathonScrape){

    private val logger = LoggerFactory.getLogger(BostonProducer::class.java)
    val threads = mutableListOf<CompletableFuture<String>>()

    fun process(): List<CompletableFuture<String>> {
        return try {
            logger.info("Starting Boston Marathon")

            listOf(2014, 2015, 2016, 2017, 2018).forEach {
                threads.add(bostonMarathonScrape.scrape(runnerDataQueue, it))
            }
            threads.toList()
        } catch (e : Exception){
            logger.error("Failed to start Boston", e)
            emptyList()
        }
    }
}

@Component
class ChicagoProducer(@Autowired private val runnerDataQueue: LinkedBlockingQueue<RunnerData>,
                      @Autowired private val chicagoMarathonScrape: ChicagoMarathonScrape){

    private val logger = LoggerFactory.getLogger(ChicagoProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    fun process() : List<CompletableFuture<String>> {
        return try {
            logger.info("Starting Chicago Scrape")

            val years = listOf(2014, 2015, 2016, 2017)
            years.forEach { threads.add(chicagoMarathonScrape.scrape(runnerDataQueue, it, "M")) }
            years.forEach { threads.add(chicagoMarathonScrape.scrape(runnerDataQueue, it, "W")) }
            threads.add(chicagoMarathonScrape.scrape2017(runnerDataQueue, "M"))
            threads.add(chicagoMarathonScrape.scrape2017(runnerDataQueue, "W"))

            threads.toList()
        }  catch (e : Exception){
            logger.error("Chicago Marathon failed", e)
            emptyList()
        }
    }
}

@Component
class NyMarathonProducer(@Autowired private val runnerDataQueue: LinkedBlockingQueue<RunnerData>,
                         @Autowired private val marathonGuideScraper: MarathonGuideScraper) {

    private val logger = LoggerFactory.getLogger(NyMarathonProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    fun process() : List<CompletableFuture<String>> {
        return try {
            logger.info("Starting New York Scrape")

            val urls = mapOf(2014 to "http://www.marathonguide.com/results/browse.cfm?MIDD=472141102",
                    2015 to "http://www.marathonguide.com/results/browse.cfm?MIDD=472151101",
                    2016 to "http://www.marathonguide.com/results/browse.cfm?MIDD=472161106",
                    2017 to "http://www.marathonguide.com/results/browse.cfm?MIDD=472171105")

            val placeFirstPositions = ColumnPositions(
                    ageGender = 0,
                    place = 1,
                    finishTime = 4,
                    nationality = 5,
                    age = -1)
            val timeFirstPositions = ColumnPositions(
                    ageGender = 0,
                    place = 2,
                    finishTime = 1,
                    nationality = 5,
                    age = -1
            )

            urls.forEach { year, url ->
                when(year){
                    2014 -> threads.add(marathonGuideScraper.scrape(runnerDataQueue, year, url, Sources.NY_MARATHON_GUIDE, placeFirstPositions))
                    2015 -> threads.add(marathonGuideScraper.scrape(runnerDataQueue, year, url, Sources.NY_MARATHON_GUIDE, timeFirstPositions))
                    2016 -> threads.add(marathonGuideScraper.scrape(runnerDataQueue, year, url, Sources.NY_MARATHON_GUIDE, timeFirstPositions))
                    2017 -> threads.add(marathonGuideScraper.scrape(runnerDataQueue, year, url, Sources.NY_MARATHON_GUIDE, placeFirstPositions))
                }
            }
            threads.toList()
        } catch (e : Exception){
            logger.error("New York Marathon failed", e)
            emptyList()
        }
    }
}

@Component
class LaMarathonProducer(@Autowired private val runnerDataQueue: LinkedBlockingQueue<RunnerData>,
                         @Autowired private val trackShackResults: TrackShackResults){

    private val logger = LoggerFactory.getLogger(LaMarathonProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    fun process() : List<CompletableFuture<String>> {
        return try {
            logger.info("Starting Los Angelas Scrape")

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

            val columnInfo = ColumnPositions(place = 4, age = 3, finishTime = 15, nationality = 16, ageGender = -1)

            mens2015.forEach { threads.add(trackShackResults.scrape(runnerDataQueue, it, 2015, "M", Sources.LA, columnInfo)) }
            mens2016.forEach { threads.add(trackShackResults.scrape(runnerDataQueue, it, 2016, "M", Sources.LA, columnInfo)) }
            mens2017.forEach { threads.add(trackShackResults.scrape(runnerDataQueue, it, 2017, "M", Sources.LA, columnInfo)) }

            womens2015.forEach { threads.add(trackShackResults.scrape(runnerDataQueue, it, 2015, "W", Sources.LA, columnInfo)) }
            womens2016.forEach { threads.add(trackShackResults.scrape(runnerDataQueue, it, 2016, "W", Sources.LA, columnInfo)) }
            womens2017.forEach { threads.add(trackShackResults.scrape(runnerDataQueue, it, 2017, "W", Sources.LA, columnInfo)) }

            threads.add(trackShackResults.scrape2014(runnerDataQueue))
            threads.toList()
        } catch (e : Exception){
            logger.error("Los Angelas Marathon Failed", e)
            emptyList()
        }
    }
}

@Component
class MarineCorpsProducer(@Autowired private val runnerDataQueue: LinkedBlockingQueue<RunnerData>,
                         @Autowired private val marineCorpsScrape: MarineCorpsScrape) {

    private val logger = LoggerFactory.getLogger(NyMarathonProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    fun process() : List<CompletableFuture<String>> {
        return try {
            logger.info("Starting Marine Corps Scrape")

            listOf(2014, 2015, 2016, 2017).forEach { threads.add(marineCorpsScrape.scrape(runnerDataQueue, it)) }
            threads.toList()
        } catch (e : Exception){
            logger.error("Marine Corps Marathon failed", e)
            emptyList()
        }
    }
}

@Component
class SanFranciscoProducer(@Autowired private val runnerDataQueue: LinkedBlockingQueue<RunnerData>,
                          @Autowired private val sanFranciscoScrape: SanFranciscoScrape) {

    private val logger = LoggerFactory.getLogger(NyMarathonProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    fun process() : List<CompletableFuture<String>> {
        return try {
            logger.info("Starting San Francisco Scrape")

            threads.add(sanFranciscoScrape.scrape(runnerDataQueue, 2018, "https://www.runraceresults.com/Secure/RaceResults.cfm?ID=RCLF2018"))
            threads.add(sanFranciscoScrape.scrape(runnerDataQueue, 2017, "https://www.runraceresults.com/Secure/RaceResults.cfm?ID=RCLF2017"))
            threads.add(sanFranciscoScrape.scrape(runnerDataQueue, 2016, "https://www.runraceresults.com/Secure/RaceResults.cfm?ID=RCLF2016"))
            threads.add(sanFranciscoScrape.scrape(runnerDataQueue,2015, "https://www.runraceresults.com/Secure/RaceResults.cfm?ID=RCLF2015"))
            threads.add(sanFranciscoScrape.scrape(runnerDataQueue, 2014, "https://www.runraceresults.com/Secure/RaceResults.cfm?ID=RCLF2014"))

            threads.toList()
        } catch (e : Exception){
            logger.error("Marine Corps Marathon failed", e)
            emptyList()
        }
    }
}

@Component
class BerlinProducer(@Autowired private val runnerDataQueue: LinkedBlockingQueue<RunnerData>,
                           @Autowired private val berlinMarathonScraper: BerlinMarathonScraper) {

    private val logger = LoggerFactory.getLogger(BerlinProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    fun process() : List<CompletableFuture<String>> {
        return try {
            logger.info("Starting Berlin Scrape")

            threads.add(berlinMarathonScraper.scrape(runnerDataQueue, 2017))
            threads.add(berlinMarathonScraper.scrape(runnerDataQueue, 2016))
            threads.add(berlinMarathonScraper.scrape(runnerDataQueue,2015))
            threads.add(berlinMarathonScraper.scrape(runnerDataQueue, 2014))

            threads.toList()
        } catch (e : Exception){
            logger.error("Berlin Marathon failed", e)
            emptyList()
        }
    }
}

@Component
class ViennaProducer(@Autowired private val runnerDataQueue: LinkedBlockingQueue<RunnerData>,
                     @Autowired private val viennaMarathonScraper: ViennaMarathonScraper) {

    private val logger = LoggerFactory.getLogger(BerlinProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    fun process() : List<CompletableFuture<String>> {
        return try {
            logger.info("Starting Vienna Scrape")
            for (i in 3..14){
                for(year in 2014..2018){
                    threads.add(viennaMarathonScraper.scrape(runnerDataQueue, year, Gender.MALE, i))
                    threads.add(viennaMarathonScraper.scrape(runnerDataQueue, year, Gender.FEMALE, i))
                }
            }
            threads.toList()
        } catch (e : Exception){
            logger.error("Berlin Marathon failed", e)
            emptyList()
        }
    }
}

@Component
class MedtronicProducer(@Autowired private val runnerDataQueue: LinkedBlockingQueue<RunnerData>,
                     @Autowired private val medtronicMarathonScraper: MedtronicMarathonScraper) {

    private val logger = LoggerFactory.getLogger(BerlinProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    fun process() : List<CompletableFuture<String>> {
        return try {
            logger.info("Starting Medtronic Scrape")

            threads.add(medtronicMarathonScraper.scrape(runnerDataQueue, "https://www.mtecresults.com/race/show/2569/2014_Medtronic_Twin_Cities_Marathon-Marathon", 2014))
            threads.add(medtronicMarathonScraper.scrape(runnerDataQueue, "https://www.mtecresults.com/race/show/3410/2015_Medtronic_Twin_Cities_Marathon-Marathon", 2015))
            threads.add(medtronicMarathonScraper.scrape(runnerDataQueue, "https://www.mtecresults.com/race/show/4497/2016_Medtronic_Twin_Cities_Marathon-Marathon", 2016))
            threads.add(medtronicMarathonScraper.scrape(runnerDataQueue, "https://www.mtecresults.com/race/show/5828/2017_Medtronic_Twin_Cities_Marathon-Marathon", 2017))

            threads.toList()
        } catch (e : Exception){
            logger.error("Berlin Marathon failed", e)
            emptyList()
        }
    }
}

@Component
class DisneyMarathonProducer(@Autowired private val runnerDataQueue: LinkedBlockingQueue<RunnerData>,
                         @Autowired private val trackShackResults: TrackShackResults){

    private val logger = LoggerFactory.getLogger(DisneyMarathonProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    fun process() : List<CompletableFuture<String>> {
        return try {
            logger.info("Starting Disney Scrape")
            val mens2014 = mutableListOf<String>()
            val mens2015 = mutableListOf<String>()
            val mens2016 = mutableListOf<String>()
            val mens2017 = mutableListOf<String>()
            val mens2018 = mutableListOf<String>()

            val womens2014 = mutableListOf<String>()
            val womens2015 = mutableListOf<String>()
            val womens2016 = mutableListOf<String>()
            val womens2017 = mutableListOf<String>()
            val womens2018 = mutableListOf<String>()

            for(i in 4..16){
                mens2015.add("https://www.trackshackresults.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=A&Ind=$i")
                mens2015.add("https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=B&Ind=$i")
                mens2016.add("https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=B&Ind=$i")
                mens2017.add("https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=A&Ind=$i")
                mens2018.add("https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=B&Ind=$i")
            }

            for(i in 17..29){
                womens2014.add("https://www.trackshackresults.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=N&Ind=$i")
                womens2015.add("https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=B&Ind=$i")
                womens2016.add("https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=B&Ind=$i")
                womens2017.add("https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=A&Ind=$i")
                womens2018.add("https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=B&Ind=$i")
            }

            val columnInfo = ColumnPositions(nationality = 12, finishTime = 11, age = 3, place = 4, ageGender = -1)

            mens2014.forEach { threads.add(trackShackResults.scrape(runnerDataQueue, it, 2014, "M", Sources.DISNEY, columnInfo)) }
            mens2015.forEach { threads.add(trackShackResults.scrape(runnerDataQueue, it, 2015, "M", Sources.DISNEY, columnInfo)) }
            mens2016.forEach { threads.add(trackShackResults.scrape(runnerDataQueue, it, 2016, "M", Sources.DISNEY, columnInfo)) }
            mens2017.forEach { threads.add(trackShackResults.scrape(runnerDataQueue, it, 2017, "M", Sources.DISNEY, columnInfo)) }
            mens2018.forEach { threads.add(trackShackResults.scrape(runnerDataQueue, it, 2018, "M", Sources.DISNEY, columnInfo)) }

            womens2014.forEach { threads.add(trackShackResults.scrape(runnerDataQueue, it, 2014, "W", Sources.DISNEY, columnInfo)) }
            womens2015.forEach { threads.add(trackShackResults.scrape(runnerDataQueue, it, 2015, "W", Sources.DISNEY, columnInfo)) }
            womens2016.forEach { threads.add(trackShackResults.scrape(runnerDataQueue, it, 2016, "W", Sources.DISNEY, columnInfo)) }
            womens2017.forEach { threads.add(trackShackResults.scrape(runnerDataQueue, it, 2017, "W", Sources.DISNEY, columnInfo)) }
            womens2018.forEach { threads.add(trackShackResults.scrape(runnerDataQueue, it, 2017, "W", Sources.DISNEY, columnInfo)) }

            threads.add(trackShackResults.scrape2014(runnerDataQueue))
            threads.toList()
        } catch (e : Exception){
            logger.error("Los Angelas Marathon Failed", e)
            emptyList()
        }
    }
}