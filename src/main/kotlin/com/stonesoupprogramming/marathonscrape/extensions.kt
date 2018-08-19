package com.stonesoupprogramming.marathonscrape

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.FileWriter
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom

fun successResult(): CompletableFuture<String>  {
    return CompletableFuture.completedFuture("Success")
}

fun failResult(): CompletableFuture<String> {
    return CompletableFuture.completedFuture("Error")
}

fun sleepRandom(){
    val amount = 1000 * ThreadLocalRandom.current().nextInt(5, 60)
    try {
        Thread.sleep(amount.toLong())
    } catch (e : Exception) {}
}

fun BlockingQueue<RunnerData>.insertRunnerData(logger: Logger, age : String, finishTime : String, gender : String, year : Int, nationality : String, place : Int, source : String, company : String = "", halfwayTime : String = ""){
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
    runnerData.updateRaceYearPlace()
    put(runnerData)
    logger.info("Produced: $runnerData")
}

fun List<RunnerData>.writeToCsv(fileName : String){
    val printer = CSVPrinter(FileWriter(fileName), CSVFormat.DEFAULT.withHeader("Age",
            "Gender", "Nationality", "Finish Time", "Halfway Time", "Company, Team, or Sponsor", "Year"))
    this.forEach { printer.printRecord(it.age, it.gender, it.nationality, it.finishTime, it.halfwayTime, it.company, it.marathonYear) }
    printer.close()
}

fun List<RunnerData>.saveDuplicates(fileName : String){
    val printer = CSVPrinter(FileWriter(fileName), CSVFormat.DEFAULT.withHeader("source, age, gender, nationality, finishTime, halfwayTime, company, marathonYear, place"))
    this.forEach { printer.printRecord(it.source, it.age, it.gender, it.nationality, it.finishTime, it.halfwayTime, it.company, it.marathonYear, it.place) }
    printer.close()
}

fun RemoteWebDriver.countTableRows(tableBody : By, logger: Logger) : Int {
    return try {
        findElement(tableBody).findElements(By.tagName("tr")).size
    } catch (e : Exception){
        logger.error("Failed to count table rows", e)
        throw e
    }
}

fun RemoteWebDriver.findCellValue(tableBody: By, row : Int, cell : Int, logger: Logger) : String {
    return try {
        findElement(tableBody).findElements(By.tagName("tr"))[row].findElements(By.tagName("td"))[cell].text
    } catch (e : Exception){
        logger.error("Failed to determine value for cell [$row][$cell]", e)
        throw e
    }
}

fun String.toCss() : By {
    return By.cssSelector(this)
}