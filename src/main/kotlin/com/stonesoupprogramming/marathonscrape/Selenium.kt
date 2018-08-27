package com.stonesoupprogramming.marathonscrape

import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Semaphore

const val UNAVAILABLE = "Unavailable"

@Component
class DriverFactory {

    private val logger = LoggerFactory.getLogger(DriverFactory::class.java)
    private val semaphore = Semaphore(Runtime.getRuntime().availableProcessors() - 1)

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

//Complete
@Component
class MedtronicMarathonScraper(@Autowired private val driverFactory: DriverFactory,
                               @Autowired private val stateCodes: List<String>) {

    private val logger = LoggerFactory.getLogger(MedtronicMarathonScraper::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, url: String, year: Int): CompletableFuture<String> {
        val driver = driverFactory.createDriver()

        return try {
            driver.get(url)
            selectOverallResults(driver)
            selectMaxResultsPage(driver)

            do {
                processPage(driver, queue, year)
            } while (advancePage(driver))

            successResult()
        } catch (e: Exception) {
            logger.error("Failed to scrape url=$url, year=$year")
            failResult()
        } finally {
            driverFactory.destroy(driver)
        }
    }

    private fun processPage(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int) {
        try {
            val numRows = findNumRows(driver)
            for (row in 0 until numRows) {
                processRow(driver, queue, row, year)
            }
        } catch (e: Exception) {
            logger.error("Failed to process the page", e)
        }
    }

    private fun processRow(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, row: Int, year: Int) {
        try {
            val gender = findCellValue(driver, row, 2)
            val age = findCellValue(driver, row, 3)
            var nationality = findCellValue(driver, row, 5)
            nationality = if (stateCodes.contains(nationality)) {
                "USA"
            } else {
                "International"
            }
            val place = findCellValue(driver, row, 6).split("/")[0].trim().toInt()
            val finishTime = findCellValue(driver, row, 9)

            queue.insertRunnerData(
                    logger = logger,
                    age = age,
                    finishTime = finishTime,
                    gender = gender,
                    year = year,
                    nationality = nationality,
                    place = place,
                    source = MarathonSources.TwinCities)
        } catch (e: Exception) {
            logger.error("Unable to process row = $row for year = $year", e)
        }
    }

    private fun findCellValue(driver: RemoteWebDriver, row: Int, cell: Int): String {
        try {
            return driver.findElementByCssSelector("#searchResults > div > div > table > tbody")
                    .findElements(By.tagName("tr"))[row]
                    .findElements(By.tagName("td"))[cell].text
        } catch (e: Exception) {
            logger.error("Unable to determine cell value at row=$row, cell=$cell", e)
            throw e
        }
    }

    private fun findNumRows(driver: RemoteWebDriver): Int {
        try {
            driver.waitUntilVisible(By.cssSelector("#searchResults > div > div > table > tbody"))
            return driver.findElementByCssSelector("#searchResults > div > div > table > tbody")
                    .findElements(By.tagName("tr")).size
        } catch (e: Exception) {
            logger.error("Unable to find row count", e)
            throw e
        }
    }

    private fun advancePage(driver: RemoteWebDriver): Boolean {
        try {
            driver.waitUntilClickable(By.cssSelector("#searchResults > div > a:nth-child(2) > span"))
            val twoText = driver.findElementByCssSelector("#searchResults > div > a:nth-child(2) > span").text
            if (twoText.contains("→")) {
                driver.findElementByCssSelector("#searchResults > div > a:nth-child(2) > span").click()
                return true
            }
            try {
                val threeText = driver.findElementByCssSelector("#searchResults > div > a:nth-child(3) > span").text
                if (threeText.contains("→")) {
                    driver.findElementByCssSelector("#searchResults > div > a:nth-child(3) > span").click()
                    return true
                }
            } catch (e: Exception) {
                when (e) {
                    is NoSuchElementException -> return false
                    else -> {
                        logger.error("Failed to determine if there is another page", e)
                        throw e
                    }
                }
            }
            return false
        } catch (e: Exception) {
            logger.error("Failed to determine if there is another page")
            throw e
        }
    }

    private fun selectMaxResultsPage(driver: RemoteWebDriver) {
        try {
            driver.waitUntilClickable(By.cssSelector("#searchResults > div > a:nth-child(5) > span"))
            driver.findElementByCssSelector("#searchResults > div > a:nth-child(5) > span").click()
        } catch (e: Exception) {
            logger.error("Unable to increase page size to 500", e)
        }
    }

    private fun selectOverallResults(driver: RemoteWebDriver) {
        try {
            driver.waitUntilClickable(By.cssSelector("#quickresults > div > a:nth-child(4) > span"))
            driver.findElementByCssSelector("#quickresults > div > a:nth-child(4) > span").click()
        } catch (e: Exception) {
            logger.error("Unable to select all results", e)
        }
    }
}

//TODO: Fixme
@Component
class BerlinMarathonScraper(@Autowired private val driverFactory: DriverFactory) {

    private val logger = LoggerFactory.getLogger(BerlinMarathonScraper::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, year: Int): CompletableFuture<String> {
        val driver = driverFactory.createDriver()

        return try {
            driver.get("https://www.bmw-berlin-marathon.com/en/facts-and-figures/results-archive.html")
            loadYear(driver, year)
            loadCompetition(driver)
            processRows(driver, queue, year)

            successResult()
        } catch (e: Exception) {
            logger.error("Failed to scrape Berlin for year = $year", e)
            failResult()
        } finally {
            driverFactory.destroy(driver)
        }
    }

