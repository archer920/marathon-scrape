package com.stonesoupprogramming.marathonscrape

import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Semaphore

const val UNAVAILABLE = "Unavailable"

@Component
class DriverFactory {

    private val logger = LoggerFactory.getLogger(DriverFactory::class.java)
    private val semaphore = Semaphore(Runtime.getRuntime().availableProcessors() - 2)

    fun createDriver() : RemoteWebDriver {
        return try {
            logger.info("Waiting on Permit")
            semaphore.acquire()
            logger.info("Permit Acquired")

            sleepRandom()
            ChromeDriver()
        } catch (e : Exception){
            when(e){
                is InterruptedException -> {
                    logger.error("Timeout while waiting for driver", e)
                    throw e
                }
                else -> FirefoxDriver()
            }
        }
    }

    fun destroy(driver : RemoteWebDriver){
        try {
            semaphore.release()
            logger.info("Permit has been released")
            driver.close()
            System.gc()
        } catch (e : Exception){
            logger.error("Failed to destroy driver", e)
        }
    }
}

//Complete
@Component
class MedtronicMarathonScraper(@Autowired private val driverFactory : DriverFactory,
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
                    source = Sources.MEDTRONIC)
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
                    source = Sources.BERLIN,
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

//Processing
@Component
class ViennaMarathonScraper(@Autowired private val driverFactory: DriverFactory,
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


            GenderPagedResults(source = Sources.VIENNA, marathonYear = year, url = url, gender = gender).markComplete(genderPagedResultsRepository, queue, resultsPage, logger)

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
            val rowCount = findRowCount(driver)
            for (row in 2 until rowCount step 2) {
                processRow(driver, row, year, gender, resultsPage)
            }
        } catch (e: Exception) {
            logger.error("Failed to process table for year=$year, gender=$gender", e)
            throw e
        }
    }

    private fun processRow(driver: RemoteWebDriver, row: Int, year: Int, gender: Gender, resultsPage: MutableList<RunnerData>) {
        try {
            val place = findCellValue(driver, row, 0).toInt()
            val finishTime = findCellValue(driver, row, 10)
            val nationality = findCellValue(driver, row, 5)
            val age = (LocalDateTime.now().year - (1900 + findCellValue(driver, row, 4).toInt())).toString()
            resultsPage.insertRunnerData(logger, age, finishTime, gender.code, year, nationality, place, Sources.VIENNA)
        } catch (e: Exception) {
            logger.error("Failed to process row=$row, for year=$year, for gender=$gender", e)
            throw e
        }
    }

    private fun findCellValue(driver: RemoteWebDriver, row: Int, cell: Int): String {
        try {
            return driver.findElementsByCssSelector(".resultList > tbody > tr")[row]
                    .findElements(By.tagName("td"))[cell].text
        } catch (e: Exception) {
            logger.error("Failed to get cell value for row=$row, cell=$cell", e)
            throw e
        }
    }

    private fun findRowCount(driver: RemoteWebDriver): Int {
        try {
            driver.waitUntilVisible(By.className("resultList"))
            return driver.findElementsByCssSelector(".resultList > tbody > tr").size
        } catch (e: Exception) {
            logger.error("Unable to find row count", e)
            throw e
        }
    }

    private fun selectCategory(driver: RemoteWebDriver, year: Int, gender: Gender, category: String) {
        try {
            driver.waitUntilVisible(By.cssSelector(category))
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

//TODO: FIXME
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
                for (row in 0 until tableRows.size - 2 step 2){
                    val age = tableRows[row][3].trim()
                    val gender = tableRows[row][4].trim()
                    val nationality = tableRows[row][7].trim()
                    val place = tableRows[row + 1][1].split("/")[0].trim().toInt()
                    val finishTime = tableRows[row + 1][5]

                    resultsPage.insertRunnerData(logger, age, finishTime, gender, year, nationality, place, Sources.BOSTON)
                }

                PagedResults(source = Sources.BOSTON, marathonYear =  year, url = url, pageNum = pageNum)
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

//TODO: Fixme
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
                            source = Sources.CHICAGO,
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

//TODO: Fixme
@Component
class MarathonGuideScraper(@Autowired private val driverFactory: DriverFactory) {

    private val logger = LoggerFactory.getLogger(MarathonGuideScraper::class.java)

    fun findRangeOptionsForUrl(url: String): List<String> {
        val driver = driverFactory.createDriver()

        return try {
            driver.get(url)
            findRangeOptions(driver)
        } catch (e: Exception) {
            logger.error("Unable to get the range options on Marathon Guide", e)
            throw e
        } finally {
            driverFactory.destroy(driver)
        }
    }

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, year: Int, url: String, source: String, columnPositions: ColumnPositions, rangeOption: String): CompletableFuture<String> {
        val driver = driverFactory.createDriver()

        try {
            driver.get(url)

            driver.waitUntilClickable(By.name("RaceRange"))
            driver.selectComboBoxOption(By.cssSelector("select[name=RaceRange]"), rangeOption)
            driver.findElementByName("SubmitButton").click()

            processTable(driver, queue, year, source, columnPositions)

            logger.info("Finished $url, $year, $rangeOption successfully")
            return successResult()
        } catch (e: Exception) {
            logger.error("Failed to scrape $url, $year, $rangeOption, $url", e)
            return failResult()
        } finally {
            driverFactory.destroy(driver)
        }
    }

    private fun processTable(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int, source: String, columnPositions: ColumnPositions) {
        try {
            driver.waitUntilVisible(By.className("BoxTitleOrange"))

            for (row in 2 until countRows(driver)) {
                val ageGender = findCellValue(driver, row, columnPositions.ageGender)
                val gender = ageGender.substringAfter("(")[0].toString()
                val age = ageGender.substringAfter("(").substring(1, 3)
                val place = findCellValue(driver, row, columnPositions.place).toInt()
                val finishTime = findCellValue(driver, row, columnPositions.finishTime)
                val nationality = findCellValue(driver, row, columnPositions.nationality)

                queue.insertRunnerData(
                        logger = logger,
                        age = age,
                        finishTime = finishTime,
                        gender = gender,
                        year = year,
                        nationality = nationality,
                        place = place,
                        source = source
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to process table for $year", e)
        }
    }

    private fun countRows(driver: RemoteWebDriver): Int {
        try {
            return driver.findElementsByCssSelector("body > table:nth-child(119) > tbody > tr:nth-child(1) > td:nth-child(2) > table:nth-child(4) > tbody > tr:nth-child(2) > td > table > tbody > tr > td > table > tbody > tr > td > table > tbody > tr").size
        } catch (e: Exception) {
            logger.error("Unable to determine the number of rows", e)
            throw e
        }
    }

    private fun findCellValue(driver: RemoteWebDriver, row: Int, cell: Int): String {
        try {
            return driver.findElementsByCssSelector("body > table:nth-child(119) > tbody > tr:nth-child(1) > td:nth-child(2) > table:nth-child(4) > tbody > tr:nth-child(2) > td > table > tbody > tr > td > table > tbody > tr > td > table > tbody > tr")[row]
                    .findElements(By.tagName("td"))[cell].text
        } catch (e: Exception) {
            logger.error("Unable to get cell value at $row, $cell", e)
            throw e
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

@Component
class MtecResultScraper(@Autowired private val driverFactory: DriverFactory,
                        @Autowired private val pagedResultsRepository: PagedResultsRepository){

    private val logger = LoggerFactory.getLogger(MtecResultScraper::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, url : String, year : Int, source : String, startPage: Int, endPage: Int) : CompletableFuture<String> {
        val driver = driverFactory.createDriver()

        try {
            driver.get(url)

            driver.click("#quickresults > div > a:nth-child(4)".toCss(), logger)
            driver.click("#searchResults > div > a:nth-child(5)".toCss(), logger)

            for(pageNum in 1 .. endPage){
                if(pageNum > startPage){
                    val resultPage = mutableListOf<RunnerData>()
                    processTable(driver, resultPage, year, source)
                    PagedResults(source = source, marathonYear = year, pageNum = pageNum).markComplete(pagedResultsRepository, queue, resultPage, logger)

                }
                if(pageNum == 1){
                    driver.click("#searchResults > div > a:nth-child(2)".toCss(), logger)
                } else {
                    driver.click("#searchResults > div:nth-child(1) > a:nth-child(3)".toCss(), logger)
                }
            }

            return successResult()
        } catch (e : Exception){
            logger.error("Failed to process year=$year, url=$url", e)
            return failResult()
        } finally {
            driverFactory.destroy(driver)
        }
    }

    private fun processTable(driver: RemoteWebDriver, resultPage: MutableList<RunnerData>, year: Int, source: String) {
        try {
            val numRows = driver.countTableRows("#searchResults > div > div > table > tbody".toCss(), logger)
            for (row in 0 until numRows){
                processRow(driver, row, resultPage, year, source)
            }
        } catch (e : Exception){
            logger.error("Failed to process page", e)
            throw e
        }
    }

    private fun processRow(driver: RemoteWebDriver, row: Int, resultPage: MutableList<RunnerData>, year: Int, source : String) {
        try {
            val tbody = "#searchResults > div > div > table > tbody"
            val gender = driver.findCellValue(tbody.toCss(), row, 2, logger)
            val age = driver.findCellValue(tbody.toCss(), row, 3, logger)
            val finishTime = driver.findCellValue(tbody.toCss(), row, 9, logger)
            val place = driver.findCellValue(tbody.toCss(), row, 6, logger).substringBefore("/").trim().toInt()

            resultPage.insertRunnerData(logger, age, finishTime, gender, year, "USA", place, source)
        } catch (e : Exception){
            logger.error("Failed to process row=$row", e)
            throw e
        }
    }
}

@Component
class TrackShackResults(@Autowired private val driverFactory: DriverFactory,
                        @Autowired private val urlPageRepository: UrlPageRepository,
                        @Autowired private val stateCodes: List<String>) {

    private val logger = LoggerFactory.getLogger(TrackShackResults::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, page: UrlPage, gender : String, columnPositions: ColumnPositions): CompletableFuture<String> {
        val driver = driverFactory.createDriver()

        try {
            driver.get(page.url)
            driver.waitUntilVisible(By.cssSelector("#f1 > p:nth-child(13) > table"))

            val resultsPage = mutableListOf<RunnerData>()
            val rows = driver.countTableRows("#f1 > p:nth-child(13) > table > tbody".toCss(), logger)

            for (row in 2 until rows) {
                val tbody = "#f1 > p:nth-child(13) > table > tbody"

                val place = driver.findCellValue(tbody.toCss(), row, columnPositions.place, logger).toInt()
                val age = driver.findCellValue(tbody.toCss(), row, columnPositions.age, logger)
                var nationality = driver.findCellValue(tbody.toCss(), row, columnPositions.nationality, logger).substringAfterLast(",").trim()
                nationality = stateCodes.toCountry(nationality)
                val finishTime = driver.findCellValue(tbody.toCss(), row, columnPositions.finishTime, logger)

                resultsPage.insertRunnerData(logger, age, finishTime, gender, page.marathonYear, nationality, place, page.source)
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
    fun scrape(queue: BlockingQueue<RunnerData>, year: Int, startPage : Int, endPage : Int): CompletableFuture<String> {
        val driver = driverFactory.createDriver()
        val resultsPage = mutableListOf<RunnerData>()

        try {
            processForm(driver, year)

            for(pageNum in 1 .. endPage){
                if(pageNum > startPage){
                    processTable(driver, resultsPage, year)
                    val numberedPage = PagedResults(null, Sources.MARINES, year, "http://www.marinemarathon.com/results/marathon", pageNum)

                    try {
                        pagedResultsRepository.save(numberedPage)
                        queue.addResultsPage(resultsPage)
                        logger.info("Successfully scraped: $numberedPage")
                    } catch (e : Exception){
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

    private fun processTable(driver: RemoteWebDriver, resultPage : MutableList<RunnerData>, year: Int, attempt : Int = 0, maxAttempts : Int = 60) {
        try {
            val tempResultsPage = mutableListOf<RunnerData>()
            val numRows = driver.countTableRows("#xact_results_agegroup_results > tbody".toCss(), logger)
            for (row in 0 until numRows) {
                processRow(driver, row, tempResultsPage, year)
            }
            resultPage.addAll(tempResultsPage)
        } catch (e: Exception) {
            when(e){
                is IndexOutOfBoundsException -> {
                    Thread.sleep(1000)
                    if(attempt < maxAttempts){
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

    private fun processRow(driver: RemoteWebDriver, row: Int, resultPage : MutableList<RunnerData>, year: Int) {
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
                    source = Sources.MARINES)
            resultPage.insertRunnerData(logger, age, finishTime, gender, year, "USA", place, Sources.MARINES)
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
                    source = Sources.SAN_FRANSCISO
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
class SportStatsScrape(@Autowired private val driverFactory: DriverFactory) {

    private val logger = LoggerFactory.getLogger(SportStatsScrape::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, url: String, year: Int, source: String, numPages: Int, columnPositions: ColumnPositions): CompletableFuture<String> {
        val driver = driverFactory.createDriver()

        return try {
            driver.get(url)

            for (page in 0 until numPages) {
                processPage(driver, queue, page, year, source, columnPositions)
                advancePage(driver)
            }

            successResult()
        } catch (e: Exception) {
            logger.error("Failed to scrape $year on $url", e)
            failResult()
        } finally {
            driverFactory.destroy(driver)
        }
    }

    private fun processPage(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, page: Int, year: Int, source: String, columnPositions: ColumnPositions) {
        try {
            val numRows = driver.countTableRows(By.cssSelector("#mainForm\\:dataTable_data"), logger)
            for (row in 0 until numRows) {
                processRow(driver, queue, year, page, row, source, columnPositions)
            }
        } catch (e: Exception) {
            logger.error("Failed to process page=$page, year=$year, source=$source", e)
        }
    }

    private fun processRow(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int, page: Int, row: Int, source: String, columnPositions: ColumnPositions) =
            try {
                val table = "#mainForm\\:dataTable_data"
                val ageGender = driver.findCellValue(By.cssSelector(table), row, columnPositions.ageGender)
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

                //May not be determinable because of blank values and DNF
                val rank = driver.findCellValue(By.cssSelector(table), row, columnPositions.place)

                val place = when (rank) {
                    "DQ" -> Int.MAX_VALUE
                    "DNF" -> Int.MAX_VALUE
                    else -> rank.toInt()
                }
                val nationality = if (columnPositions.nationality > 0) {
                    driver.findCellValue(By.cssSelector(table), row, columnPositions.nationality)
                } else {
                    UNAVAILABLE
                }
                var finishTime = driver.findCellValue(By.cssSelector(table), row, columnPositions.finishTime)
                finishTime = if (finishTime.isBlank()) {
                    UNAVAILABLE
                } else {
                    finishTime
                }

                try {
                    queue.insertRunnerData(
                            logger,
                            age,
                            finishTime,
                            gender,
                            year,
                            nationality,
                            place,
                            source)
                } catch (e: Exception) {
                    logger.error("Values are ageGender=$ageGender, finishTime=$finishTime, rank=$rank, nationality=$nationality at row=$row on page=$page", e)
                }
            } catch (e: Exception) {
                logger.error("Failed to process row=$row, page=$page, year=$year, source=$source", e)
            }

    private fun RemoteWebDriver.findCellValue(tableBody: By, row: Int, cell: Int): String {
        return try {
            findElement(tableBody)
                    .findElements(By.tagName("tr"))[row]
                    .findElements(By.tagName("td"))[cell]
                    .findElement(By.tagName("span")).text
        } catch (e: Exception) {
            logger.error("Unable to determine value for [$row][$cell]", e)
            throw e
        }
    }

    private fun advancePage(driver: RemoteWebDriver) {
        try {
            driver.findElementByCssSelector(".pagination > li:nth-child(13)").findElement(By.tagName("a")).click()
            Thread.sleep(5000)
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
            } catch (e : Exception){
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
    private fun addResultsToQueue(queue: BlockingQueue<RunnerData>, resultsPage : MutableList<RunnerData>){
        queue.addAll(resultsPage)
    }

    private fun processRow(driver: RemoteWebDriver, resultsPage : MutableList<RunnerData>, row: Int, year: Int, columnPositions: ColumnPositions) {
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
                    age, finishTime, gender, year, nationality, place, Sources.BUDAPEST)

        } catch (e: Exception) {
            logger.error("Failed to process row=$row", e)
            throw e
        }
    }
}