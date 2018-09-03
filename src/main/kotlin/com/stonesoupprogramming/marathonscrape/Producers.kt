package com.stonesoupprogramming.marathonscrape

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import javax.annotation.PostConstruct

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

abstract class BaseUrlPageProducer(logger: Logger, marathonSources: MarathonSources, private val urlPageRepository: UrlPageRepository) :
    BaseProducer(logger, marathonSources){

    private lateinit var completePages : List<UrlPage>

    @PostConstruct
    fun init(){
        completePages = urlPageRepository.findBySource(marathonSources)
    }

    protected fun filterCompleted(inputs : List<UrlScrapeInfo>) : List<UrlScrapeInfo> =
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
                scrapeInfo2014 = PagedResultsScrapeInfo(url, marathonSources, 2014, ColumnPositions(), lastPageNum2014, endPage, lastPageNum2014, firstNextSelector, backwardsSelector, "", secondNextPageSelector = secondNextSelector)
            }
        }
        urls[2015]?.let { url ->
            endPages[2015]?.let { endPage ->
                scrapeInfo2015 = PagedResultsScrapeInfo(url, marathonSources, 2015, ColumnPositions(), lastPageNum2015, endPage, lastPageNum2015, firstNextSelector, backwardsSelector, "", secondNextPageSelector = secondNextSelector)
            }
        }
        urls[2016]?.let {url ->
            endPages[2016]?.let { endPage ->
                scrapeInfo2016 = PagedResultsScrapeInfo(url, marathonSources, 2016, ColumnPositions(), lastPageNum2016, endPage, lastPageNum2016, firstNextSelector, backwardsSelector, "", secondNextPageSelector = secondNextSelector)
            }
        }
        urls[2017]?.let {url ->
            endPages[2017]?.let { endPage ->
                scrapeInfo2017 = PagedResultsScrapeInfo(url, marathonSources, 2017, ColumnPositions(), lastPageNum2017, endPage, lastPageNum2017, firstNextSelector, backwardsSelector, "", secondNextPageSelector = secondNextSelector)
            }
        }
        urls[2018]?.let {url ->
            endPages[2018]?.let { endPage ->
                scrapeInfo2018 = PagedResultsScrapeInfo(url, marathonSources, 2018, ColumnPositions(), lastPageNum2018, endPage, lastPageNum2018, firstNextSelector, backwardsSelector, "", secondNextPageSelector = secondNextSelector)
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

//FIXME: Does not conform to current architecture
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
                    marathonSources, year, positions, selector, gender, raceSelection, "", true))
        }
    }

    override fun buildThreads() {
        filterCompleted(categoryScrapeInfoList).forEach { it ->
            threads.add(registrationMarathonScraper.scrape(it))
        }
    }
}

