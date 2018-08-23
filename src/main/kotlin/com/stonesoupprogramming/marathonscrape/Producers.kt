package com.stonesoupprogramming.marathonscrape

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import javax.annotation.PostConstruct

@Component
class BostonProducer(@Autowired private val runnerDataQueue : LinkedBlockingQueue<RunnerData>,
                     @Autowired private val pagedResultsRepository: PagedResultsRepository,
                     @Autowired private val bostonMarathonScrape: BostonMarathonScrape){

    private val logger = LoggerFactory.getLogger(BostonProducer::class.java)

    private val threads = mutableListOf<CompletableFuture<String>>()
    private var lastPageNum2014 : Int = 0
    private var lastPageNum2015 : Int = 0
    private var lastPageNum2016 : Int = 0
    private var lastPageNum2017 : Int = 0
    private var lastPageNum2018 : Int = 0

    @PostConstruct
    private fun init(){
        lastPageNum2014 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Boston, 2014).maxBy { it.pageNum }?.pageNum ?: 0
        lastPageNum2015 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Boston, 2015).maxBy { it.pageNum }?.pageNum ?: 0
        lastPageNum2016 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Boston, 2016).maxBy { it.pageNum }?.pageNum ?: 0
        lastPageNum2017 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Boston, 2017).maxBy { it.pageNum }?.pageNum ?: 0
        lastPageNum2018 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Boston, 2018).maxBy { it.pageNum }?.pageNum ?: 0
    }

    fun process(): List<CompletableFuture<String>> {
        return try {
            logger.info("Starting Boston Marathon")

            threads.add(bostonMarathonScrape.scrape(runnerDataQueue, lastPageNum2014, 2014))
            threads.add(bostonMarathonScrape.scrape(runnerDataQueue, lastPageNum2015, 2015))
            threads.add(bostonMarathonScrape.scrape(runnerDataQueue, lastPageNum2016, 2016))
            threads.add(bostonMarathonScrape.scrape(runnerDataQueue, lastPageNum2017, 2017))
            threads.add(bostonMarathonScrape.scrape(runnerDataQueue, lastPageNum2018, 2018))

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
                    2014 -> {
                        val rangeOptions = marathonGuideScraper.findRangeOptionsForUrl(url)
                        rangeOptions.forEach{ range -> threads.add(marathonGuideScraper.scrape(runnerDataQueue, year, url, MarathonSources.Nyc, placeFirstPositions, range))}

                    }
                    2015 -> {
                        val rangeOptions = marathonGuideScraper.findRangeOptionsForUrl(url)
                        rangeOptions.forEach { range ->  threads.add(marathonGuideScraper.scrape(runnerDataQueue, year, url, MarathonSources.Nyc, timeFirstPositions, range))}
                    }
                    2016 -> {
                        val rangeOptions = marathonGuideScraper.findRangeOptionsForUrl(url)
                        rangeOptions.forEach { range ->  threads.add(marathonGuideScraper.scrape(runnerDataQueue, year, url, MarathonSources.Nyc, timeFirstPositions, range))}
                    }
                    2017 -> {
                        val rangeOptions = marathonGuideScraper.findRangeOptionsForUrl(url)
                        rangeOptions.forEach { range -> threads.add(marathonGuideScraper.scrape(runnerDataQueue, year, url, MarathonSources.Nyc, placeFirstPositions, range)) }
                    }
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
class OttawaMarathonProducer(@Autowired private val runnerDataQueue: LinkedBlockingQueue<RunnerData>,
                             @Autowired private val pagedResultsRepository: PagedResultsRepository,
                             @Autowired private val sportStatsScrape: SportStatsScrape) {

    private val logger = LoggerFactory.getLogger(NyMarathonProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    private var lastPageNum2014: Int = 0
    private var lastPageNum2015: Int = 0
    private var lastPageNum2016: Int = 0
    private var lastPageNum2017: Int = 0

    @PostConstruct
    fun init(){
        lastPageNum2014 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Ottawa, 2014).maxBy { it.pageNum }?.pageNum ?: 0
        lastPageNum2015 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Ottawa, 2015).maxBy { it.pageNum }?.pageNum ?: 0
        lastPageNum2016 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Ottawa, 2016).maxBy { it.pageNum }?.pageNum ?: 0
        lastPageNum2017 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Ottawa, 2017).maxBy { it.pageNum }?.pageNum ?: 0
    }

    fun process() : List<CompletableFuture<String>> {
        return try {
            logger.info("Starting Ottawa Marathon Scrape")

            val columnPositions = ColumnPositions(ageGender = 5, place = 6, finishTime = 9)
            val nationalColumnPositions = ColumnPositions(nationality = 5, ageGender = 6, place = 7, finishTime = 14)

            threads.add(sportStatsScrape.scrape(runnerDataQueue, PagedResults(source = MarathonSources.Ottawa, marathonYear = 2014, url = "https://www.sportstats.ca/display-results.xhtml?raceid=166"), lastPageNum2014, 140, columnPositions))
            threads.add(sportStatsScrape.scrape(runnerDataQueue, PagedResults(source = MarathonSources.Ottawa, marathonYear = 2015, url = "https://www.sportstats.ca/display-results.xhtml?raceid=26006"), lastPageNum2015, 117, columnPositions))
            threads.add(sportStatsScrape.scrape(runnerDataQueue, PagedResults(source = MarathonSources.Ottawa, marathonYear = 2016, url = "https://www.sportstats.ca/display-results.xhtml?raceid=29494"), lastPageNum2016, 110, nationalColumnPositions))
            threads.add(sportStatsScrape.scrape(runnerDataQueue, PagedResults(source = MarathonSources.Ottawa, marathonYear = 2017, url = "https://www.sportstats.ca/display-results.xhtml?raceid=42854"), lastPageNum2017, 115, nationalColumnPositions))

            threads.toList()
        } catch (e : Exception){
            logger.error("Ottawa Marathon failed", e)
            emptyList()
        }
    }
}

