package com.stonesoupprogramming.marathonscrape

import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import java.time.LocalDateTime
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture

private fun RemoteWebDriver.selectComboBoxOption(selector: By, value: String) {
    Select(this.findElement(selector)).selectByVisibleText(value)
}

private fun RemoteWebDriver.waitUntilClickable(selector: By, timeout: Long = 60) {
    WebDriverWait(this, timeout).until(ExpectedConditions.elementToBeClickable(selector))
}

private fun RemoteWebDriver.waitUntilVisible(selector: By, timeout: Long = 60) {
    WebDriverWait(this, timeout).until(ExpectedConditions.visibilityOfElementLocated(selector))
}

private fun RemoteWebDriver.scrollIntoView(selector: By) {
    val elem = this.findElement(selector)
    this.executeScript("arguments[0].scrollIntoView(true);", elem)
    this.waitUntilVisible(selector)
}

private fun WebElement.scrollIntoView(driver: RemoteWebDriver) {
    driver.executeScript("arguments[0].scrollIntoView(true);", this)
    this.waitUntilVisible(driver)
}

private fun WebElement.waitUntilVisible(driver: RemoteWebDriver, timeOut: Long = 60) {
    WebDriverWait(driver, timeOut).until(ExpectedConditions.visibilityOf(this))

}

//TODO: Get rid of this
@Deprecated("The scrape method isn't applicable to all sites so do not use this.")
interface WebScraper {
    @Async
    fun scrape(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int, url: String = "")
}

@Component
class BerlinMarathonScraper {

    private val logger = LoggerFactory.getLogger(BerlinMarathonScraper::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, year : Int) : CompletableFuture<String> {
        sleepRandom()
        val driver = ChromeDriver()

        return try {
            driver.get("https://www.bmw-berlin-marathon.com/en/facts-and-figures/results-archive.html")
            loadYear(driver, year)
            loadCompetition(driver)
            processRows(driver, queue, year)

            successResult()
        } catch (e : Exception){
            logger.error("Failed to scrape Berlin for year = $year", e)
            failResult()
        } finally {
            driver.close()
        }
    }

    private fun processRows(driver: ChromeDriver, queue: BlockingQueue<RunnerData>, year: Int) {
        try {
            driver.waitUntilVisible(By.id("resultGrid"))

            var row = 1
            val totalRows = when(year){
                2017 -> 39234
                2016 -> 35999
                2015 -> 36768
                2014 -> 28946
                else -> throw IllegalArgumentException()
            }
            processRow@while(row < totalRows){
                if(row < 1){
                    row = 1
                }
                if(row > 0){
                    try{
                        advanceRows(driver, row, year)
                    } catch (e : Exception){
                        try {
                            advanceRows(driver, row - 1, year)
                        } catch (e : Exception){
                            row -= 1
                            continue@processRow
                        }
                    }
                }
                row = processRow(driver, queue, row, year)
            }
        } catch (e : Exception){
            logger.error("Failed to process rows for year = $year", e)
        }
    }

    private fun processRow(driver: ChromeDriver, queue: BlockingQueue<RunnerData>, row: Int, year: Int): Int {
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
                    marathonYear =  year,
                    place = place,
                    source = Sources.BERLIN,
                    nationality = nation
            )
            runnerData.updateRaceYearPlace()
            queue.put(runnerData)
            logger.info("Produced $runnerData")

