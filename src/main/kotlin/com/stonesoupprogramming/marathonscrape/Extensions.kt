package com.stonesoupprogramming.marathonscrape

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.Logger
import java.io.FileWriter
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom
import javax.validation.ConstraintViolationException

fun successResult(): CompletableFuture<String> {
    return CompletableFuture.completedFuture("Success")
}

fun failResult(): CompletableFuture<String> {
    return CompletableFuture.completedFuture("Error")
}

fun sleepRandom(min : Int = 5, max : Int = 60) {
    val amount = 1000 * ThreadLocalRandom.current().nextInt(min, max)
    try {
        Thread.sleep(amount.toLong())
    } catch (e: Exception) {
    }
}

@Deprecated("Each page of results should be processed atomically. Use List.insertRunnerData and then add the finished list to the Queue")
fun BlockingQueue<RunnerData>.insertRunnerData(logger: Logger, age: String, finishTime: String, gender: String, year: Int, nationality: String, place: Int, source: MarathonSources, company: String = "", halfwayTime: String = "") {
    val runnerData = RunnerData(
            source = source,
            age = age,
            gender = gender,
            nationality = nationality,
            finishTime = finishTime,
            halfwayTime = halfwayTime,
            company = company,
            marathonYear = year,
            place = place
    )
    put(runnerData)
    logger.info("Produced: $runnerData")
}

fun MutableList<RunnerData>.insertRunnerData(logger: Logger, age: String, finishTime: String, gender: String, year: Int, nationality: String, place: Int, source: MarathonSources, company: String = "", halfwayTime: String = "") {
    val runnerData = RunnerData(
            source = source,
            age = age,
            gender = gender,
            nationality = nationality,
            finishTime = finishTime,
            halfwayTime = halfwayTime,
            company = company,
            marathonYear = year,
            place = place
    )
    add(runnerData)
    logger.info("Produced: $runnerData")
}

fun createRunnerData(logger: Logger, age: String, finishTime: String, gender: String, year: Int, nationality: String, place: Int, source: MarathonSources, company: String = "", halfwayTime: String = ""): RunnerData {
    val runnerData = RunnerData(
            source = source,
            age = age,
            gender = gender,
            nationality = nationality,
            finishTime = finishTime,
            halfwayTime = halfwayTime,
            company = company,
            marathonYear = year,
            place = place
    )
    logger.info("Produced: $runnerData")
    return runnerData
}

fun List<RunnerData>.writeToCsv(fileName: String) {
    val printer = CSVPrinter(FileWriter(fileName), CSVFormat.DEFAULT.withHeader("Age",
            "Gender", "Nationality", "Finish Time", "Halfway Time", "Company, Team, or Sponsor", "Year"))
    this.forEach { printer.printRecord(it.age, it.gender, it.nationality, it.finishTime, it.halfwayTime, it.company, it.marathonYear) }
    printer.close()
}

fun List<RunnerData>.saveToCSV(fileName: String) {
    val printer = CSVPrinter(FileWriter(fileName), CSVFormat.DEFAULT.withHeader("source, age, gender, nationality, finishTime, halfwayTime, company, marathonYear, place"))
    this.forEach { printer.printRecord(it.source, it.age, it.gender, it.nationality, it.finishTime, it.halfwayTime, it.company, it.marathonYear, it.place) }
    printer.close()
}

fun RemoteWebDriver.countTableRows(tableBody: By, logger: Logger, attemptNum : Int = 0, maxAttempts : Int = 60): Int {
    return try {
        findElement(tableBody).findElements(By.tagName("tr")).size
    } catch (e: Exception) {
        return when(e){
            is NoSuchElementException -> {
                if(attemptNum < maxAttempts){
                    Thread.sleep(1000)
                    countTableRows(tableBody, logger, attemptNum + 1, maxAttempts)
                } else{
                    logger.error("Giving up after reaching maximum attempts", e)
                    throw e
                }
            }
            else -> {
                logger.error("Failed to count table rows", e)
                throw e
            }
        }
    }
}

