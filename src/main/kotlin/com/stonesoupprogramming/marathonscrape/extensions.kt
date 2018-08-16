package com.stonesoupprogramming.marathonscrape

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.slf4j.Logger
import java.io.FileWriter
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom

fun successResult(): CompletableFuture<String> {
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