            row + 1
        } catch (e : Exception){
            logger.error("Failed to process row=$row, in year = $year, rolling back", e)
            row - 1
        }
    }

    private fun getCellValue(driver: ChromeDriver, row: Int, cell: Int): String {
        return driver.findElementByCssSelector("#resultGrid > tbody")
                .findElements(By.tagName("tr"))[row]
                .findElements(By.tagName("td"))[cell].text
    }

    private fun advanceRows(driver: ChromeDriver, row : Int, year: Int) {
        try {
            driver.findElementByCssSelector("#resultGrid")
                    .findElement(By.tagName("tbody"))
                    .findElements(By.tagName("tr"))[row].scrollIntoView(driver)
            Thread.sleep(1000)
        } catch (e : Exception){
            logger.error("Failed to advance to next set of rows. row = $row, year = $year", e)
            throw e
        }
    }

    private fun loadCompetition(driver: ChromeDriver) {
        try {
            driver.waitUntilClickable(By.cssSelector("#gridCompetitionChooser"))
            driver.selectComboBoxOption(By.cssSelector("#gridCompetitionChooser"), "Runner")
        } catch (e : Exception){
            logger.error("Failed to load the runner competition", e)
        }
    }

    private fun loadYear(driver: ChromeDriver, year: Int) {
        try {
            driver.waitUntilClickable(By.cssSelector("#gridEventChooser"))
            when (year) {
                2014 -> driver.selectComboBoxOption(By.id("gridEventChooser"), "2014 | 41. BMW BERLIN-MARATHON")
                2015 -> driver.selectComboBoxOption(By.id("gridEventChooser"), "2015 | 42. BMW BERLIN-MARATHON")
                2016 -> driver.selectComboBoxOption(By.id("gridEventChooser"), "2016 | 43. BMW BERLIN-MARATHON")
                2017 -> driver.selectComboBoxOption(By.id("gridEventChooser"), "2017 | 44. BMW BERLIN-MARATHON")
            }
        } catch (e : Exception){
            logger.error("Failed to load year = $year", e)
        }
    }
}

//@Deprecated("FIXME")
//@Component
//@Scope("prototype")
//class BerlinMarathonScraper : WebScraper {
//    private val logger = LoggerFactory.getLogger(BerlinMarathonScraper::class.java)
//
//    override fun scrape(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int, url: String) {
//        try {
//            driver.get(url)
//
//            for (i in 2014 until 2018) {
//                scrapeForYear(driver, queue, year)
//            }
//        } catch (e: Exception) {
//            logger.error("Exception while processing $url", e)
//        } finally {
//            driver.close()
//        }
//    }
//
//    private fun scrapeForYear(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int) {
//        val sw = StopWatch()
//        sw.start()
//        try {
//            driver.waitUntilClickable(By.id("gridEventChooser"))
//            when (year) {
//                2014 -> driver.selectComboBoxOption(By.id("gridEventChooser"), "2014 | 41. BMW BERLIN-MARATHON")
//                2015 -> driver.selectComboBoxOption(By.id("gridEventChooser"), "2015 | 42. BMW BERLIN-MARATHON")
//                2016 -> driver.selectComboBoxOption(By.id("gridEventChooser"), "2016 | 43. BMW BERLIN-MARATHON")
//                2017 -> driver.selectComboBoxOption(By.id("gridEventChooser"), "2017 | 44. BMW BERLIN-MARATHON")
//                else -> throw IllegalArgumentException("Illegal year $year")
//            }
//
//            driver.waitUntilVisible(By.id("gridCompetitionChooser"))
//            driver.waitUntilClickable(By.id("gridCompetitionChooser"))
//            driver.selectComboBoxOption(By.id("gridCompetitionChooser"), "Runner")
//
//            driver.waitUntilVisible(By.id("resultGrid"))
//
//            try {
//                var rowIndex = 1 //The first row doesn't have any data
//                while (rowIndex < 40000) {
//                    val row = driver
//                            .findElementById("resultGrid")
//                            .findElement(By.tagName("tbody"))
//                            .findElements(By.tagName("tr"))[rowIndex]
//                    row.scrollIntoView(driver)
//
//                    val runnerData = RunnerData(
//                            source = Sources.BERLIN,
//                            marathonYear = year,
//                            place = findPlace(driver, rowIndex),
//                            company = findCompany(driver, rowIndex),
//                            nationality = findNationality(driver, rowIndex),
//                            age = findAge(driver, rowIndex),
//                            gender = findGender(driver, rowIndex),
//                            finishTime = findFinishTime(driver, rowIndex))
//
//                    runnerData.updateRaceYearPlace()
//                    queue.put(runnerData)
//                    logger.info("Produced: $runnerData")
//
//                    rowIndex++
//                }
//            } catch (e: Exception) {
//                logger.error(e.toString(), e)
//            }
//        } catch (e: Exception) {
//            logger.error("Exception while scraping year $year")
//        }
//        sw.stop()
//        logger.info("Finished $year in ${sw.totalTimeSeconds} seconds")
//    }
//
//    private fun findFinishTime(driver: RemoteWebDriver, rowIndex: Int): String {
//        return driver
//                .findElementById("resultGrid")
//                .findElement(By.tagName("tbody"))
//                .findElements(By.tagName("tr"))[rowIndex]
//                .findElements(By.tagName("td"))[12].text
//    }
//
//    private fun findGender(driver: RemoteWebDriver, rowIndex: Int): String {
//        return driver
//                .findElementById("resultGrid")
//                .findElement(By.tagName("tbody"))
//                .findElements(By.tagName("tr"))[rowIndex]
//                .findElements(By.tagName("td"))[9].text
//    }
//
//    private fun findPlace(driver: RemoteWebDriver, rowIndex: Int): Int {
//        return driver
//                .findElementById("resultGrid")
//                .findElement(By.tagName("tbody"))
//                .findElements(By.tagName("tr"))[rowIndex]
//                .findElements(By.tagName("td"))[2].text.toInt()
//    }
//
//    private fun findCompany(driver: RemoteWebDriver, rowIndex: Int): String {
//        return driver
//                .findElementById("resultGrid")
//                .findElement(By.tagName("tbody"))
//                .findElements(By.tagName("tr"))[rowIndex]
//                .findElements(By.tagName("td"))[6].text
//    }
//
//    private fun findNationality(driver: RemoteWebDriver, rowIndex: Int): String {
//        return driver
//                .findElementById("resultGrid")
//                .findElement(By.tagName("tbody"))
//                .findElements(By.tagName("tr"))[rowIndex]
//                .findElements(By.tagName("td"))[7].text
//    }
//
//    private fun findAge(driver: RemoteWebDriver, rowIndex: Int): String {
//        return (LocalDateTime.now().year -
//                driver
//                        .findElementById("resultGrid")
//                        .findElement(By.tagName("tbody"))
//                        .findElements(By.tagName("tr"))[rowIndex]
//                        .findElements(By.tagName("td"))[8].text.toInt()).toString()
//    }
//}

