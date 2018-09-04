package com.stonesoupprogramming.marathonscrape

import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Semaphore

@Component
class DriverFactory {

    private val logger = LoggerFactory.getLogger(DriverFactory::class.java)
    private val semaphore = Semaphore(Runtime.getRuntime().availableProcessors())

    fun createDriver(): RemoteWebDriver {
        return try {
            logger.info("Waiting on Permit")
            semaphore.acquire()
            logger.info("Permit Acquired")

            sleepRandom()
            ChromeDriver()
        } catch (e: Exception) {
            when (e) {
                is InterruptedException -> {
                    logger.error("Timeout while waiting for driver", e)
                    throw e
                }
                else -> FirefoxDriver()
            }
        }
    }

    fun destroy(driver: RemoteWebDriver) {
        try {
            semaphore.release()
            logger.info("Permit has been released")
            driver.close()
            System.gc()
        } catch (e: Exception) {
            logger.error("Failed to destroy driver", e)
        }
    }
}

abstract class BaseScraper<T : PageInfo> (protected val logger : Logger, protected val runnerDataRepository : RunnerDataRepository,
                           protected val driverFactory: DriverFactory, protected val jsDriver: JsDriver){

    @Async
    open fun scrape(scrapeInfo: T) : CompletableFuture<String> {
        val driver = driverFactory.createDriver()

        return try {
            webscrape(driver, scrapeInfo)

            successResult()
        } catch (e : Exception){
            logger.error("Failed to scrape $scrapeInfo", e)
            failResult()
        } finally {
            driverFactory.destroy(driver)
        }
    }

    protected abstract fun webscrape(driver: RemoteWebDriver, scrapeInfo: T)
}

abstract class UrlPageScraper(logger: Logger, runnerDataRepository: RunnerDataRepository,
                              driverFactory: DriverFactory, jsDriver: JsDriver,
                              protected val urlPageRepository: UrlPageRepository) : BaseScraper<UrlScrapeInfo>(logger, runnerDataRepository, driverFactory, jsDriver)

abstract class CategoryPageScraper(logger: Logger, runnerDataRepository: RunnerDataRepository,
                                   driverFactory: DriverFactory, jsDriver: JsDriver,
                                   protected val categoryResultsRepository: CategoryResultsRepository) :
    BaseScraper<CategoryScrapeInfo>(logger, runnerDataRepository, driverFactory, jsDriver)