@Component
class TcsAmsterdamProducer(@Autowired urlPageRepository: UrlPageRepository,
                           @Autowired pagedResultsRepository: PagedResultsRepository,
                           @Autowired private val evenementenUitslagenScraper: EvenementenUitslagenScraper)
    : BaseProducer(LoggerFactory.getLogger(TcsAmsterdamProducer::class.java), MarathonSources.Amsterdam){

    val urlProducer = object : BaseUrlPageProducer(this.logger, marathonSources, urlPageRepository) {
        private val urls2014 = mutableListOf<UrlScrapeInfo>()
        private val urls2015 = mutableListOf<UrlScrapeInfo>()

        init {
            val columnPositions = ColumnPositions(place = 0, nationality = 4, ageGender = 6, finishTime = 8)
            val url2014 = "http://evenementen.uitslagen.nl/2014/amsterdammarathon/uitslag.php?on=1&ct=&p=&tl=en"
            buildUrlScrapeInfo(urls2014, columnPositions, url2014, 2014,
                    "Msen" to 26, "M35" to 16, "M40" to 19,
                    "M45" to 16, "M50" to 11, "M55" to 6,
                    "M60" to 3, "M65" to 1,"M70" to 1,
                    "M75" to 1, "Vsen" to 10,  "V35" to 5,
                    "V40" to 5,  "V45" to 5, "V50" to 3,
                    "V55" to 2,  "V60" to 1,  "V65" to 1)

            val url2015 = "http://evenementen.uitslagen.nl/2015/amsterdammarathon/uitslag.php?on=1&ct=&p=&tl=en"
            buildUrlScrapeInfo(urls2015, columnPositions, url2015, 2015,
                    "Msen" to 72, "M35" to 16, "M40" to 18,
                    "M45" to 17, "M50" to 12, "M55" to 7,
                    "M60" to 3, "M65" to 1, "M70" to 1,
                    "M75" to 1, "Vsen" to 10,  "V35" to 4,
                    "V40" to 5,  "V45" to 5, "V50" to 3,
                    "V55" to 2,  "V60" to 1,  "V65" to 1)
        }

        private fun buildUrlScrapeInfo(list: MutableList<UrlScrapeInfo>, columnPositions: ColumnPositions, url: String, year: Int, vararg pair: Pair<String, Int>) {
            pair.forEach { p ->  list.addAll(buildUrls(url, p.first, p.second, year, columnPositions))}
        }

        private fun buildUrls(url : String, category : String, lastPage: Int, year : Int, columnPositions: ColumnPositions) : List<UrlScrapeInfo> {
            val mutableList = mutableListOf<UrlScrapeInfo>()
            for(i in 1 .. lastPage){
                mutableList.add(UrlScrapeInfo(url = url.replace("ct=", "ct=$category").replace("p=", "p=$i"),
                        marathonSources = marathonSources,
                        marathonYear = year,
                        columnPositions = columnPositions,
                        tableBodySelector = ".i > tbody:nth-child(1)",
                        headerRow = true))
            }
            return mutableList.toList()
        }

        override fun buildThreads() {
            filterCompleted(urls2014).forEach { it -> threads.add(evenementenUitslagenScraper.scrape(it)) }
            filterCompleted(urls2015).forEach { it -> threads.add(evenementenUitslagenScraper.scrape(it)) }
        }
    }

    val pagedProducer = object : BaseResultPageProducer(this.logger, marathonSources, pagedResultsRepository) {

        override fun buildYearlyThreads(year: Int, lastPage: Int) {
            when(year){
                //2016 -> threads.addAll()
            }
        }
    }

    @PostConstruct
    fun initialize(){
        urlProducer.init()
    }

    override fun buildThreads() {
        threads.addAll(urlProducer.process())
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
class GenevaProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                         @Autowired pagedResultsRepository: PagedResultsRepository)
     : BaseAthProducer(
     	athLinksMarathonScraper,
     	pagedResultsRepository,
     	LoggerFactory.getLogger(GenevaProducer::class.java),
        MarathonSources.Geneva,
        mapOf(2014 to "https://www.athlinks.com/event/34908/results/Event/328309/Course/475398/Results",
                2015 to "https://www.athlinks.com/event/34908/results/Event/443754/Course/658734/Results",
                2016 to "https://www.athlinks.com/event/34908/results/Event/605457/Course/803503/Results",
                2017 to "https://www.athlinks.com/event/34908/results/Event/620452/Course/1015613/Results"),
        mapOf(2014 to 31, 2015 to 31, 2016 to 30, 2017 to 37))

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
class BayshoreProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                          @Autowired pagedResultsRepository: PagedResultsRepository)
    : BaseAthProducer(
        athLinksMarathonScraper,
        pagedResultsRepository,
        LoggerFactory.getLogger(BayshoreProducer::class.java),
        MarathonSources.Bayshore,
        mapOf(2014 to "https://www.athlinks.com/event/3943/results/Event/320494/Course/567321/Results",
                2015 to "https://www.athlinks.com/event/3943/results/Event/406103/Course/668148/Results",
                2016 to "https://www.athlinks.com/event/3943/results/Event/452343/Course/675329/Results",
                2017 to "https://www.athlinks.com/event/3943/results/Event/599600/Course/1003920/Results"),
        mapOf(2014 to 41, 2015 to 41, 2016 to 41, 2017 to 35))

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

