package com.stonesoupprogramming.marathonscrape

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import javax.annotation.PostConstruct

abstract class BaseProducer(private val logger : Logger, protected val marathonSources: MarathonSources) {

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

abstract class BaseUrlPageProducer(logger: Logger, marathonSources: MarathonSources, private val urlPageRepository: UrlPageRepository) :
    BaseProducer(logger, marathonSources){

    private lateinit var completePages : List<UrlPage>

    @PostConstruct
    fun init(){
        completePages = urlPageRepository.findBySource(marathonSources)
    }

    protected fun filterCompleted(inputs : List<UrlPage>) : List<UrlPage> =
            inputs.filter { it -> completePages.none { cp -> it.url == cp.url }}
}

abstract class BaseResultPageProducer(logger: Logger, marathonSources: MarathonSources, private val pagedResultsRepository: PagedResultsRepository)
    : BaseProducer(logger, marathonSources) {

    protected var lastPageNum2014 : Int = 0
    protected var lastPageNum2015 : Int = 0
    protected var lastPageNum2016 : Int = 0
    protected var lastPageNum2017 : Int = 0
    protected var lastPageNum2018 : Int = 0

    @PostConstruct
    private fun init(){
        lastPageNum2014 = pagedResultsRepository.findBySourceAndMarathonYear(marathonSources, 2014).maxBy { it.pageNum }?.pageNum ?: 0
        lastPageNum2014++

        lastPageNum2015 = pagedResultsRepository.findBySourceAndMarathonYear(marathonSources, 2015).maxBy { it.pageNum }?.pageNum ?: 0
        lastPageNum2015++

        lastPageNum2016 = pagedResultsRepository.findBySourceAndMarathonYear(marathonSources, 2016).maxBy { it.pageNum }?.pageNum ?: 0
        lastPageNum2016++

        lastPageNum2017 = pagedResultsRepository.findBySourceAndMarathonYear(marathonSources, 2017).maxBy { it.pageNum }?.pageNum ?: 0
        lastPageNum2017++

        lastPageNum2018 = pagedResultsRepository.findBySourceAndMarathonYear(marathonSources, 2018).maxBy { it.pageNum }?.pageNum ?: 0
        lastPageNum2018++
    }

    override fun buildThreads() {
        buildYearlyThreads(2014, lastPageNum2014)
        buildYearlyThreads(2015, lastPageNum2015)
        buildYearlyThreads(2016, lastPageNum2016)
        buildYearlyThreads(2017, lastPageNum2017)
        buildYearlyThreads(2018, lastPageNum2018)
    }

    abstract fun buildYearlyThreads(year: Int, lastPage: Int)
}

abstract class BaseCategoryPageProducer(logger: Logger, marathonSources: MarathonSources, private val categoryResultsRepository: CategoryResultsRepository)
    : BaseProducer(logger, marathonSources) {

    private lateinit var completedCategories : List<CategoryResults>

    @PostConstruct
    fun init(){
        completedCategories = categoryResultsRepository.findBySource(marathonSources)
    }

    protected fun filterCompleted(inputs: List<CategoryScrapeInfo>) : List<CategoryScrapeInfo> =
            inputs.filter { it -> completedCategories.none { cr -> cr.matches(it) } }
}

abstract class BaseAthProducer(private val athLinksMarathonScraper: AthLinksMarathonScraper,
                               pagedResultsRepository: PagedResultsRepository,
                               logger : Logger,
                               marathonSources: MarathonSources,
                               private val urls : Map<Int, String>,
                               private val endPages : Map<Int, Int>)
    : BaseResultPageProducer(logger, marathonSources, pagedResultsRepository){

    private var scrapeInfo2014 : PagedResultsScrapeInfo? = null
    private var scrapeInfo2015 : PagedResultsScrapeInfo? = null
    private var scrapeInfo2016 : PagedResultsScrapeInfo? = null
    private var scrapeInfo2017 : PagedResultsScrapeInfo? = null
    private var scrapeInfo2018 : PagedResultsScrapeInfo? = null

    @PostConstruct
    private fun init(){
        val backwardsSelector = "#pager > div:nth-child(1) > div:nth-child(1) > button:nth-child(1)"
        val firstNextSelector = "#pager > div:nth-child(1) > div:nth-child(6) > button:nth-child(1)"
        val secondNextSelector = "#pager > div:nth-child(1) > div:nth-child(7) > button:nth-child(1)"

        urls[2014]?.let {url ->
            endPages[2014]?.let { endPage ->
                scrapeInfo2014 = PagedResultsScrapeInfo(url, marathonSources, 2014, ColumnPositions(), lastPageNum2014, endPage, firstNextSelector, backwardsSelector, "", secondNextPageSelector = secondNextSelector)
            }
        }
        urls[2015]?.let { url ->
            endPages[2015]?.let { endPage ->
                scrapeInfo2015 = PagedResultsScrapeInfo(url, marathonSources, 2015, ColumnPositions(), lastPageNum2015, endPage, firstNextSelector, backwardsSelector, "", secondNextPageSelector = secondNextSelector)
            }
        }
        urls[2016]?.let {url ->
            endPages[2016]?.let { endPage ->
                scrapeInfo2016 = PagedResultsScrapeInfo(url, marathonSources, 2016, ColumnPositions(), lastPageNum2016, endPage, firstNextSelector, backwardsSelector, "", secondNextPageSelector = secondNextSelector)
            }
        }
        urls[2017]?.let {url ->
            endPages[2017]?.let { endPage ->
                scrapeInfo2017 = PagedResultsScrapeInfo(url, marathonSources, 2017, ColumnPositions(), lastPageNum2017, endPage, firstNextSelector, backwardsSelector, "", secondNextPageSelector = secondNextSelector)
            }
        }
        urls[2018]?.let {url ->
            endPages[2018]?.let { endPage ->
                scrapeInfo2018 = PagedResultsScrapeInfo(url, marathonSources, 2018, ColumnPositions(), lastPageNum2018, endPage, firstNextSelector, backwardsSelector, "", secondNextPageSelector = secondNextSelector)
            }
        }
    }

    override fun buildYearlyThreads(year: Int, lastPage: Int) {
        when(year) {
            2014 -> {
                scrapeInfo2014?.let {
                    threads.add(athLinksMarathonScraper.scrape(it))
                }
            }
            2015 -> {
                scrapeInfo2015?.let {
                    threads.add(athLinksMarathonScraper.scrape(it))
                }
            }
            2016 -> {
                scrapeInfo2016?.let {
                    threads.add(athLinksMarathonScraper.scrape(it))
                }
            }
            2017 -> {
                scrapeInfo2017?.let {
                    threads.add(athLinksMarathonScraper.scrape(it))
                }
            }
            2018 -> {
                scrapeInfo2018?.let {
                    threads.add(athLinksMarathonScraper.scrape(it))
                }
            }
        }
    }
}

@Component
class StockholmProducer(@Autowired categoryResultsRepository: CategoryResultsRepository,
                        @Autowired private val registrationMarathonScraper: RegistrationMarathonScraper)
    : BaseCategoryPageProducer(LoggerFactory.getLogger(StockholmProducer::class.java), MarathonSources.Stockholm, categoryResultsRepository){

    private val categoryScrapeInfoList = mutableListOf<CategoryScrapeInfo>()
    private val columnPositions = ColumnPositions(place = 0, age = 2, nationality = 3, finishTime = 4)
    private val extendedColumnPositions = ColumnPositions(place = 1, age = 3, nationality = 4, finishTime = 5)

    init {
        buildCategories(2014, 1, 48, Gender.MALE, "ASICS Stockholm Marathon 2014")
        buildCategories(2014, 50, 66, Gender.FEMALE, "ASICS Stockholm Marathon 2014")

        buildCategories(2015, 1, 44, Gender.MALE, "ASICS Stockholm Marathon 2015")
        buildCategories(2015, 46, 61, Gender.FEMALE, "ASICS Stockholm Marathon 2015")

        buildCategories(2016, 1, 38, Gender.MALE, "ASICS Stockholm Marathon 2016")
        buildCategories(2016, 40, 53, Gender.FEMALE, "ASICS Stockholm Marathon 2016")

        buildCategories(2017, 1, 37, Gender.MALE, "ASICS Stockholm Marathon 2017")
        buildCategories(2017, 39, 53, Gender.FEMALE, "ASICS Stockholm Marathon 2017")

        buildCategories(2018, 1, 41, Gender.MALE, "ASICS Stockholm Marathon 2018")
        buildCategories(2018, 43, 59, Gender.FEMALE, "ASICS Stockholm Marathon 2018")
    }

    private fun buildCategories(year : Int, start: Int, end : Int, gender: Gender, raceSelection : String) {
        for(i in start .. end){
            val selector = if(i < 10){
                "#ctl00_HeaderContentPlaceholder_RegistrationResult_ShortCutRepeater_ctl0${i}_ShortCutLinkButton"
            } else {
                "#ctl00_HeaderContentPlaceholder_RegistrationResult_ShortCutRepeater_ctl${i}_ShortCutLinkButton"
            }
            val positions = if(year < 2017){
                columnPositions
            } else {
                extendedColumnPositions
            }
            categoryScrapeInfoList.add(CategoryScrapeInfo("https://registration.marathongruppen.se/ResultList.aspx?LanguageCode=en&RaceId=51",
                    marathonSources, year, positions, selector, gender, raceSelection))
        }
    }

    override fun buildThreads() {
        filterCompleted(categoryScrapeInfoList).forEach { it ->
            threads.add(registrationMarathonScraper.scrape(it))
        }
    }
}

@Component
class TcsAmsterdamProducer(@Autowired pagedResultsRepository: PagedResultsRepository,
                           @Autowired private val tcsAmsterdamScraper: TcsAmsterdamScraper)
    : BaseResultPageProducer(LoggerFactory.getLogger(TcsAmsterdamProducer::class.java), MarathonSources.Amsterdam, pagedResultsRepository){

    override fun buildYearlyThreads(year: Int, lastPage: Int) {

        when(year){
            2014 -> {
                val columnPositions = ColumnPositions(place = 0, nationality = 4, ageGender = 6, finishTime = 8)
                val pagedResultsScrapeInfo = PagedResultsScrapeInfo("http://evenementen.uitslagen.nl/2014/amsterdammarathon/index-en.html",
                        marathonSources, year, columnPositions, lastPage,
                        0, //FIXME
                        "span.noprint:nth-child(2) > center:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(12) > a:nth-child(1)",
                        "span.noprint:nth-child(2) > center:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(3) > a:nth-child(1)",
                        ".i > tbody:nth-child(1)",
                        tableFrame = "frame[name=uinfo]",
                        comboBoxSelector = "select[name=on]",
                        comboBoxValue = "TCS Amsterdam Marathon",
                        comboBoxFrame = "frame[name=umenu]")
                threads.add(tcsAmsterdamScraper.scrape(pagedResultsScrapeInfo))
            }
        }
    }
}

@Component
class SantiagoProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                       @Autowired pagedResultsRepository: PagedResultsRepository)
    : BaseAthProducer(
        athLinksMarathonScraper,
        pagedResultsRepository,
        LoggerFactory.getLogger(SantiagoProducer::class.java),
        MarathonSources.Santiago,
        mapOf(2014 to "https://www.athlinks.com/event/34489/results/Event/350570/Course/512372/Results",
                2015 to "https://www.athlinks.com/event/34489/results/Event/433872/Course/651614/Results",
                2016 to "https://www.athlinks.com/event/34489/results/Event/533858/Course/793289/Results",
                2017 to "https://www.athlinks.com/event/34489/results/Event/634661/Course/978409/Results"),
        mapOf(2014 to 74, 2015 to 91, 2016 to 93, 2017 to 93))