abstract class PagedResultsScraper(logger: Logger, runnerDataRepository: RunnerDataRepository,
                                 driverFactory: DriverFactory, jsDriver: JsDriver,
                                 protected val resultsRepository: PagedResultsRepository)
    : BaseScraper<PagedResultsScrapeInfo>(logger, runnerDataRepository, driverFactory, jsDriver){

    override fun webscrape(driver: RemoteWebDriver, scrapeInfo: PagedResultsScrapeInfo) {
        if(scrapeInfo.startPage > scrapeInfo.endPage){
            throw IllegalStateException("Start page can't be after end page: start=${scrapeInfo.startPage}, end=${scrapeInfo.endPage}")
        }

        driver.get(scrapeInfo.url)

        scrapeInfo.comboBoxSelector?.let { cb ->
            scrapeInfo.comboBoxValue?.let { value ->
                scrapeInfo.comboBoxFrame?.let { fr ->
                    driver.switchTo().frame(driver.findElementByCssSelector(fr))
                }
                Thread.sleep(1000)
                driver.selectComboBoxOption(cb.toCss(), value)
            }
        }

        for(page in 1 until scrapeInfo.startPage){
            synchronizePages(driver, page, findCurrentPageNum(driver), scrapeInfo)
            logger.info("[year=${scrapeInfo.marathonYear}] On page=$page, advancing to ${scrapeInfo.startPage}")
        }

        logger.info("Arrived at starting page! (${scrapeInfo.startPage})")

        for(page in scrapeInfo.startPage .. scrapeInfo.endPage){
            synchronizePages(driver, page, findCurrentPageNum(driver), scrapeInfo)
            processPage(driver, page, scrapeInfo)
        }
    }

    protected open fun processPage(driver: RemoteWebDriver, currentPage: Int, scrapeInfo: PagedResultsScrapeInfo){
        scrapeInfo.tableFrame?.let {
            driver.switchTo().parentFrame()
            Thread.sleep(1000)
            driver.switchTo().frame(driver.findElement(it.toCss()))
        }

        var table = jsDriver.readTableRows(driver, scrapeInfo.tableBodySelector)
        var tableHtml = jsDriver.readTableRows(driver, scrapeInfo.tableBodySelector, rawHtml = true)
        if(table.isEmpty()){
            throw IllegalStateException("Failed to gather table information")
        }
        if(scrapeInfo.headerRow){
            table = table.subList(1, table.size)
            tableHtml = tableHtml.subList(1, tableHtml.size)
        }

        val resultsPage = table.mapIndexed { index, row -> processRow(row, scrapeInfo.columnPositions, scrapeInfo, tableHtml[index]) }
        resultsRepository.markPageComplete(runnerDataRepository, resultsPage, scrapeInfo, currentPage, logger)
    }

    abstract fun processRow(row: List<String>, columnPositions: ColumnPositions, scrapeInfo: PagedResultsScrapeInfo, rowHtml: List<String>): RunnerData

    protected abstract fun findCurrentPageNum(driver: RemoteWebDriver): Int

    private fun synchronizePages(driver: RemoteWebDriver, currentPage: Int, jsPage: Int, scrapeInfo: PagedResultsScrapeInfo, attempt: Int = 0, giveUp: Int = 10){
        logger.info("page = $currentPage, ui page = $jsPage")

        try{
            when {
                jsPage == -1 -> {
                    if(attempt < giveUp){
                        jsDriver.clickElement(driver, pickSelector(driver, scrapeInfo))
                        Thread.sleep(1000)
                        synchronizePages(driver, currentPage, findCurrentPageNum(driver), scrapeInfo, attempt + 1)
                    } else {
                        driver.navigate().refresh()
                        synchronizePages(driver, currentPage, findCurrentPageNum(driver), scrapeInfo)
                    }
                }
                currentPage < jsPage -> {
                    jsDriver.clickElement(driver, scrapeInfo.backwardsSelector)
                    Thread.sleep(1000)
                    synchronizePages(driver, currentPage, findCurrentPageNum(driver), scrapeInfo)

                }
                currentPage > jsPage -> {
                    jsDriver.clickElement(driver, pickSelector(driver, scrapeInfo))
                    Thread.sleep(1000)
                    synchronizePages(driver, currentPage, findCurrentPageNum(driver), scrapeInfo)
                }
            }
        } catch (e : Exception){
            if(attempt < giveUp){
                driver.navigate().refresh()
                synchronizePages(driver, currentPage, findCurrentPageNum(driver), scrapeInfo, attempt + 1)
            } else {
                logger.error("Unable to synchronize pages", e)
                throw e
            }
        }

    }

    private fun pickSelector(driver: RemoteWebDriver, scrapeInfo: PagedResultsScrapeInfo) : String {
        return if(findCurrentPageNum(driver) <= 1){
            scrapeInfo.nextPageSelector
        } else {
            scrapeInfo.secondNextPageSelector ?: scrapeInfo.nextPageSelector
        }
    }
}