@Deprecated("FIXME")
@Component
class ViennaMarathonScrape : WebScraper {

    private val logger = LoggerFactory.getLogger(ViennaMarathonScrape::class.java)

    override fun scrape(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int, url: String) {
        try {

            for (i in 2014..2018) {
                val sw = StopWatch()
                sw.start()
                driver.get(url)

                scraperForYear(driver, queue, i, url)
                sw.stop()
                logger.info("Finished year $i in ${sw.totalTimeSeconds}")
            }
        } catch (e: Exception) {
            logger.error("Failed to scrape Vienna Marathon", e)
        } finally {
            driver.close()
        }
    }

    private fun scraperForYear(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int, url: String) {
        try {
            //scrapeByGender(driver, queue, year, "f") //TODO Uncomment once we have results for men
            scrapeByGender(driver, queue, year, "m")
        } catch (e: Exception) {
            logger.error("Failed to scrape year $year", e)
        }
    }

    private fun scrapeByGender(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int, gender: String) {
        for (i in 0..10) {
            driver.scrollIntoView(By.cssSelector("""select[name="resultYear"""))
            driver.waitUntilClickable(By.cssSelector("""select[name="resultYear"""))
            driver.selectComboBoxOption(By.cssSelector("""select[name="resultYear"""), year.toString())
            driver.waitUntilClickable(By.className("list-group-item"))

            driver.executeScript(buildJs(year, i, gender))

            driver.waitUntilVisible(By.className("resultList"))
            val rows = driver.findElementsByCssSelector(".resultList > tbody > tr").size
            for (row in 1 until rows) {
                if (row % 2 != 0) {
                    continue
                }
                processRow(driver, queue, row, gender, year)
            }
            driver.executeScript("searchResultList()")
        }
    }

    fun buildJs(year: Int, section: Int, gender: String): String {
        val rank = when (section) {
            0 -> "1-499"
            1 -> "500-999"
            2 -> "1000-1499"
            3 -> "1500-1999"
            4 -> "2000-2499"
            5 -> "2500-2999"
            6 -> "3000-3499"
            7 -> "3500-3999"
            8 -> "4000-4499"
            9 -> "5000-5499"
            10 -> "5500-9999"
            else -> throw IllegalArgumentException("Invalid section number")
        }
        return """openResultList('?&wantList=$year&mara=true&rank=$rank&gender=$gender')"""
    }