@Component
class LaMarathonProducer(@Autowired private val runnerDataQueue: LinkedBlockingQueue<RunnerData>,
                         @Autowired private val urlPageRepository: UrlPageRepository,
                         @Autowired private val pagedResultsRepository: PagedResultsRepository,
                         @Autowired private val mtecResultScraper: MtecResultScraper,
                         @Autowired private val trackShackResults: TrackShackResults){

    private val logger = LoggerFactory.getLogger(LaMarathonProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    private lateinit var completedPages : MutableList<UrlPage>
    private var lastPageNum2014 = 0

    private val mensLinks = mutableListOf<UrlPage>()
    private val womensLinks = mutableListOf<UrlPage>()

    @PostConstruct
    fun init(){
        completedPages = urlPageRepository.findAll()

        buildMens2017()
        buildWomens2017()

        buildMens2016()
        buildWomens2016()

        buildMens2015()
        buildWomens2015()

        lastPageNum2014 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.LosAngeles, 2014).maxBy { it.pageNum }?.pageNum ?: 0
    }

    private fun buildWomens2015() {
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=O&Ind=17"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=R&Ind=18"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=S&Ind=19"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=SA&Ind=20"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=U&Ind=21"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=V&Ind=22"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=W&Ind=23"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=X&Ind=24"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=Y&Ind=25"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=Z&Ind=26"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=ZA&Ind=27"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=ZB&Ind=28"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=ZC&Ind=29"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=ZD&Ind=30"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=ZE&Ind=31"))
    }

    private fun buildMens2015() {
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=D&Ind=2"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=DA&Ind=3"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=E&Ind=4"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=F&Ind=5"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=G&Ind=6"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=H&Ind=7"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=I&Ind=8"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=J&Ind=9"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=K&Ind=10"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=L&Ind=11"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=M&Ind=12"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=MA&Ind=13"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=N&Ind=14"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=NA&Ind=15"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2015, url = "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=NB&Ind=16"))
    }

    private fun buildWomens2016() {
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=OA&Ind=15"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=R&Ind=16"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=S&Ind=17"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=SA&Ind=18"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=U&Ind=19"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=V&Ind=20"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=W&Ind=21"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=X&Ind=22"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=Y&Ind=23"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=Z&Ind=24"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=ZA&Ind=25"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=ZB&Ind=26"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=ZC&Ind=27"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=ZD&Ind=28"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=ZE&Ind=29"))
    }

    private fun buildMens2016() {
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=D&Ind=0"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=DA&Ind=1"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=E&Ind=2"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=F&Ind=3"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=G&Ind=4"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=H&Ind=5"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=I&Ind=6"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=J&Ind=7"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=K&Ind=8"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=L&Ind=9"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=M&Ind=10"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=MA&Ind=11"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=N&Ind=12"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=NA&Ind=13"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2016, url = "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=NB&Ind=14"))
    }

    private fun buildWomens2017() {
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=OA&Ind=15"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=R&Ind=16"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=S&Ind=17"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=SA&Ind=18"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=U&Ind=19"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=V&Ind=20"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=W&Ind=21"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=X&Ind=22"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=Y&Ind=23"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=Z&Ind=24"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=ZA&Ind=25"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=ZB&Ind=26"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=ZC&Ind=27"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=ZD&Ind=28"))
        womensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=ZE&Ind=29"))
    }

    private fun buildMens2017() {
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=D&Ind=0"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=DA&Ind=1"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=E&Ind=2"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=F&Ind=3"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=G&Ind=4"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=H&Ind=5"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=I&Ind=6"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=J&Ind=7"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=K&Ind=8"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=L&Ind=9"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=M&Ind=10"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=MA&Ind=11"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=N&Ind=12"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=NA&Ind=13"))
        mensLinks.add(UrlPage(source = MarathonSources.LosAngeles, marathonYear = 2017, url = "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=NB&Ind=14"))
    }

    fun process() : List<CompletableFuture<String>> {
        return try {
            logger.info("Starting Los Angeles Scrape")

            createThreads(mensLinks, Gender.MALE)
            createThreads(womensLinks, Gender.FEMALE)

            threads.add(mtecResultScraper.scrape(runnerDataQueue, "https://www.mtecresults.com/race/show/2074/2014_LA_Marathon-ASICS_LA_Marathon", 2014, MarathonSources.LosAngeles, lastPageNum2014, 43))
            threads.toList()
        } catch (e : Exception){
            logger.error("Los Angelas Marathon Failed", e)
            emptyList()
        }
    }

    private fun createThreads(links: MutableList<UrlPage>, gender: Gender) {
        val columnInfo = ColumnPositions(place = 4, age = 3, finishTime = 15, nationality = 16, ageGender = -1)

        links.forEach{ page ->
            if(completedPages.none { page.url == it.url }){
                threads.add(trackShackResults.scrape(runnerDataQueue, page, gender, columnInfo ))
            } else {
                logger.info("Skipping already completed page: $page")
            }
        }
    }
}

