package com.stonesoupprogramming.marathonscrape

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import javax.annotation.PostConstruct

//Complete
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

//Complete
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

//Completed
@Component
class NyMarathonProducer(@Autowired private val urlPageRepository: UrlPageRepository,
                         @Autowired private val marathonGuideScraper: MarathonGuideScraper) {

    private val logger = LoggerFactory.getLogger(NyMarathonProducer::class.java)
    private val threads = mutableListOf<CompletableFuture<String>>()

    private lateinit var completed : List<UrlPage>

    @PostConstruct
    fun init(){
        completed = urlPageRepository.findBySource(MarathonSources.Nyc)
    }

    fun process() : List<CompletableFuture<String>> {
        return try {
            logger.info("Starting New York Scrape")

            val urls = mapOf(
                    2014 to "http://www.marathonguide.com/results/browse.cfm?MIDD=472141102",
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

            val range2014 = marathonGuideScraper.findRangeOptionsForUrl(urls[2014].orEmpty())
            val range2015 = marathonGuideScraper.findRangeOptionsForUrl(urls[2015].orEmpty())
            val range2016 = marathonGuideScraper.findRangeOptionsForUrl(urls[2016].orEmpty())
            val range2017 = marathonGuideScraper.findRangeOptionsForUrl(urls[2017].orEmpty())

            urls.forEach { year, url ->
                when(year){
                    2014 -> {
                        range2014.get().buildThreadsForYear(2014, url, placeFirstPositions)
                    }
                    2015 -> {
                        range2015.get().buildThreadsForYear(2015, url, timeFirstPositions)
                    }
                    2016 -> {
                        range2016.get().buildThreadsForYear(2016, url, timeFirstPositions)
                    }
                    2017 -> {
                        range2017.get().buildThreadsForYear(2017, url, placeFirstPositions)
                    }
                }
            }
            threads.toList()
        } catch (e : Exception){
            logger.error("New York Marathon failed", e)
            emptyList()
        }
    }

    private fun List<String>.buildThreadsForYear(year : Int, url : String, columnPositions: ColumnPositions){
        this.forEach { range ->
            if(completed.none { it.marathonYear == year && it.url == range }){
                threads.add(marathonGuideScraper.scrape(year, url, MarathonSources.Nyc, columnPositions, range))
            } else {
                logger.info("Skipping completed range=$range, year=$year")
            }
        }
    }
}

//Complete
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

//Complete
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

//Complete
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

//Complete
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

//Complete
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

//Complete
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

//Complete
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

//Complete
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

//Complete
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

@Component
class TaipeiProducer(
        @Autowired private val athLinksMarathonScraper: AthLinksMarathonScraper,
        @Autowired private val pagedResultsRepository: PagedResultsRepository){

    private val logger = LoggerFactory.getLogger(TaipeiProducer::class.java)

    private val threads = mutableListOf<CompletableFuture<String>>()
    private var lastPageNum2014 : Int = 0
    private var lastPageNum2015 : Int = 0
    private var lastPageNum2016 : Int = 0
    private var lastPageNum2017 : Int = 0

    @PostConstruct
    private fun init(){
        lastPageNum2014 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Taipei, 2014).maxBy { it.pageNum }?.pageNum ?: 0
        lastPageNum2015 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Taipei, 2015).maxBy { it.pageNum }?.pageNum ?: 0
        lastPageNum2016 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Taipei, 2016).maxBy { it.pageNum }?.pageNum ?: 0
        lastPageNum2017 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Taipei, 2017).maxBy { it.pageNum }?.pageNum ?: 0
    }

    fun process(): List<CompletableFuture<String>> {
        return try {
            logger.info("Starting Taipei Marathon")

            //threads.add(athLinksMarathonScraper.scrape("https://www.athlinks.com/event/34450/results/Event/410756/Course/617603/Results", 2014, MarathonSources.Taipei, lastPageNum2014))
            //threads.add(athLinksMarathonScraper.scrape("https://www.athlinks.com/event/34450/results/Event/512311/Course/669211/Results", 2015, MarathonSources.Taipei, lastPageNum2015))
            //threads.add(athLinksMarathonScraper.scrape("https://www.athlinks.com/event/34450/results/Event/704200/Course/1147895/Results", 2016, MarathonSources.Taipei, lastPageNum2016))
            //threads.add(athLinksMarathonScraper.scrape("https://www.athlinks.com/event/34450/results/Event/701640/Course/1142522/Results", 2017, MarathonSources.Taipei, lastPageNum2017))

            threads.toList()
        } catch (e : Exception){
            logger.error("Failed to start Taipei", e)
            emptyList()
        }
    }
}