    private fun processRow(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, row: Int, gender: String, year: Int) {
        val place = findCellValue(driver, row, 0).toInt()
        val age = LocalDateTime.now().year - ("19${findCellValue(driver, row, 4)}").toInt()
        val finishTime = findCellValue(driver, row, 9)
        val nationality = findCellValue(driver, row, 5)
        val g = when (gender) {
            "f" -> "W"
            "m" -> "M"
            else -> ""
        }
        val runnerData = RunnerData(age = age.toString(),
                finishTime = finishTime,
                gender = g,
                marathonYear = year,
                nationality = nationality,
                place = place,
                source = Sources.VIENNA)
        runnerData.updateRaceYearPlace()
        queue.put(runnerData)
        logger.info("Produced: $runnerData")
    }

    private fun findCellValue(driver: RemoteWebDriver, row: Int, cell: Int): String {
        return driver.findElementsByCssSelector(".resultList > tbody > tr")[row].findElements(By.tagName("td"))[cell].text
    }
}

@Component
class BostonMarathonScrape {

    private val logger = LoggerFactory.getLogger(BostonMarathonScrape::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, year: Int): CompletableFuture<String> {
        sleepRandom()

        val driver = ChromeDriver()

        try {
            driver.get("http://registration.baa.org/cfm_Archive/iframe_ArchiveSearch.cfm")
            driver.waitUntilClickable(By.cssSelector("select[name=RaceYearLowID"))
            driver.selectComboBoxOption(By.cssSelector("select[name=RaceYearLowID]"), year.toString())
            driver.selectComboBoxOption(By.cssSelector("select[name=RaceYearHighID]"), year.toString())

            driver.scrollIntoView(By.className("submit_button"))
            driver.findElementByCssSelector(".form_submit_pad > .submit_button").click()

            driver.waitUntilVisible(By.cssSelector("input[name=next]"))

            while (next25Present(driver)) {
                driver.waitUntilVisible(By.className("tablegrid_list"), timeout = 60)

                for (i in 0 until numRows(driver)) {
                    val marathonYear = trHeaderCellValue(driver, i, 0).toInt()
                    val age = trHeaderCellValue(driver, i, 3)
                    val gender = trHeaderCellValue(driver, i, 4)
                    val country = trHeaderCellValue(driver, i, 7)
                    val finishTime = infoGridCellValue(driver, i, 4)
                    val place = infoGridCellValue(driver, i, 0).split("/")[0].trim().toInt()

                    val runnerData = RunnerData(marathonYear = marathonYear,
                            place = place,
                            source = Sources.BOSTON,
                            age = age,
                            gender = gender,
                            nationality = country,
                            finishTime = finishTime)
                    runnerData.updateRaceYearPlace()
                    queue.put(runnerData)
                    logger.info("Produced: $runnerData")
                }
                driver.findElementByCssSelector("input[name=next]").click()
            }
            return CompletableFuture.completedFuture("Success")
        } catch (e: Exception) {
            logger.error("Failed to scrape Boston Marathon year = $year")
            return CompletableFuture.completedFuture("Error")
        } finally {
            driver.close()
        }
    }

    private fun trHeaderCellValue(driver: RemoteWebDriver, row: Int, cell: Int): String {
        return driver.findElementsByClassName("tr_header")[row].findElements(By.tagName("td"))[cell].text
    }

    private fun infoGridCellValue(driver: RemoteWebDriver, row: Int, cell: Int): String {
        return driver.findElementsByCssSelector(".table_infogrid")[row]
                .findElements(By.tagName("tr"))[1]
                .findElements(By.tagName("td"))[cell].text
    }

    private fun numRows(driver: RemoteWebDriver): Int {
        return driver.findElementsByClassName("tr_header").size
    }

    private fun next25Present(driver: RemoteWebDriver): Boolean {
        return try {
            driver.findElementByCssSelector("input[name=next]")
            true
        } catch (e: Exception) {
            logger.trace("Element wasn't present", e)
            false
        }
    }
}

@Component
class ChicagoMarathonScrape {