@Component
class AthLinksMarathonScraper(@Autowired driverFactory: DriverFactory,
                              @Autowired private val athJsDriver: AthJsDriver,
                              @Autowired private val usStateCodes: List<String>,
                              @Autowired private val canadaProvinceCodes: List<String>,
                              @Autowired runnerDataRepository: RunnerDataRepository,
                              @Autowired private val pagedResultsRepository: PagedResultsRepository)
    : PagedResultsScraper(LoggerFactory.getLogger(AthLinksMarathonScraper::class.java), runnerDataRepository, driverFactory, athJsDriver, pagedResultsRepository){

    override fun processPage(driver: RemoteWebDriver, currentPage: Int, scrapeInfo: PagedResultsScrapeInfo) {
        sleepRandom(1, 3)

        val tableData = athJsDriver.readPage(driver)
        if (tableData.isEmpty()) {
            throw IllegalStateException("pageData is empty on year=${scrapeInfo.marathonYear}, page = $currentPage")
        }

        val tableRows = tableData.map { it -> listOf(it["nationality"]!!, it["place"]!!, it["age"]!!, it["gender"]!!, it["finishTime"]!!) }
        val resultsPage = tableRows.map { processRow(it, scrapeInfo.columnPositions, scrapeInfo, emptyList()) }
        pagedResultsRepository.markPageComplete(runnerDataRepository, resultsPage, scrapeInfo, currentPage, logger)
    }

    override fun processRow(row: List<String>, columnPositions: ColumnPositions, scrapeInfo: PagedResultsScrapeInfo, rowHtml: List<String>): RunnerData {
        var nationality = row[0]
        if(nationality.contains(",")){
            val parts = nationality.split(",")
            nationality = usStateCodes.stateToUSA(parts.last().trim())
            nationality = canadaProvinceCodes.provinceToCanada(nationality)
        } else {
            nationality = usStateCodes.stateToUSA(nationality)
            nationality = canadaProvinceCodes.provinceToCanada(nationality)
        }
        val place = row[1].safeInt(logger)
        return try {
            createRunnerData(logger, row[2], row[4], row[3], scrapeInfo.marathonYear, nationality, place, scrapeInfo.marathonSources)
        } catch (e : Exception){
            logger.error("Failed to create runner", e)
            throw e
        }
    }

    override fun findCurrentPageNum(driver: RemoteWebDriver): Int = athJsDriver.findCurrentPage(driver)
}

@Component
class TcsAmsterdamScraper(@Autowired runnerDataRepository: RunnerDataRepository,
                          @Autowired driverFactory: DriverFactory,
                          @Autowired private val tcsAmsterdamJsDriver: TcsAmsterdamJsDriver,
                          @Autowired resultsRepository: PagedResultsRepository)
    : PagedResultsScraper(LoggerFactory.getLogger(TcsAmsterdamScraper::class.java), runnerDataRepository, driverFactory, tcsAmsterdamJsDriver, resultsRepository) {

    override fun processRow(row: List<String>, columnPositions: ColumnPositions, scrapeInfo: PagedResultsScrapeInfo, rowHtml: List<String>): RunnerData {

        val place = row[columnPositions.place].safeInt(logger)
        val nationality = try{
            rowHtml[columnPositions.nationality].split("=")[4].replace(">", "").replace("\"", "")
        } catch (e : Exception){
            logger.error("Failed to parse nationality", e)
            UNAVAILABLE
        }
        val ageGender = row[columnPositions.ageGender]
        val age = if(ageGender.isNotBlank()){
            ageGender.substring(1)
        } else {
            UNAVAILABLE
        }
        val gender = if(ageGender.isNotBlank()){
            ageGender[0].toString()
        } else {
            UNAVAILABLE
        }
        val finish = row[columnPositions.finishTime].unavailableIfBlank()
        return createRunnerData(logger, age, finish, gender, scrapeInfo.marathonYear, nationality, place, scrapeInfo.marathonSources)
    }

    override fun findCurrentPageNum(driver: RemoteWebDriver): Int {
        driver.switchTo().parentFrame()

        return tcsAmsterdamJsDriver.findCurrentPage(driver)
    }
}

