package com.stonesoupprogramming.marathonscrape

import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.support.ui.Select
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import java.time.LocalDateTime
import java.util.concurrent.BlockingQueue
import javax.validation.ConstraintViolationException

const val END_OF_SCRAPE = "End of Scrape"

private fun RemoteWebDriver.selectComboBoxOption(selector: By, value : String){
    Select(this.findElement(selector)).selectByVisibleText(value)
}

private fun RemoteWebDriver.waitUntilClickable(selector: By, timeout : Long = 60){
    WebDriverWait(this, timeout).until(ExpectedConditions.elementToBeClickable(selector))
}

private fun RemoteWebDriver.waitUntilVisible(selector : By, timeout : Long = 60){
    WebDriverWait(this, timeout).until(ExpectedConditions.visibilityOfElementLocated(selector))
}

private fun RemoteWebDriver.scrollIntoView(selector: By){
    val elem = this.findElement(selector)
    this.executeScript("arguments[0].scrollIntoView(true);", elem)
    this.waitUntilVisible(selector)
}

private fun WebElement.scrollIntoView(driver: RemoteWebDriver){
    driver.executeScript("arguments[0].scrollIntoView(true);", this)
    this.waitUntilVisible(driver)
}

private fun WebElement.waitUntilVisible(driver: RemoteWebDriver, timeOut : Long = 60){
    WebDriverWait(driver, timeOut).until(ExpectedConditions.visibilityOf(this))

}

//TODO: Get rid of this
@Deprecated("The scrape method isn't applicable to all sites so do not use this.")
interface WebScraper {
    @Async
    fun scrape(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int, url: String = "")
}

@Component
@Scope("prototype")
class NyWebScraper : WebScraper {

    private val logger = LoggerFactory.getLogger(NyWebScraper::class.java)

    @Async
    override fun scrape(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int, url: String) {
        try {
            driver.get(url)
            var showMore = true
            var page = 0

            try {
                driver.findElementByClassName("footer-rms-ads-close").click()
            } catch (e : Exception){
                logger.error("Unable to dismiss ad", e)
            }

            while (showMore) {
                try {
                    val elements = driver.findElementsByClassName("cmd-finisher")
                    elements.forEach {
                        val parts = it.text.split("\n")
                        val place = parts[8].replace(",", "").toInt()
                        val runnerData = RunnerData(
                                age = age(parts[1]),
                                gender = gender(parts[1]),
                                nationality = nationality(parts[1]),
                                finishTime = parts[4],
                                marathonYear = year,
                                source = Sources.NY,
                                place = place)
                        runnerData.updateRaceYearPlace()
                        queue.put(runnerData)
                        logger.info("Produced: $runnerData")
                    }

                    val button = driver.findElementsByClassName("button-load-more")
                    showMore = button.size == 2
                    if (showMore) {
                        driver.executeScript("arguments[0].scrollIntoView(true);", button[1])
                        WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(button[1]))
                        button[1].click()

                        logger.info("Advanced to page $page")
                        page++
                    }
                } catch (e: Exception) {
                    logger.error("Exception while advancing", e)
                    showMore = false
                }
            }
            queue.put(RunnerData(source = END_OF_SCRAPE))
            logger.info("Scraping $year completed")
        } catch (e : Exception){
            logger.error("Exception while processing $url", e)
        } finally {
            driver.close()
        }
    }

    private fun gender(input: String) = input.substring(0, 1)
    private fun age(input: String) = input.substring(1, 3)
    private fun nationality(input: String) =
            when {
                input.contains("USA") -> "USA"
                else -> input.substring(4, 7)
            }
}

@Component
@Scope("prototype")
class BerlinMarathonScraper : WebScraper{
    private val logger = LoggerFactory.getLogger(BerlinMarathonScraper::class.java)

    override fun scrape(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int, url: String) {
        try {
            driver.get(url)

            for(i in 2014 until 2018){
                scrapeForYear(driver, queue, year)
            }
        } catch (e : Exception){
            logger.error("Exception while processing $url", e)
        } finally {
            driver.close()
        }
    }