    private val logger = LoggerFactory.getLogger(ChicagoMarathonScrape::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, year: Int, gender: String) : CompletableFuture<String> {
        sleepRandom()

        val driver = ChromeDriver()

        try {
            val url = "http://chicago-history.r.mikatiming.de/2015/"
            when(gender) {
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
            driver.close()
        }
    }

    @Async
    fun scrape2017(queue: BlockingQueue<RunnerData>, gender: String) : CompletableFuture<String> {
        sleepRandom()

        val driver = ChromeDriver()

        try {
            val url = "http://results.chicagomarathon.com/2017/"
            when(gender){
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
            driver.close()
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
                    runnerData.updateRaceYearPlace()
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

@Component
class NyMarathonScraper {

    private val logger = LoggerFactory.getLogger(NyMarathonScraper::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, year: Int, url: String) : CompletableFuture<String> {
        val driver = ChromeDriver()
        sleepRandom()

        try {
            driver.get(url)
            val rangeOptions = findRangeOptions(driver)
            for (range in rangeOptions) {
                driver.waitUntilClickable(By.name("RaceRange"))
                driver.selectComboBoxOption(By.cssSelector("select[name=RaceRange]"), range)

                driver.findElementByName("SubmitButton").click()
                when (year) {
                    2014 -> processTablePlaceFirst(driver, queue, year)
                    2015 -> processTableTimeFirst(driver, queue, year)
                    2016 -> processTableTimeFirst(driver, queue, year)
                    2017 -> processTablePlaceFirst(driver, queue, year)
                }

                driver.navigate().back()
            }
            return successResult()
        } catch (e: Exception) {
            logger.error("Failed to scrape $year, $url", e)
            return failResult()
        } finally {
            driver.close()
        }
    }

    private fun processTableTimeFirst(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int) {
        try {
            driver.waitUntilVisible(By.className("BoxTitleOrange"))

            for (row in 2 until countRows(driver)) {
                val ageGender = findCellValue(driver, row, 0)
                val gender = ageGender.substringAfter("(")[0].toString()
                val age = ageGender.substringAfter("(").substring(1, 3)
                val place = findCellValue(driver, row, 2).toInt()
                val finishTime = findCellValue(driver, row, 1)
                val nationality = findCellValue(driver, row, 5)

                val runnerData = RunnerData(
                        source = Sources.NY_MARATHON_GUIDE,
                        marathonYear = year,
                        gender = gender,
                        age = age,
                        place = place,
                        finishTime = finishTime,
                        nationality = nationality)
                runnerData.updateRaceYearPlace()
                queue.put(runnerData)
                logger.info("Produced: $runnerData")
            }
        } catch (e: Exception) {
            logger.error("Failed to process table for $year", e)
        }
    }

    private fun processTablePlaceFirst(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int) {
        try {
            driver.waitUntilVisible(By.className("BoxTitleOrange"))

            for (row in 2 until countRows(driver)) {
                val ageGender = findCellValue(driver, row, 0)
                val gender = ageGender.substringAfter("(")[0].toString()
                val age = ageGender.substringAfter("(").substring(1, 3)

                val place = try {
                    findCellValue(driver, row, 1).toInt()
                } catch (e: Exception) {
                    logger.error("Row=$row, Cell=1, Year=$year", e)
                    throw e
                }
                val finishTime = findCellValue(driver, row, 4)
                val nationality = findCellValue(driver, row, 5).substringAfterLast(",").trim()

                val runnerData = RunnerData(
                        source = Sources.NY_MARATHON_GUIDE,
                        marathonYear = year,
                        gender = gender,
                        age = age,
                        place = place,
                        finishTime = finishTime,
                        nationality = nationality)
                runnerData.updateRaceYearPlace()
                queue.put(runnerData)
                logger.info("Produced: $runnerData")
            }
        } catch (e: Exception) {
            logger.error("Failed to process table year=$year", e)
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
class LaMarathonScrape(@Autowired private val stateCodes: List<String>) {

    private val logger = LoggerFactory.getLogger(LaMarathonScrape::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, url: String, year: Int, gender: String) : CompletableFuture<String> {
        val driver = ChromeDriver()
        sleepRandom()

        try {
            driver.get(url)
            driver.waitUntilVisible(By.cssSelector("#f1 > p:nth-child(13) > table"))

            for (row in 2 until numRows(driver)) {
                val place = findCellValue(driver, row, 4).toInt()
                val age = findCellValue(driver, row, 3)
                var nationality = findCellValue(driver, row, 16).substringAfterLast(",").trim()
                if (stateCodes.contains(nationality)) {
                    nationality = "USA"
                }
                val finishTime = findCellValue(driver, row, 15)

                val runnerData = RunnerData(age = age,
                        finishTime = finishTime,
                        gender = gender,
                        marathonYear = year,
                        nationality = nationality,
                        place = place,
                        source = Sources.LA)
                runnerData.updateRaceYearPlace()
                queue.put(runnerData)
                logger.info("Produced: $runnerData")
            }
            return successResult()
        } catch (e: Exception) {
            logger.error("Unable to scrape $url")
            return failResult()
        } finally {
            driver.close()
        }
    }

    @Async
    fun scrape2014(queue: BlockingQueue<RunnerData>) : CompletableFuture<String> {
        val driver = ChromeDriver()
        sleepRandom()

        val url = "https://www.mtecresults.com/race/show/2074/2014_LA_Marathon-ASICS_LA_Marathon"
        try {
            driver.get(url)

            driver.waitUntilClickable(By.cssSelector("#quickresults > div > a:nth-child(4)"))
            driver.findElementByCssSelector("#quickresults > div > a:nth-child(4)").click()

            driver.waitUntilClickable(By.cssSelector("#searchResults > div > a:nth-child(5)"))
            driver.findElementByCssSelector("#searchResults > div > a:nth-child(5)").click()

            var keepScraping = true
            do {
                process2014Table(driver, queue)
                if (hasNextPage(driver)) {
                    nextPage(driver)
                } else {
                    keepScraping = false
                }
            } while (keepScraping)
            return successResult()
        } catch (e: Exception) {
            logger.error("Unable to scrape $url", e)
            return failResult()
        } finally {
            driver.close()
        }
    }

    private fun process2014Table(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>) {
        for (row in 0 until find2014RowCount(driver)) {
            val gender = find2014CellValue(driver, row, 2)
            val age = find2014CellValue(driver, row, 3)
            val finishTime = find2014CellValue(driver, row, 9)
            val place = find2014CellValue(driver, row, 6).substringBefore("/").trim().toInt()

            val runnerData = RunnerData(
                    age = age,
                    finishTime = finishTime,
                    gender = gender,
                    marathonYear = 2014,
                    nationality = "USA",
                    place = place,
                    source = Sources.LA)
            runnerData.updateRaceYearPlace()
            queue.put(runnerData)
            logger.info("Produced: $runnerData")
        }
    }

    private fun find2014RowCount(driver: RemoteWebDriver): Int {
        try {
            driver.waitUntilVisible(By.cssSelector("#searchResults > div > div > table > tbody"))
            return driver.findElementByCssSelector("#searchResults > div > div > table > tbody")
                    .findElements(By.tagName("tr")).size
        } catch (e: Exception) {
            logger.error("Unable to count the number of rows", e)
            throw e
        }
    }

    private fun find2014CellValue(driver: RemoteWebDriver, row: Int, col: Int): String {
        try {
            return driver.findElementByCssSelector("#searchResults > div > div > table > tbody")
                    .findElements(By.tagName("tr"))[row]
                    .findElements(By.tagName("td"))[col].text
        } catch (e: Exception) {
            logger.error("Unable to get value for $row, $col", e)
            throw e
        }
    }

    private fun hasNextPage(driver: RemoteWebDriver): Boolean {
        try {
            return driver.findElementsByCssSelector("#searchResults > div > a:nth-child(2)").isEmpty()
        } catch (e: Exception) {
            logger.error("Can't determine if there was a another page", e)
            throw e
        }
    }

    private fun nextPage(driver: RemoteWebDriver) {
        try {
            driver.waitUntilClickable(By.cssSelector("#searchResults > div > a:nth-child(2)"))
            driver.findElementByCssSelector("#searchResults > div > a:nth-child(2)").click()
        } catch (e: Exception) {
            logger.error("Unable to advance to the next page", e)
        }
    }


    fun numRows(driver: RemoteWebDriver): Int {
        try {
            return driver.findElementByCssSelector("#f1 > p:nth-child(13) > table > tbody")
                    .findElements(By.tagName("tr")).size
        } catch (e: Exception) {
            logger.error("Unable to find the number of rows", e)
            throw e
        }
    }

    fun findCellValue(driver: RemoteWebDriver, row: Int, cell: Int): String {
        try {
            return driver.findElementByCssSelector("#f1 > p:nth-child(13) > table > tbody")
                    .findElements(By.tagName("tr"))[row]
                    .findElements(By.tagName("td"))[cell].text
        } catch (e: Exception) {
            logger.error("Failed to determine cell value at row=$row, cell=$cell", e)
            throw e
        }
    }
}

@Component
class MarineCorpsScrape {
    private val logger = LoggerFactory.getLogger(MarineCorpsScrape::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, year: Int): CompletableFuture<String> {
        val driver = ChromeDriver()
        sleepRandom()

        try {
            processForm(driver, year)

            var scrape = true
            do {
                processTable(driver, queue, year)

                if (hasNextPage(driver)) {
                    advancePage(driver)
                } else {
                    scrape = false
                }
            } while (scrape)

            return CompletableFuture.completedFuture("Success")
        } catch (e: Exception) {
            logger.error("Unable to scrape", e)
            return CompletableFuture.completedFuture("Error")
        } finally {
            driver.close()
        }
    }

    private fun processTable(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int) {
        try {
            for (row in 0 until numRows(driver)) {
                processRow(driver, row, queue, year)
            }
        } catch (e: Exception) {
            logger.error("Failed to process the table", e)
        }
    }

    private fun processRow(driver: RemoteWebDriver, row: Int, queue: BlockingQueue<RunnerData>, year: Int) {
        try {
            val ageGender = findCellValue(driver, row, 4)
            val gender = ageGender[0].toString()
            val age = ageGender.substringAfterLast("/")
            val finishTime = findCellValue(driver, row, 5)
            val place = findCellValue(driver, row, 8).toInt()

            val runnerData = RunnerData(age = age,
                    finishTime = finishTime,
                    gender = gender,
                    marathonYear = year,
                    nationality = "USA",
                    place = place,
                    source = Sources.MARINES)
            runnerData.updateRaceYearPlace()
            queue.put(runnerData)
            logger.info("Produced: $runnerData")
        } catch (e: Exception) {
            logger.error("Failed to scrape row $row", e)
        }
    }

    private fun findCellValue(driver: RemoteWebDriver, row: Int, cell: Int): String {
        try {
            return driver.findElementByCssSelector("#xact_results_agegroup_results > tbody")
                    .findElements(By.tagName("tr"))[row]
                    .findElements(By.tagName("td"))[cell].text
        } catch (e: Exception) {
            logger.error("Failed to get cell value for row=$row, cell=$cell", e)
            throw e
        }
    }

    private fun advancePage(driver: RemoteWebDriver) {
        try {
            driver.waitUntilClickable(By.cssSelector("#xact_results_agegroup_results_next"))
            driver.findElementByCssSelector("#xact_results_agegroup_results_next").click()
        } catch (e: Exception) {
            logger.error("Failed to advance page", e)
        }
    }

    private fun numRows(driver: RemoteWebDriver): Int {
        try {
            return driver.findElementByCssSelector("#xact_results_agegroup_results > tbody")
                    .findElements(By.tagName("tr")).size
        } catch (e: Exception) {
            logger.error("Unable to find the number of rows", e)
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
        }
    }

    fun hasNextPage(driver: RemoteWebDriver): Boolean {
        try {
            return !driver.findElementByCssSelector("#xact_results_agegroup_results_next").getAttribute("class").contains("ui-state-disabled")
        } catch (e: Exception) {
            logger.error("Unable to determine if there is another page", e)
            throw e
        }
    }
}

@Component
class SanFranciscoScrape {

    private val logger = LoggerFactory.getLogger(SanFranciscoScrape::class.java)

    @Async
    fun scrape(queue: BlockingQueue<RunnerData>, year: Int, url: String): CompletableFuture<String> {
        val driver = ChromeDriver()
        sleepRandom()

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
            driver.close()
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
            runnerData.updateRaceYearPlace()
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
                    .findElements(By.tagName("a")).size
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