@Component
class YuenglingProducer(
        @Autowired private val athLinksMarathonScraper: AthLinksMarathonScraper,
        @Autowired private val pagedResultsRepository: PagedResultsRepository){

    private val logger = LoggerFactory.getLogger(TaipeiProducer::class.java)

    private val threads = mutableListOf<CompletableFuture<String>>()
    private var lastPageNum2014 : Int = 0
    private var lastPageNum2015 : Int = 0
    private var lastPageNum2016 : Int = 0
    private var lastPageNum2017 : Int = 0

    @PostConstruct
    private fun init(){
        lastPageNum2014 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Yuengling, 2014).maxBy { it.pageNum }?.pageNum ?: 0
        if(lastPageNum2014 > 0){
            lastPageNum2014++
        }
        lastPageNum2015 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Yuengling, 2015).maxBy { it.pageNum }?.pageNum ?: 0
        if(lastPageNum2015 > 0){
            lastPageNum2015++
        }
        lastPageNum2016 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Yuengling, 2016).maxBy { it.pageNum }?.pageNum ?: 0
        if(lastPageNum2016 > 0){
            lastPageNum2016++
        }
        lastPageNum2017 = pagedResultsRepository.findBySourceAndMarathonYear(MarathonSources.Yuengling, 2017).maxBy { it.pageNum }?.pageNum ?: 0
        if(lastPageNum2017 > 0){
            lastPageNum2017++
        }
    }

    fun process(): List<CompletableFuture<String>> {
        return try {
            logger.info("Starting Taipei Marathon")

            threads.add(athLinksMarathonScraper.scrape("https://www.athlinks.com/event/3175/results/Event/334125/Course/414365/Results", 2014, MarathonSources.Yuengling, lastPageNum2014))
            threads.add(athLinksMarathonScraper.scrape("https://www.athlinks.com/event/3175/results/Event/429684/Course/644989/Results", 2015, MarathonSources.Yuengling, lastPageNum2015))
            threads.add(athLinksMarathonScraper.scrape("https://www.athlinks.com/event/3175/results/Event/488730/Course/726687/Results", 2016, MarathonSources.Yuengling, lastPageNum2016))
            threads.add(athLinksMarathonScraper.scrape("https://www.athlinks.com/event/3175/results/Event/615660/Course/940547/Results", 2017, MarathonSources.Yuengling, lastPageNum2017))

            threads.toList()
        } catch (e : Exception){
            logger.error("Failed to start Taipei", e)
            emptyList()
        }
    }
}

@Component
class BerlinProducer(@Autowired private val athLinksMarathonScraper: AthLinksMarathonScraper,
                     @Autowired private val pagedResultsRepository: PagedResultsRepository) : BaseProducer(LoggerFactory.getLogger(BerlinProducer::class.java), MarathonSources.Berlin) {

    private var lastPageNum2014 : Int = 0
    private var lastPageNum2015 : Int = 0
    private var lastPageNum2016 : Int = 0
    private var lastPageNum2017 : Int = 0

    @PostConstruct
    private fun init(){
        lastPageNum2014 = pagedResultsRepository.findBySourceAndMarathonYear(marathonSources, 2014).maxBy { it.pageNum }?.pageNum ?: 0
        if(lastPageNum2014 > 0){
            lastPageNum2014++
        }
        lastPageNum2015 = pagedResultsRepository.findBySourceAndMarathonYear(marathonSources, 2015).maxBy { it.pageNum }?.pageNum ?: 0
        if(lastPageNum2015 > 0){
            lastPageNum2015++
        }
        lastPageNum2016 = pagedResultsRepository.findBySourceAndMarathonYear(marathonSources, 2016).maxBy { it.pageNum }?.pageNum ?: 0
        if(lastPageNum2016 > 0){
            lastPageNum2016++
        }
        lastPageNum2017 = pagedResultsRepository.findBySourceAndMarathonYear(marathonSources, 2017).maxBy { it.pageNum }?.pageNum ?: 0
        if(lastPageNum2017 > 0){
            lastPageNum2017++
        }
    }

    override fun buildThreads() {
        threads.add(athLinksMarathonScraper.scrape("https://www.athlinks.com/event/34448/results/Event/358856/Course/523662/Results", 2014, marathonSources, lastPageNum2014))
        threads.add(athLinksMarathonScraper.scrape("https://www.athlinks.com/event/34448/results/Event/406759/Course/609347/Results", 2015, marathonSources, lastPageNum2015))
        threads.add(athLinksMarathonScraper.scrape("https://www.athlinks.com/event/34448/results/Event/488569/Course/726394/Results", 2016, marathonSources, lastPageNum2016))
        threads.add(athLinksMarathonScraper.scrape("https://www.athlinks.com/event/34448/results/Event/602229/Course/911221/Results", 2017, marathonSources, lastPageNum2017))
    }
}