    private fun scrapeForYear(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int){
        val sw = StopWatch()
        sw.start()
        try {
            driver.waitUntilClickable( By.id("gridEventChooser"))
            when(year){
                2014 -> driver.selectComboBoxOption( By.id("gridEventChooser"),"2014 | 41. BMW BERLIN-MARATHON")
                2015 -> driver.selectComboBoxOption( By.id("gridEventChooser"),"2015 | 42. BMW BERLIN-MARATHON")
                2016 -> driver.selectComboBoxOption( By.id("gridEventChooser"), "2016 | 43. BMW BERLIN-MARATHON")
                2017 -> driver.selectComboBoxOption( By.id("gridEventChooser"), "2017 | 44. BMW BERLIN-MARATHON")
                else -> throw IllegalArgumentException("Illegal year $year")
            }

            driver.waitUntilVisible(By.id("gridCompetitionChooser"))
            driver.waitUntilClickable(By.id("gridCompetitionChooser"))
            driver.selectComboBoxOption(By.id("gridCompetitionChooser"), "Runner")

            driver.waitUntilVisible(By.id("resultGrid"))

            try{
                var rowIndex = 1 //The first row doesn't have any data
                while(rowIndex < 40000){
                    val row = driver
                            .findElementById("resultGrid")
                            .findElement(By.tagName("tbody"))
                            .findElements(By.tagName("tr"))[rowIndex]
                    row.scrollIntoView(driver)

                    val runnerData = RunnerData(
                            source = Sources.BERLIN,
                            marathonYear = year,
                            place = findPlace(driver, rowIndex),
                            company = findCompany(driver, rowIndex),
                            nationality = findNationality(driver, rowIndex),
                            age = findAge(driver, rowIndex),
                            gender = findGender(driver, rowIndex),
                            finishTime = findFinishTime(driver, rowIndex))

                    runnerData.updateRaceYearPlace()
                    queue.put(runnerData)
                    logger.info("Produced: $runnerData")

                    rowIndex++
                }
            } catch (e : Exception){
                logger.error(e.toString(), e)
            }
        } catch (e : Exception){
            logger.error("Exception while scraping year $year")
        }
        sw.stop()
        logger.info("Finished $year in ${sw.totalTimeSeconds} seconds")
    }

    private fun findFinishTime(driver: RemoteWebDriver, rowIndex: Int) : String {
        return driver
                .findElementById("resultGrid")
                .findElement(By.tagName("tbody"))
                .findElements(By.tagName("tr"))[rowIndex]
                .findElements(By.tagName("td"))[12].text
    }

    private fun findGender(driver: RemoteWebDriver, rowIndex: Int) : String {
        return driver
                .findElementById("resultGrid")
                .findElement(By.tagName("tbody"))
                .findElements(By.tagName("tr"))[rowIndex]
                .findElements(By.tagName("td"))[9].text
    }

    private fun findPlace(driver : RemoteWebDriver, rowIndex : Int) : Int {
        return driver
                .findElementById("resultGrid")
                .findElement(By.tagName("tbody"))
                .findElements(By.tagName("tr"))[rowIndex]
                .findElements(By.tagName("td"))[2].text.toInt()
    }

    private fun findCompany(driver: RemoteWebDriver, rowIndex: Int) : String {
        return driver
                .findElementById("resultGrid")
                .findElement(By.tagName("tbody"))
                .findElements(By.tagName("tr"))[rowIndex]
                .findElements(By.tagName("td"))[6].text
    }

    private fun findNationality(driver: RemoteWebDriver, rowIndex: Int) : String{
        return driver
                .findElementById("resultGrid")
                .findElement(By.tagName("tbody"))
                .findElements(By.tagName("tr"))[rowIndex]
                .findElements(By.tagName("td"))[7].text
    }

    private fun findAge(driver: RemoteWebDriver, rowIndex: Int) : String {
        return (LocalDateTime.now().year -
                driver
                .findElementById("resultGrid")
                .findElement(By.tagName("tbody"))
                .findElements(By.tagName("tr"))[rowIndex]
                .findElements(By.tagName("td"))[8].text.toInt()).toString()
    }
}

@Component
class ViennaMarathonScrape : WebScraper {

    private val logger = LoggerFactory.getLogger(ViennaMarathonScrape::class.java)