@Component
class VeniceProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                     @Autowired pagedResultsRepository: PagedResultsRepository)
    : BaseAthProducer(
        athLinksMarathonScraper,
        pagedResultsRepository,
        LoggerFactory.getLogger(VeniceProducer::class.java),
        MarathonSources.Venice,
        mapOf(2014 to "https://www.athlinks.com/event/35465/results/Event/409660/Course/615995/Results",
                2015 to "https://www.athlinks.com/event/143495/results/Event/510979/Course/759749/Results",
                2016 to "https://www.athlinks.com/event/143495/results/Event/584828/Course/878087/Results",
                2017 to "https://www.athlinks.com/event/143495/results/Event/617382/Course/942051/Results"),
        mapOf(2014 to 108, 2015 to 133, 2016 to 93, 2017 to 119))

@Component
class RouteSixSixProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                     @Autowired pagedResultsRepository: PagedResultsRepository)
    : BaseAthProducer(
        athLinksMarathonScraper,
        pagedResultsRepository,
        LoggerFactory.getLogger(RouteSixSixProducer::class.java),
        MarathonSources.Route66,
        mapOf(2014 to "https://www.athlinks.com/event/23791/results/Event/406936/Course/611434/Results",
                2015 to "https://www.athlinks.com/event/23791/results/Event/497737/Course/644783/Results",
                2016 to "https://www.athlinks.com/event/23791/results/Event/601598/Course/909913/Results",
                2017 to "https://www.athlinks.com/event/23791/results/Event/602207/Course/911175/Results"),
        mapOf(2014 to 34, 2015 to 52, 2016 to 30, 2017 to 32))

@Component
class IndianapolisProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                     @Autowired pagedResultsRepository: PagedResultsRepository)
    : BaseAthProducer(
        athLinksMarathonScraper,
        pagedResultsRepository,
        LoggerFactory.getLogger(IndianapolisProducer::class.java),
        MarathonSources.Indianapolis,
        mapOf(2014 to "https://www.athlinks.com/event/20222/results/Event/403001/Course/604834/Results",
                2015 to "https://www.athlinks.com/event/20222/results/Event/424695/Course/732359/Results",
                2016 to "https://www.athlinks.com/event/20222/results/Event/536281/Course/796710/Results",
                2017 to "https://www.athlinks.com/event/20222/results/Event/607772/Course/921921/Results"),
        mapOf(2014 to 75, 2015 to 81, 2016 to 84, 2017 to 94))

@Component
class MunchenProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                           @Autowired pagedResultsRepository: PagedResultsRepository)
    : BaseAthProducer(
        athLinksMarathonScraper,
        pagedResultsRepository,
        LoggerFactory.getLogger(MunchenProducer::class.java),
        MarathonSources.Munchen,
        mapOf(2014 to "https://www.athlinks.com/event/34524/results/Event/360545/Course/600937/Results",
                2015 to "https://www.athlinks.com/event/34524/results/Event/485084/Course/721349/Results",
                2016 to "https://www.athlinks.com/event/34524/results/Event/539699/Course/801925/Results",
                2017 to "https://www.athlinks.com/event/34524/results/Event/679518/Course/1088010/Results"),
        mapOf(2014 to 125, 2015 to 119, 2016 to 98, 2017 to 88))

@Component
class FargoProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
			@Autowired pagedResultsRepository: PagedResultsRepository)
: BaseAthProducer(
        athLinksMarathonScraper,
        pagedResultsRepository,
        LoggerFactory.getLogger(FargoProducer::class.java),
        MarathonSources.Fargo,
        mapOf(2014 to "https://www.athlinks.com/event/21780/results/Event/313589/Course/497735/Results",
                2015 to "https://www.athlinks.com/event/21780/results/Event/444056/Course/664823/Results",
                2016 to "https://www.athlinks.com/event/21780/results/Event/544842/Course/706230/Results",
                2017 to "https://www.athlinks.com/event/21780/results/Event/603348/Course/913426/Results"),
        mapOf(2014 to 34, 2015 to 31, 2016 to 30, 2017 to 27))
        