@Component
class RegistrationMarathonScraper(@Autowired runnerDataRepository: RunnerDataRepository,
                                  @Autowired driverFactory: DriverFactory,
                                  @Autowired jsDriver: JsDriver,
                                  @Autowired categoryResultsRepository: CategoryResultsRepository)
    :CategoryPageScraper(LoggerFactory.getLogger(RegistrationMarathonScraper::class.java), runnerDataRepository, driverFactory, jsDriver, categoryResultsRepository) {

    override fun webscrape(driver: RemoteWebDriver, scrapeInfo: CategoryScrapeInfo) {
        driver.get(scrapeInfo.url)
        scrapeInfo.raceSelection?.let {
            driver.selectComboBoxOption("#ctl00_HeaderContentPlaceholder_RegistrationResult_RaceVersionDropDownList".toCss(), it)
        }

        Thread.sleep(1000)
        val js = jsDriver.readAttribute(driver, scrapeInfo.category, "href").replace("javascript:", "")

        driver.executeScript(js)
        Thread.sleep(1000)

        val table = jsDriver.readTableRows(driver, "#ctl00_HeaderContentPlaceholder_RegistrationResult_ResultGridView > tbody:nth-child(1)")
        val columnPositions = scrapeInfo.columnPositions

        if(table.isEmpty()){
            throw IllegalStateException("Table can't be empty")
        }
        val resultsPage = table.subList(1, table.size).map { row ->
            val place = row[columnPositions.place].safeInt(logger)
            val gender = scrapeInfo.gender?.code ?: throw IllegalArgumentException("Gender is required")
            val age = row[columnPositions.age].calcAge(logger)
            val nationality = row[columnPositions.nationality].unavailableIfBlank()
            val finishTime = row[columnPositions.finishTime].unavailableIfBlank()
            createRunnerData(logger, age, finishTime, gender, scrapeInfo.marathonYear, nationality, place, scrapeInfo.marathonSources)
        }.toList()

        categoryResultsRepository.markPageComplete(runnerDataRepository, resultsPage, scrapeInfo, logger)
    }
}