    override fun scrape(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int, url: String) {
        try {

            for (i in 2014..2018){
                val sw = StopWatch()
                sw.start()
                driver.get(url)

                scraperForYear(driver, queue, i, url)
                sw.stop()
                logger.info("Finished year $i in ${sw.totalTimeSeconds}")
            }
        } catch (e : Exception) {
            logger.error("Failed to scrape Vienna Marathon", e)
        }
    }

    private fun scraperForYear(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int, url : String) {
        try {
            //scrapeByGender(driver, queue, year, "f") //TODO Uncomment once we have results for men
            scrapeByGender(driver, queue, year, "m")
        } catch (e : Exception){
            logger.error("Failed to scrape year $year", e)
        }
    }

    private fun scrapeByGender(driver : RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int, gender: String){
        for(i in 0..10){
            driver.scrollIntoView(By.cssSelector("""select[name="resultYear"""))
            driver.waitUntilClickable(By.cssSelector("""select[name="resultYear"""))
            driver.selectComboBoxOption(By.cssSelector("""select[name="resultYear"""), year.toString())
            driver.waitUntilClickable(By.className("list-group-item"))

            driver.executeScript(buildJs(year, i, gender))

            driver.waitUntilVisible(By.className("resultList"))
            val rows = driver.findElementsByCssSelector(".resultList > tbody > tr").size
            for (row in 1 until rows){
                if(row % 2 != 0){
                    continue
                }
                processRow(driver, queue, row, gender, year)
            }
            driver.executeScript("searchResultList()")
        }
    }