    private fun processRows(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int) {
        try {
            driver.waitUntilVisible(By.id("resultGrid"))

            var row = 1
            val totalRows = when (year) {
                2017 -> 39234
                2016 -> 35999
                2015 -> 36768
                2014 -> 28946
                else -> throw IllegalArgumentException()
            }
            processRow@ while (row < totalRows) {
                if (row < 1) {
                    row = 1
                }
                if (row > 0) {
                    try {
                        advanceRows(driver, row, year)
                    } catch (e: Exception) {
                        try {
                            advanceRows(driver, row - 1, year)
                        } catch (e: Exception) {
                            row -= 1
                            continue@processRow
                        }
                    }
                }
                row = processRow(driver, queue, row, year)
            }
        } catch (e: Exception) {
            logger.error("Failed to process rows for year = $year", e)
        }
    }

    private fun processRow(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, row: Int, year: Int): Int {
        return try {
            val place = getCellValue(driver, row, 2).toInt()
            val team = getCellValue(driver, row, 6)
            val nation = getCellValue(driver, row, 7)
            val age = (LocalDateTime.now().year - getCellValue(driver, row, 8).toInt()).toString()
            val gender = getCellValue(driver, row, 9)
            val finishTime = getCellValue(driver, row, 12)

            val runnerData = RunnerData(
                    age = age,
                    company = team,
                    finishTime = finishTime,
                    gender = gender,
                    marathonYear = year,
                    place = place,
                    source = MarathonSources.Berlin,
                    nationality = nation
            )
            queue.put(runnerData)
            logger.info("Produced $runnerData")

            row + 1
        } catch (e: Exception) {
            logger.error("Failed to process row=$row, in year = $year, rolling back", e)
            row - 1
        }
    }

    private fun getCellValue(driver: RemoteWebDriver, row: Int, cell: Int): String {
        return driver.findElementByCssSelector("#resultGrid > tbody")
                .findElements(By.tagName("tr"))[row]
                .findElements(By.tagName("td"))[cell].text
    }

    private fun advanceRows(driver: RemoteWebDriver, row: Int, year: Int) {
        try {
            driver.findElementByCssSelector("#resultGrid")
                    .findElement(By.tagName("tbody"))
                    .findElements(By.tagName("tr"))[row].scrollIntoView(driver)
            Thread.sleep(1000)
        } catch (e: Exception) {
            logger.error("Failed to advance to next set of rows. row = $row, year = $year", e)
            throw e
        }
    }

    private fun loadCompetition(driver: RemoteWebDriver) {
        try {
            driver.waitUntilClickable(By.cssSelector("#gridCompetitionChooser"))
            driver.selectComboBoxOption(By.cssSelector("#gridCompetitionChooser"), "Runner")
        } catch (e: Exception) {
            logger.error("Failed to load the runner competition", e)
        }
    }