//TODO: Update to current architecture
@Component
class MarathonGuideScraper(@Autowired driverFactory: DriverFactory,
                           @Autowired jsDriver: JsDriver,
                           @Autowired runnerDataRepository: RunnerDataRepository,
                           @Autowired urlPageRepository: UrlPageRepository) : UrlPageScraper(LoggerFactory.getLogger(MarathonGuideScraper::class.java), runnerDataRepository, driverFactory, jsDriver, urlPageRepository) {

    override fun webscrape(driver: RemoteWebDriver, urlScrapeInfo: UrlScrapeInfo) {
        val rangeOption = urlScrapeInfo.rangeOptions ?: throw IllegalArgumentException("Range option is required")
        val columnPositions = urlScrapeInfo.columnPositions
        val pageResults = mutableListOf<RunnerData>()

        driver.get(urlScrapeInfo.url)

        driver.waitUntilClickable(By.name("RaceRange"))
        driver.selectComboBoxOption(By.cssSelector("select[name=RaceRange]"), rangeOption)
        driver.findElementByName("SubmitButton").click()

        var table = jsDriver.readTableRows(driver, "table.BoxTitleOrange > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1)")
        table = table.subList(2, table.size)

        for(row in table){
            val place = row[columnPositions.place].toInt()
            val finish = row[columnPositions.finishTime]
            val ageGender = row[columnPositions.ageGender]
            val age = try {
                ageGender.substring(1)
            } catch (e : Exception){
                logger.error("Unable to get age", e)
                UNAVAILABLE
            }
            val gender = try {
                ageGender[0].toString()
            } catch (e : Exception){
                logger.error("Unable to get gender", e)
                UNAVAILABLE
            }

            pageResults.add(createRunnerData(logger, age, finish, gender, urlScrapeInfo.marathonYear, UNAVAILABLE, place, urlScrapeInfo.marathonSources))
        }

        val url = urlScrapeInfo.url + ", " + rangeOption
        UrlPage(source = urlScrapeInfo.marathonSources, marathonYear = urlScrapeInfo.marathonYear, url = url)
                .markComplete(urlPageRepository, runnerDataRepository, pageResults, logger)
    }

    @Async
    fun findRangeOptionsForUrl(url: String): CompletableFuture<List<String>> {
        val driver = driverFactory.createDriver()

        return try {
            driver.get(url)
            CompletableFuture.completedFuture(findRangeOptions(driver))
        } catch (e: Exception) {
            logger.error("Unable to get the range options on Marathon Guide", e)
            throw e
        } finally {
            driverFactory.destroy(driver)
        }
    }

    @Async
    fun scrape(year: Int, url: String, source: MarathonSources, columnPositions: ColumnPositions, rangeOption: String): CompletableFuture<String> {
        val driver = driverFactory.createDriver()

        try {
            driver.get(url)

            driver.waitUntilClickable(By.name("RaceRange"))
            driver.selectComboBoxOption(By.cssSelector("select[name=RaceRange]"), rangeOption)
            driver.findElementByName("SubmitButton").click()

            processTable(driver, year, source, rangeOption,columnPositions)

            logger.info("Finished $url, $year, $rangeOption successfully")
            return successResult()
        } catch (e: Exception) {
            logger.error("Failed to scrape $url, $year, $rangeOption, $url", e)
            return failResult()
        } finally {
            driverFactory.destroy(driver)
        }
    }

    private fun processTable(driver: RemoteWebDriver, year: Int, source: MarathonSources, rangeOption: String, columnPositions: ColumnPositions) {
        try {
            driver.waitUntilVisible(By.className("BoxTitleOrange"))

            var table = jsDriver.readTableRows(driver, "table.BoxTitleOrange > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1)")
            table = table.subList(2, table.size)
            val pageResults = table.map { row ->
                try {
                    val ageGender = row[columnPositions.ageGender]
                    val gender = ageGender.substringAfter("(")[0].toString()
                    val age = ageGender.substringAfter("(").substring(1, 3)
                    val place = row[columnPositions.place].toInt()
                    val finishTime = row[columnPositions.finishTime]
                    val nationality = row[columnPositions.nationality]

                    createRunnerData(logger, age, finishTime, gender, year, nationality, place, source)
                } catch (e : Exception){
                    logger.error("Failed to process row=$row", e)
                    throw e
                }
            }.toList()
            UrlPage(source = source, marathonYear = year, url = rangeOption).markComplete(urlPageRepository, runnerDataRepository, pageResults.toMutableList(), logger)
        } catch (e: Exception) {
            logger.error("Failed to process table for $year", e)
        }
    }

    private fun findRangeOptions(driver: RemoteWebDriver): List<String> {
        try {
            driver.waitUntilClickable(By.cssSelector("select[name=RaceRange]"))
            return driver
                    .findElementByCssSelector("select[name=RaceRange")
                    .findElements(By.tagName("option"))
                    .map { it -> it.text }
                    .filter { it -> it != "Overall Results" }
                    .toList()
        } catch (e: Exception) {
            logger.error("Failed to get the select option values", e)
            throw e
        }
    }
}

//TODO: Update to current architecture
@Component
class MtecResultScraper(@Autowired private val driverFactory: DriverFactory,
                        @Autowired private val jsDriver: JsDriver,
                        @Autowired private val pagedResultsRepository: PagedResultsRepository) {

    private val logger = LoggerFactory.getLogger(MtecResultScraper::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, url: String, year: Int, source: MarathonSources, startPage: Int, endPage: Int): CompletableFuture<String> {
        val driver = driverFactory.createDriver()

        try {
            driver.get(url)

            driver.click("#quickresults > div > a:nth-child(4)".toCss(), logger)
            driver.click("#searchResults > div > a:nth-child(5)".toCss(), logger)

            for (pageNum in 1..endPage) {
                if (pageNum > startPage) {
                    val resultPage = mutableListOf<RunnerData>()
                    processTable(driver, resultPage, year, source)
                    //PagedResults(source = source, marathonYear = year, pageNum = pageNum, url = url).markComplete(pagedResultsRepository, queue, resultPage, logger)
                }
                if (pageNum == 1) {
                    driver.click("#searchResults > div > a:nth-child(2)".toCss(), logger)
                } else {
                    driver.click("#searchResults > div:nth-child(1) > a:nth-child(3)".toCss(), logger)
                }
            }

            return successResult()
        } catch (e: Exception) {
            logger.error("Failed to process year=$year, url=$url", e)
            return failResult()
        } finally {
            driverFactory.destroy(driver)
        }
    }

    private fun processTable(driver: RemoteWebDriver, resultPage: MutableList<RunnerData>, year: Int, source: MarathonSources) {
        try {
            val table = jsDriver.readTableRows(driver, "#searchResults > div > div > table > tbody")
            for (row in 0 until table.size) {
                processRow(table[row], resultPage, year, source)
            }
        } catch (e: Exception) {
            logger.error("Failed to process page", e)
            throw e
        }
    }

    private fun processRow(row: List<String>, resultPage: MutableList<RunnerData>, year: Int, source: MarathonSources) {
        try {
            val gender = row[2].trim()
            val age = row[3].trim()
            val finishTime = row[9].trim()
            val place = row[6].substringBefore("/").trim().toInt()

            resultPage.insertRunnerData(logger, age, finishTime, gender, year, "USA", place, source)
        } catch (e: Exception) {
            logger.error("Failed to process row=$row", e)
            throw e
        }
    }
}