abstract class BaseAthProducer(private val athLinksMarathonScraper: AthLinksMarathonScraper,
                      private val pagedResultsRepository: PagedResultsRepository,
                      logger : Logger,
                      marathonSources: MarathonSources, private val urls : Map<Int, String>) : BaseProducer(logger, marathonSources){

    private var lastPageNum2014 : Int = 0
    private var lastPageNum2015 : Int = 0
    private var lastPageNum2016 : Int = 0
    private var lastPageNum2017 : Int = 0
    private var lastPageNum2018 : Int = 0

    @PostConstruct
    private fun init(){
        lastPageNum2014 = pagedResultsRepository.findBySourceAndMarathonYear(marathonSources, 2014).maxBy { it.pageNum }?.pageNum ?: 0
        if(lastPageNum2014 > 0){
            lastPageNum2014++
        }
        lastPageNum2015 = pagedResultsRepository.findBySourceAndMarathonYear(marathonSources, 2015).maxBy { it.pageNum }?.pageNum ?: 0
        if(lastPageNum2015 > 0){
            lastPageNum2015++
        }
        lastPageNum2016 = pagedResultsRepository.findBySourceAndMarathonYear(marathonSources, 2016).maxBy { it.pageNum }?.pageNum ?: 0
        if(lastPageNum2016 > 0){
            lastPageNum2016++
        }
        lastPageNum2017 = pagedResultsRepository.findBySourceAndMarathonYear(marathonSources, 2017).maxBy { it.pageNum }?.pageNum ?: 0
        if(lastPageNum2017 > 0){
            lastPageNum2017++
        }
        lastPageNum2018 = pagedResultsRepository.findBySourceAndMarathonYear(marathonSources, 2018).maxBy { it.pageNum }?.pageNum ?: 0
        if(lastPageNum2018 > 0){
            lastPageNum2018++
        }
    }

    override fun buildThreads() {
        urls.forEach { year, url ->
            when (year) {
                2014 -> threads.add(athLinksMarathonScraper.scrape(url, year, marathonSources, lastPageNum2014))
                2015 -> threads.add(athLinksMarathonScraper.scrape(url, year, marathonSources, lastPageNum2015))
                2016 -> threads.add(athLinksMarathonScraper.scrape(url, year, marathonSources, lastPageNum2016))
                2017 -> threads.add(athLinksMarathonScraper.scrape(url, year, marathonSources, lastPageNum2017))
                2018 -> threads.add(athLinksMarathonScraper.scrape(url, year, marathonSources, lastPageNum2018))
            }
        }
    }
}

@Component
class JeruselmProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                       @Autowired pagedResultsRepository: PagedResultsRepository) : BaseAthProducer(athLinksMarathonScraper, pagedResultsRepository,
        LoggerFactory.getLogger(JeruselmProducer::class.java),
        MarathonSources.Jeruselm,
        mapOf(2014 to "https://www.athlinks.com/event/34617/results/Event/374111/Course/480660/Results",
                2015 to "https://www.athlinks.com/event/34617/results/Event/428801/Course/644981/Results",
                2016 to "https://www.athlinks.com/event/34617/results/Event/504502/Course/750003/Results",
                2017 to "https://www.athlinks.com/event/34617/results/Event/622847/Course/954745/Results"))

@Component
class EversourceHartfordProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                       @Autowired pagedResultsRepository: PagedResultsRepository) : BaseAthProducer(athLinksMarathonScraper, pagedResultsRepository,
        LoggerFactory.getLogger(EversourceHartfordProducer::class.java),
        MarathonSources.Eversource,
        mapOf(2014 to "https://www.athlinks.com/event/1581/results/Event/398994/Course/596968/Results",
                2015 to "https://www.athlinks.com/event/1581/results/Event/409547/Course/638361/Results",
                2016 to "https://www.athlinks.com/event/1581/results/Event/591319/Course/746308/Results",
                2017 to "https://www.athlinks.com/event/1581/results/Event/595523/Course/898185/Results"))