@Component
class CopenhagenProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                         @Autowired pagedResultsRepository: PagedResultsRepository)
    :BaseAthProducer(
        athLinksMarathonScraper,
        pagedResultsRepository,
        LoggerFactory.getLogger(CopenhagenProducer::class.java),
        MarathonSources.Copenhagen,
        mapOf(2014 to "https://www.athlinks.com/event/34641/results/Event/325403/Course/502513/Results",
                2015 to "https://www.athlinks.com/event/34641/results/Event/397848/Course/596378/Results",
                2016 to "https://www.athlinks.com/event/34641/results/Event/546820/Course/812974/Results",
                2017 to "https://www.athlinks.com/event/34641/results/Event/643352/Course/1001712/Results"),
        mapOf(2014 to 193, 2015 to 185, 2016 to 168, 2017 to 164))

@Component
class RheinEnergieProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                       @Autowired pagedResultsRepository: PagedResultsRepository)
    : BaseAthProducer(
        athLinksMarathonScraper,
        pagedResultsRepository,
        LoggerFactory.getLogger(RheinEnergieProducer::class.java),
        MarathonSources.RheinEnergie,
        mapOf(2014 to "https://www.athlinks.com/event/100584/results/Event/366315/Course/325711/Results",
                2015 to "https://www.athlinks.com/event/100584/results/Event/485015/Course/721218/Results",
                2016 to "https://www.athlinks.com/event/100584/results/Event/586267/Course/880800/Results",
                2017 to "https://www.athlinks.com/event/100584/results/Event/677031/Course/1088092/Results"),
        mapOf(2014 to 80, 2015 to 88, 2016 to 102, 2017 to 91))