//TODO: Update to current architecture
@Component
class TrackShackResults(@Autowired private val driverFactory: DriverFactory,
                        @Autowired private val urlPageRepository: UrlPageRepository,
                        @Autowired private val jsDriver: JsDriver,
                        @Autowired private val usStateCodes: List<String>) {

    private val logger = LoggerFactory.getLogger(TrackShackResults::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, page: UrlPage, gender: Gender, columnPositions: ColumnPositions): CompletableFuture<String> {
        val driver = driverFactory.createDriver()

        try {
            driver.get(page.url)
            driver.waitUntilVisible(By.cssSelector("#f1 > p:nth-child(13) > table"))

            val resultsPage = mutableListOf<RunnerData>()
            val table = jsDriver.readTableRows(driver, "#f1 > p:nth-child(13) > table > tbody")

            for (row in 2 until table.size) {
                val tableRow = table[row]

                val place = tableRow[columnPositions.place].toInt()
                val age = tableRow[columnPositions.age]
                var nationality = tableRow[columnPositions.nationality].substringAfterLast(",").trim()
                nationality = usStateCodes.stateToUSA(nationality)
                val finishTime = tableRow[columnPositions.finishTime]

                //resultsPage.insertRunnerData(logger, age, finishTime, gender.code, page.marathonYear, nationality, place, page.source)
            }

            //page.markComplete(urlPageRepository, queue, resultsPage, logger)

            return successResult()
        } catch (e: Exception) {
            logger.error("Unable to scrape: $page")
            return failResult()
        } finally {
            driverFactory.destroy(driver)
        }
    }
}