abstract class BaseProducer(protected val logger : Logger, protected val marathonSources: MarathonSources) {

    protected val threads = mutableListOf<CompletableFuture<String>>()

    fun process() : List<CompletableFuture<String>> {
        return try {
            logger.info("Starting $marathonSources")

            buildThreads()

            threads.toList()
        } catch (e : Exception){
            logger.error("Failed to start $marathonSources", e)
            emptyList()
        }
    }

    protected abstract fun buildThreads()
}

@Component
class HonoluluProducer(@Autowired private val pacificSportScraper: PacificSportScraper,
                       @Autowired private val marathonGuideScraper: MarathonGuideScraper,
                       @Autowired private val urlPageRepository: UrlPageRepository):
        BaseProducer(LoggerFactory.getLogger(HonoluluProducer::class.java), MarathonSources.Honolulu) {

    private lateinit var completedPages : List<UrlPage>
    private val scrapeInfoList = mutableListOf<UrlScrapeInfo>()

    @PostConstruct
    fun init(){
        completedPages = urlPageRepository.findBySource(this.marathonSources)
    }

    override fun buildThreads() {
        scrapeInfoList.addAll(buildUrlScrapeInfoGrayPage("https://pseresults.com/events/647/results/794?c0=&c1=&c2=&name=&city=&bib=&sort%5B%5D=0&sort%5B%5D=2&sort%5B%5D=4&sort%5B%5D=5&page=&per_page=1000",
                1, 22, 2014, ColumnPositions(place = 0, finishTime = 2, nationality = 7, ageGender = 8, halfwayTime = 14)))

        scrapeInfoList.addAll(buildUrlScrapeInfoBluePage("http://live.pseresults.com/e/134#/results/A/",
                start = 1, end = 863, year = 2015, columnPositions = ColumnPositions(place = 1, finishTime = 5, ageGender = 7, halfwayTime = 11, nationality = 3)))

        scrapeInfoList.addAll(buildUrlScrapeInfoGrayPage("https://www.pseresults.com/events/851/results/1163?c0=&name=&bib=&sort[]=1&sort[]=0&sort[]=7&sort[]=6&page=&per_page=1000",
                1, 22, 2016, ColumnPositions(finishTime = 0, place = 1, ageGender = 4, nationality = 9, halfwayTime = 13)))

        val range2017 = marathonGuideScraper.findRangeOptionsForUrl("http://www.marathonguide.com/results/browse.cfm?MIDD=480171210")
        val scrape2017 = range2017.get().map { it ->
            val columnPositions = ColumnPositions(ageGender = 4, finishTime = 1, place = 2)
            UrlScrapeInfo(url = "http://www.marathonguide.com/results/browse.cfm?MIDD=480171210", source = marathonSources,
                    marathonYear =  2017, columnPositions = columnPositions, rangeOptions = it) }

        scrapeInfoList
                .filter { si -> completedPages.none { cp -> cp.url == si.url } }
                .forEach { threads.add(pacificSportScraper.scrape(it)) }

        scrape2017
                .filter { s -> completedPages.none { cp -> cp.url == (s.url + ", " + s.rangeOptions) } }
                .forEach { threads.add(marathonGuideScraper.scrape(it)) }
    }

    private fun buildUrlScrapeInfoGrayPage(url : String, start: Int, end : Int, year : Int, columnPositions: ColumnPositions) : List<UrlScrapeInfo> {
        val scrapeInfoList = mutableListOf<UrlScrapeInfo>()
        for(i in start .. end){
            scrapeInfoList.add(UrlScrapeInfo(url.replace("&page=", "&page=$i"), marathonSources, year, columnPositions, ".data_table > table:nth-child(4) > tbody:nth-child(2)"))
        }
        return scrapeInfoList.toList()
    }

    private fun buildUrlScrapeInfoBluePage(url : String, start: Int, end: Int, year: Int, columnPositions: ColumnPositions) : List<UrlScrapeInfo> {
        val scrapeInfoList = mutableListOf<UrlScrapeInfo>()
        for(i in start .. end){
            scrapeInfoList.add(UrlScrapeInfo(url + i, marathonSources, year, columnPositions, ".result_table > tbody:nth-child(2)"))
        }
        return scrapeInfoList.toList()
    }
}