@Component
class BournemouthProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                           @Autowired pagedResultsRepository: PagedResultsRepository)
    : BaseAthProducer(
        athLinksMarathonScraper,
        pagedResultsRepository,
        LoggerFactory.getLogger(BournemouthProducer::class.java),
        MarathonSources.Bournemouth,
        mapOf(2014 to "https://www.athlinks.com/event/92529/results/Event/393634/Course/504521/Results",
                2015 to "https://www.athlinks.com/event/92529/results/Event/484469/Course/720526/Results",
                2016 to "https://www.athlinks.com/event/92529/results/Event/536423/Course/796931/Results",
                2017 to "https://www.athlinks.com/event/92529/results/Event/655684/Course/1025408/Results"),
        mapOf(2014 to 40, 2015 to 38, 2016 to 41, 2017 to 41))

@Component
class StJudeProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                          @Autowired pagedResultsRepository: PagedResultsRepository)
    : BaseAthProducer(
        athLinksMarathonScraper,
        pagedResultsRepository,
        LoggerFactory.getLogger(StJudeProducer::class.java),
        MarathonSources.Memphis,
        mapOf(2014 to "https://www.athlinks.com/event/3403/results/Event/408060/Course/565915/Results",
                2015 to "https://www.athlinks.com/event/3403/results/Event/500268/Course/730979/Results",
                2016 to "https://www.athlinks.com/event/3403/results/Event/513413/Course/763475/Results",
                2017 to "https://www.athlinks.com/event/3403/results/Event/692380/Course/1113189/Results"),
        mapOf(2014 to 54, 2015 to 51, 2016 to 51, 2017 to 47))