@Component
class MarineCorpsProducer(@Autowired private val runnerDataQueue: LinkedBlockingQueue<RunnerData>,
                          @Autowired private val pagedResultsRepository: PagedResultsRepository,
                         @Autowired private val marineCorpsScrape: MarineCorpsScrape) {

    private val logger = LoggerFactory.getLogger(NyMarathonProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    private var lastPageNum2014 : Int = 0
    private var lastPageNum2015 : Int = 0
    private var lastPageNum2016 : Int = 0
    private var lastPageNum2017 : Int = 0

    @PostConstruct
    fun init(){
        lastPageNum2014 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Marines, 2014).maxBy { it.pageNum }?.pageNum ?: 0
        lastPageNum2015 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Marines, 2015).maxBy { it.pageNum }?.pageNum ?: 0
        lastPageNum2016 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Marines, 2016).maxBy { it.pageNum }?.pageNum ?: 0
        lastPageNum2017 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Marines, 2017).maxBy { it.pageNum }?.pageNum ?: 0
    }

    fun process() : List<CompletableFuture<String>> {
        return try {
            logger.info("Starting Marine Corps Scrape")
            threads.add(marineCorpsScrape.scrape(runnerDataQueue, 2014, lastPageNum2014, 197))
            threads.add(marineCorpsScrape.scrape(runnerDataQueue, 2015, lastPageNum2015, 232))
            threads.add(marineCorpsScrape.scrape(runnerDataQueue, 2016, lastPageNum2016, 197))
            threads.add(marineCorpsScrape.scrape(runnerDataQueue, 2017, lastPageNum2017, 201))

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
                     @Autowired private val genderPagedResultsRepository: GenderPagedResultsRepository,
                     @Autowired private val viennaMarathonScraper: ViennaMarathonScraper) {

    private val logger = LoggerFactory.getLogger(BerlinProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    private lateinit var completedPages : List<GenderPagedResults>

    @PostConstruct
    fun init(){
        completedPages = genderPagedResultsRepository.findBySource(MarathonSources.Vienna)
    }

    fun process() : List<CompletableFuture<String>> {
        return try {
            logger.info("Starting Vienna Scrape")
            buildThreads(2014, Gender.FEMALE, 4) //1000-1499
            buildThreads(2014, Gender.MALE, 12) //5000-5499

            buildThreads(2015, Gender.FEMALE, 4)
            buildThreads(2015, Gender.MALE, 11) //4500-4999

            buildThreads(2016, Gender.FEMALE, 4)
            buildThreads(2016, Gender.MALE, 12)

            buildThreads(2017, Gender.FEMALE, 4)
            buildThreads(2017, Gender.MALE, 11)

            buildThreads(2018, Gender.FEMALE, 4)
            buildThreads(2018, Gender.MALE, 11)

            threads.toList()
        } catch (e : Exception){
            logger.error("Berlin Marathon failed", e)
            emptyList()
        }
    }

    private fun buildThreads(year : Int, gender: Gender, maxCategory: Int){
        for(i in 3..maxCategory){
            if(completedPages.none { page -> page.pageNum == i &&
                            page.marathonYear == year && page.gender == gender}){
                threads.add(viennaMarathonScraper.scrape(runnerDataQueue, year, gender, i))
            } else {
                logger.info("Skipping completed: page=$i, year=$year, gender=$gender")
            }

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
                             @Autowired private val urlPageRepository: UrlPageRepository,
                             @Autowired private val trackShackResults: TrackShackResults){

    private val logger = LoggerFactory.getLogger(DisneyMarathonProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    private lateinit var completedPages : List<UrlPage>
    private val mensLinks = mutableListOf<UrlPage>()
    private val womensLinks = mutableListOf<UrlPage>()

    private fun MutableList<UrlPage>.buildLinksForYear(year : Int, urls : List<String>){
        urls.forEach { url -> add(UrlPage(source = MarathonSources.Disney, marathonYear = year, url = url))}
    }

    @PostConstruct
    fun init(){
        completedPages = urlPageRepository.findBySource(MarathonSources.Disney)

        buildMens2014()
        buildWomens2014()

        buildMens2015()
        buildWomens2015()

        buildMens2016()
        buildWomens2016()

        buildMens2017()
        buildWomens2017()

        buildMens2018()
        buildWomens2018()
    }

    private fun buildWomens2015() {
        womensLinks.buildLinksForYear(2015, listOf(
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=M&Ind=17",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=N&Ind=18",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=O&Ind=19",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=P&Ind=20",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=Q&Ind=21",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=R&Ind=22",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=S&Ind=23",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=T&Ind=24",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=U&Ind=25",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=V&Ind=26",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=W&Ind=27",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=X&Ind=28",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=Y&Ind=29"
        ))
    }

    private fun buildMens2015() {
        mensLinks.buildLinksForYear(2015, listOf(
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=B&Ind=4",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=C&Ind=5",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=D&Ind=6",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=E&Ind=7",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=F&Ind=8",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=G&Ind=9",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=H&Ind=10",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=I&Ind=11",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=J&Ind=12",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=K&Ind=13",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=L&Ind=14",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=LA&Ind=15",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw15/mar_results.php?Link=27&Type=2&Div=LB&Ind=16"
        ))
    }

    private fun buildWomens2018() {
        womensLinks.buildLinksForYear(2018, listOf(
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=N&Ind=17",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=O&Ind=18",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=P&Ind=19",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=Q&Ind=20",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=R&Ind=21",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=S&Ind=22",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=T&Ind=23",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=U&Ind=24",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=V&Ind=25",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=W&Ind=26",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=X&Ind=27",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=Y&Ind=28"
        ))
    }

    private fun buildMens2018() {
        mensLinks.buildLinksForYear(2018, listOf(
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=B&Ind=4",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=C&Ind=5",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=D&Ind=6",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=E&Ind=7",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=F&Ind=8",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=G&Ind=9",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=H&Ind=10",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=I&Ind=11",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=J&Ind=12",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=K&Ind=13",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=L&Ind=14",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=LA&Ind=15",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw18/mar_results.php?Link=81&Type=2&Div=LB&Ind=16"
        ))
    }

    private fun buildWomens2017() {
        womensLinks.buildLinksForYear(2017, listOf(
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=M&Ind=18",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=N&Ind=19",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=O&Ind=20",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=P&Ind=21",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=Q&Ind=22",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=R&Ind=23",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=S&Ind=24",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=T&Ind=25",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=U&Ind=26",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=V&Ind=27",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=W&Ind=28",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=X&Ind=29",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=Y&Ind=30"
        ))
    }

    private fun buildMens2017() {
        mensLinks.buildLinksForYear(2017, listOf(
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=A&Ind=4",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=B&Ind=5",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=C&Ind=6",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=D&Ind=7",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=E&Ind=8",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=F&Ind=9",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=G&Ind=10",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=H&Ind=11",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=I&Ind=12",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=J&Ind=13",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=K&Ind=14",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=L&Ind=15",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=LA&Ind=16",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw17/mar_results.php?Link=62&Type=2&Div=LB&Ind=17"
        ))
    }

    private fun buildWomens2016() {
        womensLinks.buildLinksForYear(2016, listOf(
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=M&Ind=16",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=N&Ind=17",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=O&Ind=18",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=P&Ind=19",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=Q&Ind=20",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=R&Ind=21",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=S&Ind=22",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=T&Ind=23",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=U&Ind=24",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=V&Ind=25",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=W&Ind=26",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=X&Ind=27",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=Y&Ind=28"
        ))
    }

    private fun buildMens2016() {
        mensLinks.buildLinksForYear(2016, listOf(
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=B&Ind=4",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=C&Ind=5",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=D&Ind=6",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=E&Ind=7",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=F&Ind=8",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=G&Ind=9",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=H&Ind=10",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=I&Ind=11",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=J&Ind=12",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=K&Ind=13",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=L&Ind=14",
                "https://www.trackshackresults.com/disneysports/results/wdw/wdw16/mar_results.php?Link=43&Type=2&Div=LA&Ind=15"
        ))
    }

    private fun buildWomens2014() {
        womensLinks.buildLinksForYear(2014, listOf(
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=N&Ind=17",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=O&Ind=18",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=P&Ind=19",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=Q&Ind=20",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=R&Ind=21",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=S&Ind=22",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=T&Ind=23",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=U&Ind=24",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=V&Ind=25",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=W&Ind=26",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=X&Ind=27"
        ))
    }

    private fun buildMens2014() {
        mensLinks.buildLinksForYear(2014, listOf(
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=A&Ind=4",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=B&Ind=5",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=C&Ind=6",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=D&Ind=7",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=E&Ind=8",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=F&Ind=9",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=G&Ind=10",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=H&Ind=11",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=I&Ind=12",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=J&Ind=13",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=K&Ind=14",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=L&Ind=15",
                "http://trackshack.com/disneysports/results/wdw/wdw14/mar_results.php?Link=13&Type=2&Div=LA&Ind=16"))
    }

    fun process() : List<CompletableFuture<String>> {
        return try {
            logger.info("Starting Disney Scrape")

            val columnInfo = ColumnPositions(nationality = 12, finishTime = 11, age = 3, place = 4, ageGender = -1)
            womensLinks.forEach { link ->
                if(completedPages.none { it.url == link.url }){
                    threads.add(trackShackResults.scrape(runnerDataQueue, link, Gender.FEMALE, columnInfo))
                }
            }
            mensLinks.forEach { link ->
                if(completedPages.none { it.url == link.url }){
                    threads.add(trackShackResults.scrape(runnerDataQueue, link, Gender.MALE, columnInfo))
                }
            }

            //threads.add(trackShackResults.scrape2014(runnerDataQueue))
            threads.toList()
        } catch (e : Exception){
            logger.error("Los Angelas Marathon Failed", e)
            emptyList()
        }
    }
}

//Verifying Results
@Component
class BudapestProducer(@Autowired private val runnerDataQueue: LinkedBlockingQueue<RunnerData>,
                       @Autowired private val urlPageRepository: UrlPageRepository,
                       @Autowired private val budapestScrape: BudapestScrape) {

    private val logger = LoggerFactory.getLogger(BerlinProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()
    private val links = mutableListOf<UrlPage>()
    private lateinit var pages : List<UrlPage>

    @PostConstruct
    fun init(){
        pages = urlPageRepository.findBySource(MarathonSources.Budapest)

        generateLinks(2014, 4300)
        generateLinks(2015, 5600)
        generateLinks(2016, 4950)
        generateLinks(2017, 5400)
    }

    fun generateLinks(year : Int, max : Int){
        for(i in 0..max step 50){
            val url = "http://results.runinbudapest.com/?start=$i&race=marathon&lt=results&verseny=${year}_spar_e&rajtszam=&nev=&nem=&egyesulet=&varos=&orszag=&min_ido=&max_ido=&min_hely=&max_hely=&oldal=50"
            links.add(UrlPage(source = MarathonSources.Budapest, marathonYear = year, url = url))
        }
    }

    fun process() : List<CompletableFuture<String>> {
        return try {
            logger.info("Starting Budapest Scrape")

            val thirteenColumns = ColumnPositions(place = 0, age = 2, nationality = 3, gender = 6, finishTime = 13)
            val twelveColumns = ColumnPositions(place = 0, age = 2, nationality = 3, gender = 6, finishTime = 12)
            val elevenColumns = ColumnPositions(place = 0, age = 2, nationality = 3, gender = 6, finishTime = 11)

            links.forEach{ page ->
                if(pages.none { page.url == it.url }){
                    when(page.marathonYear){
                        2014 -> threads.add(budapestScrape.scrape(runnerDataQueue, page, twelveColumns))
                        2015 -> threads.add(budapestScrape.scrape(runnerDataQueue, page, elevenColumns))
                        2016 -> threads.add(budapestScrape.scrape(runnerDataQueue, page, elevenColumns))
                        2017 -> threads.add(budapestScrape.scrape(runnerDataQueue, page, thirteenColumns))
                    }
                } else {
                    logger.info("Skipping already completed page: $page")
                }
            }

            threads.toList()
        } catch (e : Exception){
            logger.error("Budapest failed", e)
            emptyList()
        }
    }
}

@Component
class MelbourneProducer(
        @Autowired private val runnerDataQueue: LinkedBlockingQueue<RunnerData>,
        @Autowired private val multisportAustraliaScraper: MultisportAustraliaScraper,
        @Autowired private val urlPageRepository: UrlPageRepository){

    private val logger = LoggerFactory.getLogger(MelbourneProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    private val links = mutableListOf<UrlPage>()
    private lateinit var completedLinks : List<UrlPage>

    @PostConstruct
    fun init(){
        completedLinks = urlPageRepository.findBySource(MarathonSources.Melbourne)
        generateLinks("https://www.multisportaustralia.com.au/home/results?c=1&r=1115&e=1&cul=en-US",
                "https://www.multisportaustralia.com.au/home/results?c=1&r=1115&e=1&pg=", 2014, 2, 129)
        generateLinks("https://www.multisportaustralia.com.au/home/results?c=1&r=1385&e=1&cul=en-US",
                "https://www.multisportaustralia.com.au/home/results?c=1&r=1385&e=1&pg=", 2015, 2, 129)
        generateLinks("https://www.multisportaustralia.com.au/home/results?c=1&r=1589&e=1&cul=en-US",
                "https://www.multisportaustralia.com.au/home/results?c=1&r=1589&e=1&pg=", 2016, 2, 122)
        generateLinks("https://www.multisportaustralia.com.au/m3/event?c=1&r=6228&e=1",
                "https://www.multisportaustralia.com.au/m3/event?c=1&r=6228&e=1&pg=", 2017, 2, 122)
    }

    private fun generateLinks(firstPageUrl: String, url: String, year: Int, start: Int, end: Int) {
        links.add(UrlPage(source = MarathonSources.Melbourne, marathonYear = year, url = firstPageUrl))
        for(i in start .. end){
            links.add(UrlPage(source = MarathonSources.Melbourne, marathonYear = year, url = url + i))
        }
    }

    fun process() : List<CompletableFuture<String>> {
        return try {
            logger.info("Starting Melbourne Scrape")

            val columnPositions2014 = ColumnPositions(place = 0, finishTime = 3, age = 4, gender = 5)
            val columnPositions = ColumnPositions(place = 0, finishTime = 3, nationality = 4, age = 5, gender = 6)

            links.forEach { link ->
                if(completedLinks.none { it.url == link.url }){
                    when(link.marathonYear){
                        2014 -> threads.add(multisportAustraliaScraper.scrape(runnerDataQueue, link, MarathonSources.Melbourne, columnPositions2014))
                        else -> threads.add(multisportAustraliaScraper.scrape(runnerDataQueue, link, MarathonSources.Melbourne, columnPositions))
                    }
                }
            }

            threads.toList()
        } catch (e : Exception){
            logger.error("Melbourne failed", e)
            emptyList()
        }
    }
}