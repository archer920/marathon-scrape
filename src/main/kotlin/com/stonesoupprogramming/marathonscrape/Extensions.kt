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
import java.time.LocalDate
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom
import javax.validation.ConstraintViolationException

const val UNAVAILABLE = "Unavailable"

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

@Deprecated("Use createRunnerData")
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
    check(age.isNotBlank()) { "age cannot be blank. Mark as $UNAVAILABLE if not present" }
    check(finishTime.isNotBlank()) { "finishTime cannot be blank. Mark as $UNAVAILABLE if not present" }
    check(gender.isNotBlank()) { "gender cannot be blank. Mark as $UNAVAILABLE if not present" }
    check(year in 2014..2018) { "The year has to be between 2014-2018" }
    check(nationality.isNotBlank()) { "nationality cannot be blank. Mark as $UNAVAILABLE if not present" }
    check(place > 0) { "Place can't be negative. Use Int.MAX if it isn't present" }
    check(source != MarathonSources.Unassigned) { "source has to be assigned" }

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

@Synchronized
fun BlockingQueue<RunnerData>.addResultsPage(page: MutableList<RunnerData>) {
    addAll(page)
    page.clear()
}

fun String.toCss(): By {
    return By.cssSelector(this)
}

fun List<String>.stateToUSA(code: String): String {
    return if (this.contains(code.trim())) {
        "USA"
    } else {
        code.trim()
    }
}

fun List<String>.provinceToCanada(code : String): String {
    return if(this.contains(code.trim())) {
        "CAN"
    } else {
        code.trim()
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

fun Array<out String>.toMarathonSources() : List<MarathonSources?> {
    return this.map { arg -> MarathonSources.values().find { arg == it.arg } }.toList()
}

fun Logger.printBlankLines(lines : Int = 2){
    for(i in 0 until lines){
        info("")
    }
}

fun String.safeInt(logger: Logger) : Int {
    return try {
        this.toInt()
    } catch (e : Exception){
        logger.error("Unable to parse int from value $this", e)
        Int.MAX_VALUE
    }
}

fun String.calcAge(logger: Logger, twoDigit : Boolean = true) : String {
    return try {
        if(this.isBlank()){
            return UNAVAILABLE
        }
        val yob = if(twoDigit){
            LocalDate.now().year - (1900 + this.safeInt(logger))
        } else {
            LocalDate.now().year - this.safeInt(logger)
        }
        yob.toString()
    } catch (e : Exception){
        logger.error("Failed to calculate age for $this", e)
        throw e
    }
}

fun String.unavailableIfBlank() : String {
    return if(this.isBlank()){
        UNAVAILABLE
    } else {
        this
    }
}

fun CategoryResultsRepository.markPageComplete(runnerDataRepository: RunnerDataRepository, resultsPage: List<RunnerData>, categoryScrapeInfo: CategoryScrapeInfo, logger: Logger){
    try {
        runnerDataRepository.saveAll(resultsPage)
        this.save(categoryScrapeInfo.toCategoryResults())
    } catch (e : Exception){
        logger.error("Failed to make complete $categoryScrapeInfo", e)
        throw e
    }
}

fun PagedResultsRepository.markPageComplete(runnerDataRepository: RunnerDataRepository, resultsPage: List<RunnerData>, pagedResultsScrapeInfo: PagedResultsScrapeInfo, currentPage : Int, logger: Logger){
    try {
        runnerDataRepository.saveAll(resultsPage)
        this.save(pagedResultsScrapeInfo.toPagedResults(currentPage))
    } catch (e : Exception){
        logger.error("Failed to make complete $pagedResultsScrapeInfo", e)
        throw e
    }
}