    fun buildJs(year: Int, section: Int, gender: String) : String {
        val rank = when(section){
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

    private fun processRow(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, row: Int, gender : String, year : Int) {
        val place = findCellValue(driver, row, 0).toInt()
        val age = LocalDateTime.now().year - ("19${findCellValue(driver, row, 4)}").toInt()
        val finishTime = findCellValue(driver, row, 9)
        val nationality = findCellValue(driver, row, 5)
        val g = when(gender){
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

    private fun findCellValue(driver: RemoteWebDriver, row: Int, cell: Int) : String {
        return driver.findElementsByCssSelector(".resultList > tbody > tr")[row].findElements(By.tagName("td"))[cell].text
    }
}

@Component
class BostonMarathonScrape : WebScraper {

    private val logger = LoggerFactory.getLogger(BostonMarathonScrape::class.java)

    override fun scrape(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int, url: String) {
        try {
            for(y in 2014..2018){
                try {
                    driver.get(url)
                    driver.waitUntilClickable(By.cssSelector("select[name=RaceYearLowID"))
                    driver.selectComboBoxOption(By.cssSelector("select[name=RaceYearLowID]"), y.toString())
                    driver.selectComboBoxOption(By.cssSelector("select[name=RaceYearHighID]"), y.toString())

                    driver.scrollIntoView(By.className("submit_button"))
                    driver.findElementByCssSelector(".form_submit_pad > .submit_button").click()

                    driver.waitUntilVisible(By.cssSelector("input[name=next]"))

                    while(next25Present(driver)){
                        driver.waitUntilVisible(By.className("tablegrid_list"), timeout = 60)

                        for (i in 0 until numRows(driver)){
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
                                    nationality =  country,
                                    finishTime = finishTime)
                            runnerData.updateRaceYearPlace()
                            queue.put(runnerData)
                            logger.info("Produced: $runnerData")
                        }
                        driver.findElementByCssSelector("input[name=next]").click()
                    }
                } catch (e : Exception){
                    logger.error("Failed to scrape Boston Marathon year = $y")
                }
            }
        } catch (e : Exception){
            logger.error("Failed to scrape Boston Marathon", e)
        }

    }

    private fun trHeaderCellValue(driver: RemoteWebDriver, row : Int, cell : Int) : String {
        return driver.findElementsByClassName("tr_header")[row].findElements(By.tagName("td"))[cell].text
    }

    private fun infoGridCellValue(driver: RemoteWebDriver, row : Int, cell: Int) : String {
        return driver.findElementsByCssSelector(".table_infogrid")[row]
                .findElements(By.tagName("tr"))[1]
                .findElements(By.tagName("td"))[cell].text
    }

    private fun numRows(driver: RemoteWebDriver) : Int {
        return driver.findElementsByClassName("tr_header").size
    }

    private fun next25Present(driver: RemoteWebDriver) : Boolean {
        return try {
            driver.findElementByCssSelector("input[name=next]")
            true
        } catch (e : Exception){
            logger.trace("Element wasn't present", e)
            false
        }
    }
}

@Component
class ChicagoMarathonScrape {

    private val logger = LoggerFactory.getLogger(ChicagoMarathonScrape::class.java)

    @Async
    fun scrape(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int, url: String) {
        try {
            yearForm(driver, "Men", year, url)
            processTable(driver, queue, "M", year)

            yearForm(driver, "Women", year, url)
            processTable(driver, queue, "W", year)

            logger.info("")
        } catch (e : Exception){
            logger.error("Failed to scrape Chicago", e)
        }
    }

    @Async
    fun scrape2017(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>){
        try {
            val url = "http://results.chicagomarathon.com/2017/"
            yearForm2017(driver, "Men", url)
            processTable(driver, queue, "M", 2017)

            yearForm2017(driver, "Women", url)
            processTable(driver, queue, "W", 2017)
        } catch (e : Exception){
            logger.error("Failed to process 2017", e)
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
        } catch (e : Exception){
            logger.error("Unable to set form", e)
        }
    }

    private fun processTable(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, gender: String, year: Int){
        try {
            var firstPage = true
            var keepScraping = true
            do {
                driver.waitUntilVisible(By.className("list-table"))
                for (i in 0 until numRows(driver)){
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
                if(hasNextButton(driver, firstPage)){
                    clickNext(driver, firstPage)
                    firstPage = false
                } else {
                    keepScraping = false
                }
            } while (keepScraping)
        } catch (e : Exception){
            logger.error("Failed to process the results", e)
        }
    }

    private fun findCellValue(driver: RemoteWebDriver, row : Int, cell : Int) : String {
        try {
            return driver.findElementsByCssSelector(".list-table > tbody > tr")[row]
                    .findElements(By.tagName("td"))[cell].text
        } catch (e : Exception){
            logger.error("Failed to get value for $row, $cell", e)
            throw e
        }
    }

    private fun numRows(driver: RemoteWebDriver) : Int {
        try {
            return driver.findElementsByCssSelector(".list-table > tbody > tr").size
        } catch (e : Exception){
            logger.error("Failed to get the number of rows", e)
            throw e
        }
    }

    private fun yearForm(driver: RemoteWebDriver, gender: String, year: Int, url: String){
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
        } catch (e : Exception){
            logger.error("Failed to select a year", e)
        }
    }

    private fun hasNextButton(driver: RemoteWebDriver, firstPage: Boolean) : Boolean {
        val size = if(firstPage) { 1 } else { 2 }
        return try {
            driver.findElementsByClassName("pages-nav-button").size == size
        } catch (e : Exception){
            logger.error("Unable to determine if there is a next button", e)
            false
        }
    }

    private fun clickNext(driver: RemoteWebDriver, firstPage: Boolean) {
        val index = if(firstPage) { 0 } else { 1 }
        try {
            driver.scrollIntoView(By.className("pages-nav-button"))
            driver.waitUntilClickable(By.className("pages-nav-button"))
            if(index == 0){
                driver.findElementByClassName("pages-nav-button").click()
            } else {
                driver.findElementsByClassName("pages-nav-button")[index].click()
            }
        } catch (e : Exception){
            logger.error("Failed to click next", e)
        }
    }
}

@Component
@Scope("prototype")
class RunnerDataConsumer(@Autowired private val repository: RunnerDataRepository){

    private val logger = LoggerFactory.getLogger(RunnerDataConsumer::class.java)

    @Async
    fun insertValues(queue: BlockingQueue<RunnerData>){

        var record = queue.take()
        while(record.source != END_OF_SCRAPE){
            try {
                repository.save(record)
                logger.info("Inserted $record")
            } catch (e: Exception) {
                when (e) {
                    is ConstraintViolationException -> logger.debug("Validation Failure: $record")
                    is DataIntegrityViolationException -> logger.debug("Duplicate Entry: $record")
                    else -> logger.error("Exception: $record", e)
                }
            }
            record = queue.take()
        }
        logger.info("All items have been inserted")
    }
}