    private fun loadYear(driver: RemoteWebDriver, year: Int) {
        try {
            driver.waitUntilClickable(By.cssSelector("#gridEventChooser"))
            when (year) {
                2014 -> driver.selectComboBoxOption(By.id("gridEventChooser"), "2014 | 41. BMW BERLIN-MARATHON")
                2015 -> driver.selectComboBoxOption(By.id("gridEventChooser"), "2015 | 42. BMW BERLIN-MARATHON")
                2016 -> driver.selectComboBoxOption(By.id("gridEventChooser"), "2016 | 43. BMW BERLIN-MARATHON")
                2017 -> driver.selectComboBoxOption(By.id("gridEventChooser"), "2017 | 44. BMW BERLIN-MARATHON")
            }
        } catch (e: Exception) {
            logger.error("Failed to load year = $year", e)
        }
    }
}

//Completed
@Component
class ViennaMarathonScraper(@Autowired private val driverFactory: DriverFactory,
                            @Autowired private val jsDriver: JsDriver,
                            @Autowired private val genderPagedResultsRepository: GenderPagedResultsRepository) {

    private val logger = LoggerFactory.getLogger(ViennaMarathonScraper::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, year: Int, gender: Gender, categoryIndex: Int): CompletableFuture<String> {
        if (categoryIndex < 3 || categoryIndex > 14) {
            throw IllegalArgumentException("Must be between 3 and 14")
        }

        val driver = driverFactory.createDriver()
        val url = "https://www.vienna-marathon.com/?surl=cd162e16e318d263fd56d6261673fe72#goto-result"
        driver.get(url)

        return try {
            val resultsPage = mutableListOf<RunnerData>()

            selectYear(driver, year)
            selectEvent(driver)
            val category = when (gender) {
                Gender.MALE -> "#contentResultPage > div > div:nth-child(6) > div.panel-body > div > a:nth-child($categoryIndex)"
                Gender.FEMALE -> "#contentResultPage > div > div:nth-child(5) > div.panel-body > div > a:nth-child($categoryIndex)"
                Gender.UNASSIGNED -> throw IllegalArgumentException("Has to be ${Gender.MALE} or ${Gender.FEMALE}")
            }
            selectCategory(driver, year, gender, category)
            agreeToCookies(driver)
            processTable(driver, year, gender, resultsPage)

            GenderPagedResults(source = MarathonSources.Vienna, marathonYear = year, url = url, gender = gender, pageNum = categoryIndex).markComplete(genderPagedResultsRepository, queue, resultsPage, logger)

            successResult()
        } catch (e: Exception) {
            logger.error("Failed to scrape Vienna for year = $year", e)
            failResult()
        } finally {
            driverFactory.destroy(driver)
        }
    }

    private fun agreeToCookies(driver: RemoteWebDriver) {
        try {
            driver.findElementByLinkText("I agree").click()
        } catch (e: Exception) {
            logger.error("Unable to dismiss cookie message", e) //This isn't essential
        }
    }

    private fun processTable(driver: RemoteWebDriver, year: Int, gender: Gender, resultsPage: MutableList<RunnerData>) {
        try {
            val table = jsDriver.readTableRows(driver, ".resultList > tbody")
            for (row in 2 until table.size step 2) {
                processRow(table[row], year, gender, resultsPage)
            }
        } catch (e: Exception) {
            logger.error("Failed to process table for year=$year, gender=$gender", e)
            throw e
        }
    }

    private fun processRow(tableRow: List<String>, year: Int, gender: Gender, resultsPage: MutableList<RunnerData>) {
        try {
            val place = tableRow[0].toInt()
            val finishTime = tableRow[10]
            val nationality = tableRow[5]
            val age = (LocalDateTime.now().year - (1900 + tableRow[0].toInt())).toString()
            resultsPage.insertRunnerData(logger, age, finishTime, gender.code, year, nationality, place, MarathonSources.Vienna)
        } catch (e: Exception) {
            logger.error("Failed to process row=$tableRow, for year=$year, for gender=$gender", e)
            throw e
        }
    }

    private fun selectCategory(driver: RemoteWebDriver, year: Int, gender: Gender, category: String) {
        try {
            //driver.waitUntilVisible(By.cssSelector(category))
            Thread.sleep(5000)
            val js = driver.findElementByCssSelector(category).getAttribute("href").replace("javascript:", "")
            driver.executeScript(js)
            Thread.sleep(1000)
        } catch (e: Exception) {
            logger.error("Failed to select category=$category, for year=$year, for gender=$gender", e)
            throw e
        }
    }

    private fun selectEvent(driver: RemoteWebDriver) {
        try {
            driver.waitUntilClickable(By.cssSelector("#resultSelectFormAction > div:nth-child(2) > div > select"))
            driver.selectComboBoxOption(By.cssSelector("#resultSelectFormAction > div:nth-child(2) > div > select"), "Vienna City Marathon")
        } catch (e: Exception) {
            logger.error("Failed to select the marathon event", e)
            throw e
        }
    }

    private fun selectYear(driver: RemoteWebDriver, year: Int) {
        try {
            driver.waitUntilClickable(By.cssSelector("#resultSelectFormAction > div:nth-child(1) > div > select"))
            driver.selectComboBoxOption(By.cssSelector("#resultSelectFormAction > div:nth-child(1) > div > select"), year.toString())
        } catch (e: Exception) {
            logger.error("Failed to select year = $year", e)
            throw e
        }
    }
}

//Complete
@Component
class BostonMarathonScrape(@Autowired private val driverFactory: DriverFactory,
                           @Autowired private val pagedResultsRepository: PagedResultsRepository,
                           @Autowired private val jsDriver: JsDriver) {

    private val logger = LoggerFactory.getLogger(BostonMarathonScrape::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, startPage: Int, year: Int): CompletableFuture<String> {
        val url = "http://registration.baa.org/cfm_Archive/iframe_ArchiveSearch.cfm"
        val driver = driverFactory.createDriver()
        var pageNum = 0
        val nextButtonSelector = "input[name=next]"
        val tbody = "td.tablegrid_list_item:nth-child(1) > table:nth-child(1) > tbody:nth-child(2)"

        try {
            driver.get("http://registration.baa.org/cfm_Archive/iframe_ArchiveSearch.cfm")

            driver.waitUntilClickable(By.cssSelector("select[name=RaceYearLowID"))
            driver.selectComboBoxOption(By.cssSelector("select[name=RaceYearLowID]"), year.toString())
            driver.selectComboBoxOption(By.cssSelector("select[name=RaceYearHighID]"), year.toString())

            jsDriver.clickElement(driver, "input.submit_button:nth-child(1)")
            jsDriver.scrollToPage(driver, nextButtonSelector, startPage)

            while (jsDriver.elementIsPresent(driver, nextButtonSelector)) {
                val resultsPage = mutableListOf<RunnerData>()
                val tableRows = jsDriver.readTableRows(driver, tbody)
                for (row in 0 until tableRows.size - 2 step 2) {
                    val age = tableRows[row][3].trim()
                    val gender = tableRows[row][4].trim()
                    val nationality = tableRows[row][7].trim()
                    val place = tableRows[row + 1][1].split("/")[0].trim().toInt()
                    val finishTime = tableRows[row + 1][5]

                    resultsPage.insertRunnerData(logger, age, finishTime, gender, year, nationality, place, MarathonSources.Boston)
                }

                PagedResults(source = MarathonSources.Boston, marathonYear = year, url = url, pageNum = pageNum)
                        .markComplete(pagedResultsRepository, queue, resultsPage, logger)
                pageNum++
                jsDriver.clickElement(driver, nextButtonSelector)
            }
            return CompletableFuture.completedFuture("Success")
        } catch (e: Exception) {
            logger.error("Failed to scrape Boston Marathon year = $year")
            return CompletableFuture.completedFuture("Error")
        } finally {
            driverFactory.destroy(driver)
        }
    }
}

//Completed
@Component
class ChicagoMarathonScrape(@Autowired private val driverFactory: DriverFactory) {