fun RemoteWebDriver.findCellValue(tableBody: By, row: Int, cell: Int, logger: Logger, attemptNum: Int = 0, giveUp: Int = 100): String {
    return try {
        findElement(tableBody).findElements(By.tagName("tr"))[row].findElements(By.tagName("td"))[cell].text
    } catch (e: Exception) {
        when (e) {
            is StaleElementReferenceException -> {
                if (attemptNum < giveUp) {
                    Thread.sleep(1000)
                    return this.findCellValue(tableBody, row, cell, logger, attemptNum + 1)
                } else {
                    logger.error("Unable to find a non-stale reference", e)
                    throw e
                }
            }
            else -> {
                logger.error("Failed to determine value for cell [$row][$cell]", e)
                throw e
            }
        }
    }
}

@Synchronized
fun BlockingQueue<RunnerData>.addResultsPage(page: MutableList<RunnerData>) {
    addAll(page)
    page.clear()
}

fun String.toCss(): By {
    return By.cssSelector(this)
}

fun List<String>.toCountry(code: String): String {
    return if (this.contains(code.trim())) {
        "USA"
    } else {
        code.trim()
    }
}

@Deprecated("Use the new function that inserts the runner data")
fun UrlPage.markComplete(urlPageRepository: UrlPageRepository, queue: BlockingQueue<RunnerData>, resultsPage: MutableList<RunnerData>, logger: Logger) {
    try {
        urlPageRepository.save(this)
        queue.addResultsPage(resultsPage)
        logger.info("Successfully scraped: $this")
    } catch (e: Exception) {
        logger.error("Failed to mark complete: $this")
    }
}

fun UrlPage.markComplete(urlPageRepository: UrlPageRepository, runnerDataRepository: RunnerDataRepository, resultsPage: List<RunnerData>, logger: Logger){
    try {
        val runnerDataViolations = mutableListOf<RunnerData>()
        for(rd in resultsPage){
            try {
                runnerDataRepository.save(rd)
            } catch (e : ConstraintViolationException){
                runnerDataViolations.add(rd)
            }
        }
        if(runnerDataViolations.isNotEmpty()){
            logger.info("Saving violations for review")
            runnerDataViolations.saveToCSV("Violations-${System.currentTimeMillis()}.csv")
        }
        urlPageRepository.save(this)
        logger.info("Successfully scraped: $this")
    } catch (e : Exception){
        when(e) {
            is ConstraintViolationException -> {

            }
        }
        logger.error("Failed to mark complete: $this)")
    }
}

@Deprecated("Use the markComplete with the runner data repository instead")
fun PagedResults.markComplete(pagedResultsRepository: PagedResultsRepository, queue : BlockingQueue<RunnerData>, resultsPage: MutableList<RunnerData>, logger: Logger){
    try {
        pagedResultsRepository.save(this)
        queue.addResultsPage(resultsPage)
        logger.info(("Successfully scraped: $this"))
    } catch (e : Exception){
        logger.error("Failed to mark complete: $this)")
    }
}

fun PagedResults.markComplete(pagedResultsRepository: PagedResultsRepository, runnerDataRepository: RunnerDataRepository, resultsPage: List<RunnerData>, logger: Logger){
    try {
        val runnerDataViolations = mutableListOf<RunnerData>()
        for(rd in resultsPage){
            try {
                runnerDataRepository.save(rd)
            } catch (e : ConstraintViolationException){
                runnerDataViolations.add(rd)
            }
        }
        if(runnerDataViolations.isNotEmpty()){
            logger.info("Saving violations for review")
            runnerDataViolations.saveToCSV("Violations-${System.currentTimeMillis()}.csv")
        }
        pagedResultsRepository.save(this)
        logger.info("Successfully scraped: $this")
    } catch (e : Exception){
        when(e) {
            is ConstraintViolationException -> {

            }
        }
        logger.error("Failed to mark complete: $this)")
    }
}

