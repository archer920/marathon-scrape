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
                        "span.noprint:nth-child(2) > center:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(12) > a:nth-child(1)",
                        "span.noprint:nth-child(2) > center:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(3) > a:nth-child(1)",
                        ".i > tbody:nth-child(1)",
                        comboBoxSelector = "select[name=on]",
                        comboBoxValue = "TCS Amsterdam Marathon")
                threads.add(tcsAmsterdamScraper.scrape(pagedResultsScrapeInfo))
            }
        }
    }
}