    private val logger = LoggerFactory.getLogger(ChicagoMarathonScrape::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, year: Int, gender: String): CompletableFuture<String> {
        val driver = driverFactory.createDriver()

        try {
            val url = "http://chicago-history.r.mikatiming.de/2015/"
            when (gender) {
                "M" -> {
                    yearForm(driver, "Men", year, url)
                    processTable(driver, queue, "M", year)
                }
                "W" -> {
                    yearForm(driver, "Women", year, url)
                    processTable(driver, queue, "W", year)
                }
            }
            return CompletableFuture.completedFuture("Success")
        } catch (e: Exception) {
            logger.error("Failed to scrape Chicago", e)
            return CompletableFuture.completedFuture("Error")
        } finally {
            driverFactory.destroy(driver)
        }
    }

    @Async
    fun scrape2017(queue: BlockingQueue<RunnerData>, gender: String): CompletableFuture<String> {
        val driver = driverFactory.createDriver()

        try {
            val url = "http://results.chicagomarathon.com/2017/"
            when (gender) {
                "M" -> {
                    yearForm2017(driver, "Men", url)
                    processTable(driver, queue, "M", 2017)
                }
                "W" -> {
                    yearForm2017(driver, "Women", url)
                    processTable(driver, queue, "W", 2017)
                }
            }
            return CompletableFuture.completedFuture("Success")
        } catch (e: Exception) {
            logger.error("Failed to process 2017", e)
            return CompletableFuture.completedFuture("Error")
        } finally {
            driverFactory.destroy(driver)
        }
    }

    private fun yearForm2017(driver: RemoteWebDriver, gender: String, url: String) {
        try {
            driver.get(url)

            driver.waitUntilClickable(By.id("lists-sex"))
            driver.selectComboBoxOption(By.id("lists-sex"), gender)
            Thread.sleep(1000)

            driver.waitUntilClickable(By.id("num_results"))
            driver.selectComboBoxOption(By.id("num_results"), 1000.toString())
            Thread.sleep(1000)

            driver.findElementById("form_lists_default").findElement(By.id("submit")).click()
        } catch (e: Exception) {
            logger.error("Unable to set form", e)
        }
    }

    private fun processTable(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, gender: String, year: Int) {
        try {
            var firstPage = true
            var keepScraping = true
            do {
                driver.waitUntilVisible(By.className("list-table"))
                for (i in 0 until numRows(driver)) {
                    val place = findCellValue(driver, i, 0).toInt()
                    val nationality = findCellValue(driver, i, 3).substringAfter("(").replace(")", "")
                    val age = findCellValue(driver, i, 6)
                    val half = findCellValue(driver, i, 8)
                    val finish = findCellValue(driver, i, 9)

                    val runnerData = RunnerData(place = place,
                            nationality = nationality,
                            age = age,
                            halfwayTime = half,
                            finishTime = finish,
                            source = MarathonSources.Chicago,
                            marathonYear = year,
                            gender = gender)
                    queue.put(runnerData)
                    logger.info("Produced: $runnerData")
                }
                if (hasNextButton(driver, firstPage)) {
                    clickNext(driver, firstPage)
                    firstPage = false
                } else {
                    keepScraping = false
                }
            } while (keepScraping)
        } catch (e: Exception) {
            logger.error("Failed to process the results", e)
        }
    }

    private fun findCellValue(driver: RemoteWebDriver, row: Int, cell: Int): String {
        try {
            return driver.findElementsByCssSelector(".list-table > tbody > tr")[row]
                    .findElements(By.tagName("td"))[cell].text
        } catch (e: Exception) {
            logger.error("Failed to get value for $row, $cell", e)
            throw e
        }
    }

    private fun numRows(driver: RemoteWebDriver): Int {
        try {
            return driver.findElementsByCssSelector(".list-table > tbody > tr").size
        } catch (e: Exception) {
            logger.error("Failed to get the number of rows", e)
            throw e
        }
    }

    private fun yearForm(driver: RemoteWebDriver, gender: String, year: Int, url: String) {
        try {
            driver.get(url)

            driver.waitUntilClickable(By.id("list_event_main_group"))
            driver.selectComboBoxOption(By.id("list_event_main_group"), year.toString())
            Thread.sleep(1000)

            driver.waitUntilClickable(By.id("list_event"))
            driver.selectComboBoxOption(By.id("list_event"), "Marathon")

            driver.waitUntilClickable(By.id("list_search-sex"))
            driver.selectComboBoxOption(By.id("list_search-sex"), gender)

            driver.waitUntilClickable(By.id("fe-lists-new-num-results"))
            driver.selectComboBoxOption(By.id("fe-lists-new-num-results"), 1000.toString())

            driver.findElementByCssSelector("#form_list > div > .submit").click()
        } catch (e: Exception) {
            logger.error("Failed to select the year $year", e)
        }
    }

    private fun hasNextButton(driver: RemoteWebDriver, firstPage: Boolean): Boolean {
        val size = if (firstPage) {
            1
        } else {
            2
        }
        return try {
            driver.findElementsByClassName("pages-nav-button").size == size
        } catch (e: Exception) {
            logger.error("Unable to determine if there is a next button", e)
            false
        }
    }