fun GenderPagedResults.markComplete(genderPagedResultsRepository: GenderPagedResultsRepository, queue: BlockingQueue<RunnerData>, resultsPage: MutableList<RunnerData>, logger : Logger){
    try {
        genderPagedResultsRepository.save(this)
        queue.addResultsPage(resultsPage)
        logger.info(("Successfully scraped: $this"))
    } catch (e : Exception){
        logger.error("Failed to mark complete: $this)")
    }
}

fun RemoteWebDriver.selectComboBoxOption(selector: By, value: String) {
    Select(this.findElement(selector)).selectByVisibleText(value)
}

fun RemoteWebDriver.waitUntilClickable(selector: By, timeout: Long = 60, attemptNum: Int = 0, giveUp: Int = 60) {
    try {
        WebDriverWait(this, timeout).until(ExpectedConditions.elementToBeClickable(selector))
    } catch (e : Exception){
        if(attemptNum < giveUp){
            this.waitUntilClickable(selector, timeout, attemptNum + 1, giveUp)
        } else {
            throw e
        }
    }
}

fun RemoteWebDriver.waitUntilVisible(selector: By, timeout: Long = 10, attemptNum: Int = 0, giveUp: Int = 60) {
    try {
        WebDriverWait(this, timeout).until(ExpectedConditions.visibilityOfElementLocated(selector))
    } catch (e : Exception){
        if(attemptNum < giveUp){
            Thread.sleep(1000)
            waitUntilVisible(selector, attemptNum = attemptNum + 1)
        } else {
            throw e
        }
    }
}

fun RemoteWebDriver.scrollIntoView(selector: By) {
    val elem = this.findElement(selector)
    this.executeScript("arguments[0].scrollIntoView(true);", elem)
    this.waitUntilVisible(selector)
}

fun WebElement.scrollIntoView(driver: RemoteWebDriver) {
    driver.executeScript("arguments[0].scrollIntoView(true);", this)
    this.waitUntilVisible(driver)
}

fun WebElement.waitUntilVisible(driver: RemoteWebDriver, timeOut: Long = 60) {
    WebDriverWait(driver, timeOut).until(ExpectedConditions.visibilityOf(this))
}

fun RemoteWebDriver.click(element: By, logger: Logger) {
    try {
        this.waitUntilClickable(element)
        this.findElement(element).click()
    } catch (e: Exception) {
        logger.error("Failed to click element", e)
        throw e
    }
}

fun Array<out String>.toMarathonSources() : MarathonSources{
    return when {
        this.contains(Application.Args.BUDAPEST) -> MarathonSources.Budapest
        this.contains(Application.Args.OTTAWA) -> MarathonSources.Ottawa
        this.contains(Application.Args.SAN_FRANSCISCO) -> MarathonSources.SanFranscisco
        this.contains(Application.Args.MEDTRONIC) -> MarathonSources.TwinCities
        this.contains(Application.Args.MARINES) -> MarathonSources.Marines
        this.contains(Application.Args.VIENNA) -> MarathonSources.Vienna
        this.contains(Application.Args.BERLIN) -> MarathonSources.Berlin
        this.contains(Application.Args.BOSTON) -> MarathonSources.Boston
        this.contains(Application.Args.CHICAGO) -> MarathonSources.Chicago
        this.contains(Application.Args.NEW_YORK) -> MarathonSources.Nyc
        this.contains(Application.Args.LOS_ANGELES) -> MarathonSources.LosAngeles
        this.contains(Application.Args.DISNEY) -> MarathonSources.Disney
        this.contains(Application.Args.MELBOURE) -> MarathonSources.Melbourne
        this.contains(Application.Args.Taipei) -> MarathonSources.Taipei
        this.contains(Application.Args.Yuengling) -> MarathonSources.Yuengling
        else -> throw IllegalArgumentException("Not a valid source")
    }
}

fun Logger.printBlankLines(lines : Int = 2){
    for(i in 0 until lines){
        info("")
    }
}
