package com.stonesoupprogramming.marathonscrape.scrapers.sites

import com.stonesoupprogramming.marathonscrape.extension.calcAge
import com.stonesoupprogramming.marathonscrape.models.AbstractScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.AgeGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.scrapers.DriverFactory
import com.stonesoupprogramming.marathonscrape.scrapers.JsDriver
import com.stonesoupprogramming.marathonscrape.scrapers.StandardWebScraperAgeGender
import com.stonesoupprogramming.marathonscrape.service.MarkCompleteService
import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.function.Function

@Component
class AlexanderTheGreatScraper(
        @Autowired driverFactory: DriverFactory,
        @Autowired jsDriver: JsDriver,
        @Autowired markedCompleteService: MarkCompleteService<AgeGenderColumnPositions, ResultsPage>,
        @Autowired usStateCodes: List<String>,
        @Autowired canadaProvinceCodes: List<String>)
    : StandardWebScraperAgeGender(driverFactory, jsDriver, markedCompleteService, usStateCodes, canadaProvinceCodes, LoggerFactory.getLogger(AlexanderTheGreatScraper::class.java)) {

    override fun processPage(driver: RemoteWebDriver, scrapeInfo: AbstractScrapeInfo<AgeGenderColumnPositions, ResultsPage>) {
        val body = jsDriver.readText(driver, scrapeInfo.tableBodySelector)
        val lines = when (scrapeInfo.marathonYear) {
            2014 -> readBody(body, "\n ", 1, 1, Function { it -> it.length == 2 }, 1, 6, 7, 8, 11, scrapeInfo)
            else -> readBody(body, "\n\n", 2, 2, Function { it -> false }, 1, -1, 5, 8, 7, scrapeInfo)
        }
        val rs = lines.map { line -> processRow(line, AgeGenderColumnPositions(place = 0, gender = 1, age = 2, nationality = 3, finishTime = 4), scrapeInfo, emptyList()) }.filterNotNull()
        markCompleteService.markComplete(ResultsPage::class.java, scrapeInfo, rs)
    }

    private fun readBody(body: String, splitCharacters: String, start: Int, end: Int, filterFunction: Function<String, Boolean>, placePos: Int, genderPos: Int, agePos: Int, nationalityPos: Int, finishTimePos: Int, scrapeInfo: AbstractScrapeInfo<AgeGenderColumnPositions, ResultsPage>): List<List<String>> {
        return try {
            var lines = body.split(splitCharacters)
            lines = lines.subList(start, lines.size - end).filterNot { it -> filterFunction.apply(it) }
            lines.map { it ->
                val parts = it.split("\n")
                val place = parts[placePos]
                val gender = if (genderPos == -1) {
                    scrapeInfo.gender?.code ?: throw IllegalArgumentException("Gender is required for this year")
                } else {
                    parts[genderPos].split("-").first()
                }
                val age = if (parts[agePos].contains("-")) {
                    parts[agePos]
                } else {
                    parts[agePos].calcAge(logger, false)
                }
                val nationality = if (parts[nationalityPos].contains("-")) {
                    parts[nationalityPos].split("-").first()
                } else {
                    parts[nationalityPos]
                }
                val finishTime = parts[finishTimePos]
                listOf(place, gender, age, nationality, finishTime)
            }
        } catch (e: Exception) {
            logger.error("Unable to read body", e)
            throw e
        }
    }
}