    private fun clickNext(driver: RemoteWebDriver, firstPage: Boolean) {
        val index = if (firstPage) {
            0
        } else {
            1
        }
        try {
            driver.scrollIntoView(By.className("pages-nav-button"))
            driver.waitUntilClickable(By.className("pages-nav-button"))
            if (index == 0) {
                driver.findElementByClassName("pages-nav-button").click()
            } else {
                driver.findElementsByClassName("pages-nav-button")[index].click()
            }
        } catch (e: Exception) {
            logger.error("Failed to click next", e)
        }
    }
}

//NYC Finished
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

            pageResults.add(createRunnerData(logger, age, finish, gender, urlScrapeInfo.marathonYear, UNAVAILABLE, place, urlScrapeInfo.source))
        }

        val url = urlScrapeInfo.url + ", " + rangeOption
        UrlPage(source = urlScrapeInfo.source, marathonYear = urlScrapeInfo.marathonYear, url = url)
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

//LA Finished
//Disney Finished
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
                    PagedResults(source = source, marathonYear = year, pageNum = pageNum, url = url).markComplete(pagedResultsRepository, queue, resultPage, logger)
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

//LA Finsihed
@Component
class TrackShackResults(@Autowired private val driverFactory: DriverFactory,
                        @Autowired private val urlPageRepository: UrlPageRepository,
                        @Autowired private val jsDriver: JsDriver,
                        @Autowired private val stateCodes: List<String>) {

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
                nationality = stateCodes.toCountry(nationality)
                val finishTime = tableRow[columnPositions.finishTime]

                resultsPage.insertRunnerData(logger, age, finishTime, gender.code, page.marathonYear, nationality, place, page.source)
            }

            page.markComplete(urlPageRepository, queue, resultsPage, logger)

            return successResult()
        } catch (e: Exception) {
            logger.error("Unable to scrape: $page")
            return failResult()
        } finally {
            driverFactory.destroy(driver)
        }
    }
}

//Completed
@Component
class MarineCorpsScrape(@Autowired private val driverFactory: DriverFactory,
                        @Autowired private val pagedResultsRepository: PagedResultsRepository) {

    private val logger = LoggerFactory.getLogger(MarineCorpsScrape::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, year: Int, startPage: Int, endPage: Int): CompletableFuture<String> {
        val driver = driverFactory.createDriver()
        val resultsPage = mutableListOf<RunnerData>()

        try {
            processForm(driver, year)

            for (pageNum in 1..endPage) {
                if (pageNum > startPage) {
                    processTable(driver, resultsPage, year)
                    val numberedPage = PagedResults(null, MarathonSources.Marines, year, "http://www.marinemarathon.com/results/marathon", pageNum)

                    try {
                        pagedResultsRepository.save(numberedPage)
                        queue.addResultsPage(resultsPage)
                        logger.info("Successfully scraped: $numberedPage")
                    } catch (e: Exception) {
                        logger.error("Failed to record page: $numberedPage", e)
                    }
                }
                driver.click("#xact_results_agegroup_results_next".toCss(), logger)
            }

            return CompletableFuture.completedFuture("Success")
        } catch (e: Exception) {
            logger.error("Unable to scrape year=$year", e)
            return CompletableFuture.completedFuture("Error")
        } finally {
            driverFactory.destroy(driver)
        }
    }

    private fun processTable(driver: RemoteWebDriver, resultPage: MutableList<RunnerData>, year: Int, attempt: Int = 0, maxAttempts: Int = 60) {
        try {
            val tempResultsPage = mutableListOf<RunnerData>()
            val numRows = driver.countTableRows("#xact_results_agegroup_results > tbody".toCss(), logger)
            for (row in 0 until numRows) {
                processRow(driver, row, tempResultsPage, year)
            }
            resultPage.addAll(tempResultsPage)
        } catch (e: Exception) {
            when (e) {
                is IndexOutOfBoundsException -> {
                    Thread.sleep(1000)
                    if (attempt < maxAttempts) {
                        processTable(driver, resultPage, year, attempt + 1)
                    } else {
                        logger.error("Unable to process on page", e)
                        throw e
                    }
                }
                else -> {
                    logger.error("Failed to process the table", e)
                    throw e
                }
            }
        }
    }

    private fun processRow(driver: RemoteWebDriver, row: Int, resultPage: MutableList<RunnerData>, year: Int) {
        try {
            val tbody = "#xact_results_agegroup_results > tbody"
            val ageGender = driver.findCellValue(tbody.toCss(), row, 4, logger)//findCellValue(driver, row, 4)
            val gender = ageGender[0].toString()
            val age = ageGender.substringAfterLast("/")
            val finishTime = driver.findCellValue(tbody.toCss(), row, 5, logger)//findCellValue(driver, row, 5)
            val place = driver.findCellValue(tbody.toCss(), row, 8, logger).toInt() //findCellValue(driver, row, 8).toInt()

            val runnerData = RunnerData(age = age,
                    finishTime = finishTime,
                    gender = gender,
                    marathonYear = year,
                    nationality = "USA",
                    place = place,
                    source = MarathonSources.Marines)
            resultPage.insertRunnerData(logger, age, finishTime, gender, year, "USA", place, MarathonSources.Marines)
            logger.info("Produced: $runnerData")
        } catch (e: Exception) {
            logger.error("Failed to scrape row $row", e)
            throw e
        }
    }

    fun processForm(driver: RemoteWebDriver, year: Int) {
        try {
            driver.get("http://www.marinemarathon.com/results/marathon")
            driver.waitUntilClickable(By.cssSelector("#xact_results_event"))

            val yearText = when (year) {
                2014 -> "2014 39th Marine Corps Marathon"
                2015 -> "2015 40th Marine Corps Marathon"
                2016 -> "2016 41st Marine Corps Marathon"
                2017 -> "2017 42nd Marine Corps Marathon"
                else -> throw IllegalArgumentException("Has to be 2014-2017")
            }
            driver.selectComboBoxOption(By.id("xact_results_event"), yearText)

            driver.waitUntilClickable(By.cssSelector("#ui-id-2"))
            driver.findElementByCssSelector("#ui-id-2").click()

            Thread.sleep(10000)

            driver.waitUntilClickable(By.cssSelector("#xact_results_agegroup_results_length > label > select"))
            driver.selectComboBoxOption(By.cssSelector("#xact_results_agegroup_results_length > label > select"), "100")

            Thread.sleep(10000)
        } catch (e: Exception) {
            logger.error("Failed on input form", e)
            throw e
        }
    }
}