@Component
class BrightonProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                      @Autowired pagedResultsRepository: PagedResultsRepository)
    : BaseAthProducer(
        athLinksMarathonScraper,
        pagedResultsRepository,
        LoggerFactory.getLogger(BrightonProducer::class.java),
        MarathonSources.Brighton,
        mapOf(2014 to "https://www.athlinks.com/event/34646/results/Event/373483/Course/492122/Results",
                2015 to "https://www.athlinks.com/event/34646/results/Event/409399/Course/615614/Results",
                2016 to "https://www.athlinks.com/event/34646/results/Event/485115/Course/721410/Results",
                2017 to "https://www.athlinks.com/event/34646/results/Event/576115/Course/864078/Results"),
        mapOf(2014 to 174, 2015 to 185, 2016 to 214, 2017 to 244))

@Component
class VancouverProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                        @Autowired pagedResultsRepository: PagedResultsRepository)
    : BaseAthProducer(
        athLinksMarathonScraper,
        pagedResultsRepository,
        LoggerFactory.getLogger(VancouverProducer::class.java),
        MarathonSources.Vancouver,
        mapOf(2014 to "https://www.athlinks.com/event/34531/results/Event/375949/Course/562585/Results",
                2015 to "https://www.athlinks.com/event/34531/results/Event/386909/Course/577475/Results",
                2016 to "https://www.athlinks.com/event/34531/results/Event/538354/Course/803195/Results",
                2017 to "https://www.athlinks.com/event/34531/results/Event/607474/Course/947043/Results"),
        mapOf(2014 to 76, 2015 to 72, 2016 to 76, 2017 to 72))

@Component
class SurfCityProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                        @Autowired pagedResultsRepository: PagedResultsRepository)
    : BaseAthProducer(
        athLinksMarathonScraper,
        pagedResultsRepository,
        LoggerFactory.getLogger(SurfCityProducer::class.java),
        MarathonSources.SurfCity,
        mapOf(2014 to "https://www.athlinks.com/event/1224/results/Event/280563/Course/580484/Results",
                2015 to "https://www.athlinks.com/event/1224/results/Event/373686/Course/553346/Results",
                2016 to "https://www.athlinks.com/event/1224/results/Event/425859/Course/755717/Results",
                2017 to "https://www.athlinks.com/event/1224/results/Event/615349/Course/767284/Results"),
        mapOf(2014 to 48, 2015 to 44, 2016 to 39, 2017 to 32))

@Component
class LiverpoolProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                        @Autowired pagedResultsRepository: PagedResultsRepository)
    : BaseAthProducer(
        athLinksMarathonScraper,
        pagedResultsRepository,
        LoggerFactory.getLogger(LiverpoolProducer::class.java),
        MarathonSources.Liverpool,
        mapOf(2014 to "https://www.athlinks.com/event/111636/results/Event/382692/Course/568672/Results",
                2015 to "https://www.athlinks.com/event/111636/results/Event/453614/Course/679741/Results",
                2016 to "https://www.athlinks.com/event/111636/results/Event/552882/Course/823759/Results",
                2017 to "https://www.athlinks.com/event/111636/results/Event/613956/Course/934668/Results"),
        mapOf(2014 to 47, 2015 to 50, 2016 to 51, 2017 to 61))

@Component
class SanDiegoProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                        @Autowired pagedResultsRepository: PagedResultsRepository)
    : BaseAthProducer(
        athLinksMarathonScraper,
        pagedResultsRepository,
        LoggerFactory.getLogger(SanDiegoProducer::class.java),
        MarathonSources.SanDiego,
        mapOf(2014 to "https://www.athlinks.com/event/99943/results/Event/359333/Course/539078/Results",
                2015 to "https://www.athlinks.com/event/99943/results/Event/451502/Course/674104/Results",
                2016 to "https://www.athlinks.com/event/99943/results/Event/550317/Course/672290/Results",
                2017 to "https://www.athlinks.com/event/99943/results/Event/550323/Course/1011651/Results"),
        mapOf(2014 to 107, 2015 to 87, 2016 to 117, 2017 to 108))