//TODO: Update to current architecture
@Component
class SportStatsScrape(@Autowired private val driverFactory: DriverFactory,
                       @Autowired private val jsDriver: JsDriver,
                       @Autowired private val pagedResultsRepository: PagedResultsRepository) {

    private val logger = LoggerFactory.getLogger(SportStatsScrape::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, pagedResults: PagedResults, startPage: Int, endPage: Int, columnPositions: ColumnPositions): CompletableFuture<String> {
        val driver = driverFactory.createDriver()

        return try {
            driver.get(pagedResults.url)
            Thread.sleep(5000)

            scrollToPage(driver, startPage)

            for (page in startPage until endPage) {
                val table = jsDriver.readTableRows(driver, ".overview-result > tbody")
                val results = table.subList(0, table.size - 1).map { row ->
                    try {
                        val nationality = if (columnPositions.nationality == -1) {
                            UNAVAILABLE
                        } else {
                            row[columnPositions.nationality]
                        }
                        val ageGender = row[columnPositions.ageGender]
                        val age = if (ageGender.isNotBlank()) {
                            ageGender.substring(1)
                        } else {
                            UNAVAILABLE
                        }
                        val gender = if (ageGender.isNotBlank()) {
                            ageGender[0].toString()
                        } else {
                            UNAVAILABLE
                        }
                        val place = try {
                            row[columnPositions.place].toInt()
                        } catch (e: NumberFormatException) {
                            Int.MAX_VALUE
                        }
                        createRunnerData(logger,
                                age,
                                row[columnPositions.finishTime],
                                gender,
                                pagedResults.marathonYear,
                                nationality,
                                place,
                                pagedResults.source)
                    } catch (e: Exception) {
                        logger.error("Index out of Bounds")
                        throw e
                    }

                }.toList()
//                PagedResults(source = pagedResults.source, marathonYear = pagedResults.marathonYear, url = pagedResults.url, pageNum = page)
//                        .markComplete(pagedResultsRepository, queue, results.toMutableList(), logger)
                advancePage(driver)
            }

            successResult()
        } catch (e: Exception) {
            logger.error("Failed to scrape $pagedResults", e)
            failResult()
        } finally {
            driverFactory.destroy(driver)
        }
    }

    private fun scrollToPage(driver: RemoteWebDriver, startPage: Int) {
        var pageNum = 0
        while (pageNum < startPage) {
            advancePage(driver)
            pageNum++
            sleepRandom()
        }
    }

    private fun advancePage(driver: RemoteWebDriver, attempt: Int = 0, giveUp: Int = 60) {
        try {
            val loaderHtml = jsDriver.readHtml(driver, "#ajaxStatusPanel")
            if (loaderHtml.contains("<div id=\"ajaxStatusPanel_start\" style=\"display: none;\">")) {
                driver.findElementByCssSelector(".pagination > li:nth-child(13)").findElement(By.tagName("a")).click()
            } else {
                if (attempt < giveUp) {
                    Thread.sleep(5000)
                    advancePage(driver, attempt + 1)
                } else {
                    throw RuntimeException("Can't advance to the next page due to preloader")
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to advance to next page", e)
            throw e
        }
    }
}

@Component
class PacificSportScraper(@Autowired runnerDataRepository: RunnerDataRepository,
                          @Autowired driverFactory: DriverFactory,
                          @Autowired jsDriver: JsDriver,
                          @Autowired urlPageRepository: UrlPageRepository) : UrlPageScraper(LoggerFactory.getLogger(PacificSportScraper::class.java), runnerDataRepository, driverFactory, jsDriver, urlPageRepository) {

    override fun webscrape(driver: RemoteWebDriver, urlScrapeInfo: UrlScrapeInfo) {
        driver.get(urlScrapeInfo.url)

        val elite = "Elite"
        val selector = urlScrapeInfo.tbodySelector ?: throw IllegalArgumentException("Table selector is required")
        val table = jsDriver.readTableRows(driver, selector)
        val resultPage = mutableListOf<RunnerData>()

        processRows@for(row in table){
            val positions = urlScrapeInfo.columnPositions
            val ageGender = row[positions.ageGender]

            if(ageGender.contains(elite)){
                logger.info("Skipping Elite Row per Requirements")
                continue@processRows
            }

            val place = try {
                row[positions.place].toInt()
            } catch (e : Exception){
                logger.error("Place isn't available", e)
                Int.MAX_VALUE
            }
            var nationality = row[positions.nationality]
            if(nationality.contains(",")){
                nationality = nationality.split(",").last().trim()
            }

            val gender = try {
                ageGender[0].toString()
            } catch (e : Exception){
                logger.error("Gender is not available")
                UNAVAILABLE
            }
            val age = try {
                ageGender.replace("M", "").replace("F", "").replace("W", "")
            } catch (e : Exception){
                logger.error("Age isn't available", e)
                UNAVAILABLE
            }
            val finish = row[positions.finishTime]
            val half = row[positions.halfwayTime]

            resultPage.add(createRunnerData(logger, age, finish, gender, urlScrapeInfo.marathonYear, nationality, place, urlScrapeInfo.marathonSources, halfwayTime = half))
        }

        UrlPage(source = urlScrapeInfo.marathonSources, marathonYear = urlScrapeInfo.marathonYear, url = urlScrapeInfo.url).markComplete(urlPageRepository, runnerDataRepository, resultPage, logger)
    }
}