//Completed
@Component
class SanFranciscoScrape(@Autowired private val driverFactory: DriverFactory) {

    private val logger = LoggerFactory.getLogger(SanFranciscoScrape::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, year: Int, url: String): CompletableFuture<String> {
        val driver = driverFactory.createDriver()


        try {
            driver.get(url)

            clickOverall(driver)
            val numPages = findNumPages(driver)

            for (page in 0 until numPages) {
                processPage(driver, queue, year, page)
                advancePage(driver, page)
            }
            return CompletableFuture.completedFuture("Success")
        } catch (e: Exception) {
            logger.error("Failed to scrape $year", e)
            return CompletableFuture.completedFuture("Error")
        } finally {
            driverFactory.destroy(driver)
        }
    }

    private fun processPage(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int, page: Int) {
        try {
            for (row in 1 until findNumRow(driver, page, year)) {
                driver.waitUntilVisible(By.id("result-data"))
                processRow(driver, queue, year, page, row)
            }
        } catch (e: Exception) {
            logger.error("Failed to process page=$page for year=$year", e)
        }
    }

    private fun processRow(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int, page: Int, row: Int) {
        try {
            val place = findCellValue(driver, row, 0, page, year).toInt()
            val nationalityParts = findCellValue(driver, row, 2, page, year).split(",")
            val nationality = if (nationalityParts[1].isBlank()) {
                nationalityParts[0]
            } else {
                "USA"
            }
            val finishTime = findCellValue(driver, row, 4, page, year)
            val genderAge = findCellValue(driver, row, 7, page, year).split("-")
            val gender = genderAge[0]
            val age = genderAge[1]

            val runnerData = RunnerData(
                    age = age,
                    finishTime = finishTime,
                    gender = gender,
                    marathonYear = year,
                    nationality = nationality,
                    place = place,
                    source = MarathonSources.SanFranscisco
            )
            queue.put(runnerData)
            logger.info("Produced: $runnerData")
        } catch (e: Exception) {
            logger.error("Failed to process row=$row on page=$page for year=$year", e)
        }
    }

    private fun findCellValue(driver: RemoteWebDriver, row: Int, cell: Int, page: Int, year: Int): String {
        try {
            return driver.findElementsByCssSelector("#result-data")[1]
                    .findElement(By.tagName("tbody"))
                    .findElements(By.tagName("tr"))[row]
                    .findElements(By.tagName("td"))[cell].text
        } catch (e: Exception) {
            logger.error("Failed to find cell value at [$row][$cell] on page=$page for year=$year", e)
            throw e
        }
    }

    private fun findNumRow(driver: RemoteWebDriver, page: Int, year: Int): Int {
        try {
            driver.waitUntilVisible(By.id("result-data"))
            return driver.findElementsById("result-data")[1]
                    .findElement(By.tagName("tbody"))
                    .findElements(By.tagName("tr")).size
        } catch (e: Exception) {
            logger.error("Failed to determine the number of rows for page=$page, year=$year", e)
            throw e
        }
    }

    private fun advancePage(driver: RemoteWebDriver, page: Int) {
        try {
            driver.findElementByCssSelector("#result-data > tbody > tr:nth-child(2) > td:nth-child(3)")
                    .findElements(By.tagName("a"))[page].click()
        } catch (e: Exception) {
            logger.error("Failed to advance to the next page", e)
        }
    }

    private fun findNumPages(driver: RemoteWebDriver): Int {
        try {
            driver.waitUntilVisible(By.cssSelector("#result-data > tbody > tr:nth-child(2) > td:nth-child(3)"))
            return driver.findElementByCssSelector("#result-data > tbody > tr:nth-child(2) > td:nth-child(3)")
                    .findElements(By.tagName("a")).size + 1
        } catch (e: Exception) {
            logger.error("Unable to count the number of pages", e)
            throw e
        }
    }

    private fun clickOverall(driver: RemoteWebDriver) {
        try {
            driver.waitUntilClickable(By.linkText("Overall"))
            driver.findElementByLinkText("Overall").click()
        } catch (e: Exception) {
            logger.error("Failed to click the overall link", e)
        }
    }
}

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
                PagedResults(source = pagedResults.source, marathonYear = pagedResults.marathonYear, url = pagedResults.url, pageNum = page)
                        .markComplete(pagedResultsRepository, queue, results.toMutableList(), logger)
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