@Component
class AkronProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                       @Autowired pagedResultsRepository: PagedResultsRepository)
    : BaseAthProducer(
        athLinksMarathonScraper,
        pagedResultsRepository,
        LoggerFactory.getLogger(AkronProducer::class.java),
        MarathonSources.Akron,
        mapOf(2014 to "https://www.athlinks.com/event/20371/results/Event/376915/Course/524041/Results",
                2015 to "https://www.athlinks.com/event/20371/results/Event/440096/Course/659649/Results",
                2016 to "https://www.athlinks.com/event/20371/results/Event/575099/Course/861873/Results",
                2017 to "https://www.athlinks.com/event/20371/results/Event/602618/Course/911979/Results"),
        mapOf(2014 to 32, 2015 to 30, 2016 to 24, 2017 to 19))

@Component
class RiverRockProducer(@Autowired private val belfestCityMarathonScraper: BelfestCityMarathonScraper,
                        @Autowired pagedResultsRepository: PagedResultsRepository)
    : BaseResultPageProducer (LoggerFactory.getLogger(RiverRockProducer::class.java), MarathonSources.RiverRock, pagedResultsRepository) {

    override fun buildYearlyThreads(year: Int, lastPage: Int) {
        val columnPositions = ColumnPositions(place = 0, nationality = 2, ageGender = 3, finishTime = 5)
        val nextPageSelector = "#resultsTable_next"
        val backwardsSelector = "#resultsTable_previous"
        val tBodySelector = "#resultsTable > tbody:nth-child(2)"

        when(year) {
            2014 -> threads.add(belfestCityMarathonScraper.scrape(PagedResultsScrapeInfo(
                    url = "https://www.belfastcitymarathon.com/results/2014#fullResults",
                    marathonSources = marathonSources,
                    marathonYear = 2014,
                    columnPositions = columnPositions,
                    startPage = lastPage,
                    endPage = 24,
                    nextPageSelector = nextPageSelector,
                    backwardsSelector = backwardsSelector,
                    tableBodySelector = tBodySelector,
                    headerRow = false)))
            2015 -> threads.add(belfestCityMarathonScraper.scrape(PagedResultsScrapeInfo(
                    url = "https://www.belfastcitymarathon.com/results/2015#fullResults",
                    marathonSources = marathonSources,
                    marathonYear = 2015,
                    columnPositions = columnPositions,
                    startPage = lastPage,
                    endPage = 22,
                    nextPageSelector = nextPageSelector,
                    backwardsSelector = backwardsSelector,
                    tableBodySelector = tBodySelector,
                    headerRow = false)))
            2016 -> threads.add(belfestCityMarathonScraper.scrape(PagedResultsScrapeInfo(
                    url = "https://www.belfastcitymarathon.com/results/2016#fullResults",
                    marathonSources = marathonSources,
                    marathonYear = 2016,
                    columnPositions = columnPositions,
                    startPage = lastPage,
                    endPage = 22,
                    nextPageSelector = nextPageSelector,
                    backwardsSelector = backwardsSelector,
                    tableBodySelector = tBodySelector,
                    headerRow = false)))
            2017 -> threads.add(belfestCityMarathonScraper.scrape(PagedResultsScrapeInfo(
                    url = "https://www.belfastcitymarathon.com/results/2017#fullResults",
                    marathonSources = marathonSources,
                    marathonYear = 2017,
                    columnPositions = columnPositions,
                    startPage = lastPage,
                    endPage = 22,
                    nextPageSelector = nextPageSelector,
                    backwardsSelector = backwardsSelector,
                    tableBodySelector = tBodySelector,
                    headerRow = false)))
        }
    }
}