//Completed
@Component
class BudapestScrape(@Autowired private val driverFactory: DriverFactory,
                     @Autowired private val urlPageRepository: UrlPageRepository) {

    private val logger = LoggerFactory.getLogger(BudapestScrape::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, urlPage: UrlPage, columnPositions: ColumnPositions): CompletableFuture<String> {
        val driver = driverFactory.createDriver()
        val resultsPage = mutableListOf<RunnerData>()

        return try {
            driver.get(urlPage.url)

            val numRows = driver.countTableRows(By.cssSelector("body > table:nth-child(7) > tbody:nth-child(1)"), logger)
            for (row in 2 until numRows) {
                processRow(driver, resultsPage, row, urlPage.marathonYear, columnPositions)
            }

            try {
                urlPageRepository.save(urlPage)
                addResultsToQueue(queue, resultsPage)
                logger.info("Successfully scraped: $urlPage")
            } catch (e: Exception) {
                logger.error("Failed to record page: $urlPage", e)
            }

            successResult()
        } catch (e: Exception) {
            logger.error("Failed to scrape $urlPage", e)
            failResult()
        } finally {
            driverFactory.destroy(driver)
        }
    }

    @Synchronized
    private fun addResultsToQueue(queue: BlockingQueue<RunnerData>, resultsPage: MutableList<RunnerData>) {
        queue.addAll(resultsPage)
    }

    private fun processRow(driver: RemoteWebDriver, resultsPage: MutableList<RunnerData>, row: Int, year: Int, columnPositions: ColumnPositions) {
        try {
            val cssSelector = "body > table:nth-child(7) > tbody:nth-child(1)"
            val place = try {
                driver.findCellValue(cssSelector.toCss(), row, columnPositions.place, logger).replace(".", "").toInt()
            } catch (e: Exception) {
                logger.info("Place is not available")
                Int.MAX_VALUE
            }
            val age = try {
                (LocalDateTime.now().year - driver.findCellValue(cssSelector.toCss(), row, columnPositions.age, logger).toInt()).toString()
            } catch (e: Exception) {
                logger.error("Failed to capture age")
                UNAVAILABLE
            }
            val nationality = driver.findCellValue(cssSelector.toCss(), row, columnPositions.nationality, logger)
            val gender = driver.findCellValue(cssSelector.toCss(), row, columnPositions.gender, logger)
            val finishTime = driver.findCellValue(cssSelector.toCss(), row, columnPositions.finishTime, logger)

            resultsPage.insertRunnerData(logger,
                    age, finishTime, gender, year, nationality, place, MarathonSources.Budapest)

        } catch (e: Exception) {
            logger.error("Failed to process row=$row", e)
            throw e
        }
    }
}

//Melbourne Finished
@Component
class MultisportAustraliaScraper(@Autowired private val driverFactory: DriverFactory,
                                 @Autowired private val jsDriver: JsDriver,
                                 @Autowired private val urlPageRepository: UrlPageRepository) {

    private val logger = LoggerFactory.getLogger(MultisportAustraliaScraper::class.java)

    @Async
    fun scrape(runnerDataQueue: LinkedBlockingQueue<RunnerData>, link: UrlPage, source: MarathonSources, columnPositions: ColumnPositions): CompletableFuture<String> {
        val driver = driverFactory.createDriver()

        return try {
            driver.get(link.url)

            val resultsPage = mutableListOf<RunnerData>()
            val tableSelector = if (link.marathonYear == 2017) {
                ".table > tbody:nth-child(2)"
            } else {
                ".ResultsTableBlk > tbody:nth-child(2)"
            }

            val tableText = jsDriver.readTableRows(driver, tableSelector)
            val tableHtml = jsDriver.readTableRows(driver, tableSelector, rawHtml = true)
            for (row in 0 until tableText.size) {
                try {
                    val nationality = if (columnPositions.nationality == -1) {
                        UNAVAILABLE
                    } else {
                        nationality(tableHtml[row][columnPositions.nationality])
                    }
                    val place = try {
                        tableText[row][columnPositions.place].split("                        ")[0].toInt()
                    } catch (e: Exception) {
                        Int.MAX_VALUE
                    }
                    resultsPage.add(createRunnerData(
                            logger = logger,
                            age = tableText[row][columnPositions.age].split("\n")[0].trim(),
                            finishTime = tableText[row][columnPositions.finishTime],
                            gender = tableText[row][columnPositions.gender][0].toString(),
                            source = source,
                            place = place,
                            year = link.marathonYear,
                            nationality = nationality))
                } catch (e: IndexOutOfBoundsException) {
                    logger.error("Index out of bounds", e)
                    throw e
                }
            }

            link.markComplete(urlPageRepository, runnerDataQueue, resultsPage, logger)

            successResult()
        } catch (e: Exception) {
            logger.error("Failed to scrape ${link.url}", e)
            failResult()
        } finally {
            driverFactory.destroy(driver)
        }
    }

    private fun nationality(rawHtml: String): String {
        return try {
            rawHtml.split(" ")[2].replace("alt=", "").replace("\"", "")
        } catch (e: Exception) {
            return UNAVAILABLE
        }
    }
}

@Component
class AthLinksMarathonScraper(@Autowired private val driverFactory: DriverFactory,
                              @Autowired private val jsDriver: AthJsDriver,
                              @Autowired private val stateCodes: List<String>,
                              @Autowired private val runnerDataRepository: RunnerDataRepository,
                              @Autowired private val pagedResultsRepository: PagedResultsRepository) {

    private val logger = LoggerFactory.getLogger(AthLinksMarathonScraper::class.java)
    private val backwardsSelector = "#pager > div:nth-child(1) > div:nth-child(1) > button:nth-child(1)"
    private val firstNextSelector = "#pager > div:nth-child(1) > div:nth-child(6) > button:nth-child(1)"
    private val secondNextSelector = "#pager > div:nth-child(1) > div:nth-child(7) > button:nth-child(1)"

    @Async
    fun scrape(url: String, year: Int, marathonSources: MarathonSources, startPage: Int): CompletableFuture<String> {
        val driver = driverFactory.createDriver()

        return try {
            driver.get(url)
            sleepRandom(2, 5)

            var selector: String
            var page = 1
            while(page < startPage){
                page = advance(driver, page)
            }

            do {
                selector = if(page == 1) { firstNextSelector } else { secondNextSelector }

                scrapePage(driver, url, year, page, marathonSources)

                page = advance(driver, page)
            } while(jsDriver.elementIsPresent(driver, selector))

            scrapePage(driver, url, year, page, marathonSources)

            successResult()
        } catch (e: Exception) {
            logger.error("Failed to scrape $marathonSources, for year=$year, url=$url", e)
            failResult()
        } finally {
            driverFactory.destroy(driver)
        }
    }

    private fun scrapePage(driver: RemoteWebDriver, url : String, year: Int, page: Int, marathonSources: MarathonSources) {
        val pageData = jsDriver.readPage(driver)
        if (pageData.isEmpty()) {
            throw IllegalStateException("pageData is empty on year=$year, page = $page")
        }

        val resultsPage = pageData.map { it ->
            var nationality = it["nationality"]!!
            if(nationality.contains(",")){
                val parts = nationality.split(",")
                nationality = stateCodes.toCountry(parts.last().trim())
            }
            val place = try {
                it["place"]!!.toInt()
            } catch (e : Exception){
                logger.error("Failed to find place", e)
                Int.MAX_VALUE
            }
            createRunnerData(logger,
                    it["age"]!!,
                    it["finishTime"]!!,
                    it["gender"]!!,
                    year,
                    nationality,
                    place,
                    marathonSources)
        }.toList()

        PagedResults(source = marathonSources, marathonYear = year, url = url, pageNum = page)
                .markComplete(pagedResultsRepository, runnerDataRepository, resultsPage, logger)
    }

    private fun advance(driver: RemoteWebDriver, page: Int) : Int {
        val selector = if(page == 1) { firstNextSelector } else { secondNextSelector }
        jsDriver.clickElement(driver, selector)
        resync(driver, page + 1, jsDriver.findCurrentPage(driver))
        return page + 1
    }

    private fun resync(driver: RemoteWebDriver, page: Int, jsPage: Int, attempt : Int = 0, giveUp: Int = 60) {
        logger.info("page = $page, ui page = $jsPage")
        if(jsPage < 0){
            if(attempt < giveUp){
                Thread.sleep(5000)
                resync(driver, page, jsDriver.findCurrentPage(driver), attempt + 1)
            } else {
                val selector = if(page == 1) { firstNextSelector } else { secondNextSelector }
                if(!jsDriver.elementIsPresent(driver, selector)){
                    return
                }
            }
        }

        when {
            page == jsPage -> return
            page < jsPage -> {
                jsDriver.clickElement(driver, backwardsSelector)
                Thread.sleep(5000)
                resync(driver, page, jsDriver.findCurrentPage(driver))
            }
            page > jsPage -> {
                val selector = if(page == 1) { firstNextSelector } else { secondNextSelector }
                jsDriver.clickElement(driver, selector)
                Thread.sleep(5000)
                resync(driver, page, jsDriver.findCurrentPage(driver))
            }
        }
    }
}

abstract class BaseScraper(protected val logger : Logger, protected val runnerDataRepository : RunnerDataRepository,
                           protected val driverFactory: DriverFactory, protected val jsDriver: JsDriver)

abstract class UrlPageScraper(logger: Logger, runnerDataRepository: RunnerDataRepository,
                              driverFactory: DriverFactory, jsDriver: JsDriver,
                              protected val urlPageRepository: UrlPageRepository) : BaseScraper(logger, runnerDataRepository, driverFactory, jsDriver) {

    @Async
    open fun scrape(urlScrapeInfo: UrlScrapeInfo) : CompletableFuture<String> {
        val driver = driverFactory.createDriver()

        return try {
            webscrape(driver, urlScrapeInfo)

            successResult()
        } catch (e : Exception){
            logger.error("Failed to scrape $urlScrapeInfo", e)
            failResult()
        } finally {
            driverFactory.destroy(driver)
        }
    }

    protected abstract fun webscrape(driver: RemoteWebDriver, urlScrapeInfo: UrlScrapeInfo)
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

            resultPage.add(createRunnerData(logger, age, finish, gender, urlScrapeInfo.marathonYear, nationality, place, urlScrapeInfo.source, halfwayTime = half))
        }

        UrlPage(source = urlScrapeInfo.source, marathonYear = urlScrapeInfo.marathonYear, url = urlScrapeInfo.url).markComplete(urlPageRepository, runnerDataRepository, resultPage, logger)
    }
}