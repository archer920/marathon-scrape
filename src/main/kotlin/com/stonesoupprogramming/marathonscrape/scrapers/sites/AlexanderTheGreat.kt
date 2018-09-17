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
            else -> readBody(body, "\n\n", 2, 2, Function { it -> false }, 0, -1, 5, 8, 7, scrapeInfo)
        }
        val rs = lines.asSequence().map { line -> processRow(line, AgeGenderColumnPositions(place = 0, gender = 1, age = 2, nationality = 3, finishTime = 4), scrapeInfo, emptyList()) }.filterNotNull().toList()
        markCompleteService.markComplete(ResultsPage::class.java, scrapeInfo, rs)
    }

    private fun readBody(body: String, splitCharacters: String, start: Int, end: Int, filterFunction: Function<String, Boolean>, placePos: Int, genderPos: Int, agePos: Int, nationalityPos: Int, finishTimePos: Int, scrapeInfo: AbstractScrapeInfo<AgeGenderColumnPositions, ResultsPage>): List<List<String>> {
        return try {
            var lines = body.split(splitCharacters)
            lines = try {
                lines.subList(start, lines.size - end).filterNot { it -> filterFunction.apply(it) }
            } catch (e: Exception) {
                logger.error("Unable to remote header and footer rows", e)
                throw e
            }
            lines.asSequence().map { it -> processLine(it, placePos, genderPos, agePos, nationalityPos, finishTimePos, scrapeInfo) }.filterNotNull().toList()
        } catch (e: Exception) {
            logger.error("Unable to read body", e)
            throw e
        }
    }

    private fun processLine(line: String, placePos: Int, genderPos: Int, agePos: Int, nationalityPos: Int, finishTimePos: Int, scrapeInfo: AbstractScrapeInfo<AgeGenderColumnPositions, ResultsPage>): List<String>? {
        val parts = line.split("\n")

        var list: List<String>? = handleSpecialCases(parts)
        if (list == null) {
            return null
        }

        if (list.isEmpty()) {
            val place = try {
                parts[placePos]
            } catch (e: Exception) {
                logger.error("Unable to read place", e)
                throw e
            }
            val gender = if (genderPos == -1) {
                scrapeInfo.gender?.code ?: throw IllegalArgumentException("Gender is required for this year")
            } else {
                try {
                    parts[genderPos].split("-").first()
                } catch (e: Exception) {
                    logger.error("Unable to read gender", e)
                    throw e
                }

            }
            val age = try {
                if (parts[agePos].contains("-")) {
                    parts[agePos]
                } else {
                    parts[agePos].calcAge(logger, false)
                }
            } catch (e: Exception) {
                logger.error("Unable to read age", e)
                throw e
            }
            val nationality = try {
                if (parts[nationalityPos].contains("-")) {
                    parts[nationalityPos].split("-").first()
                } else {
                    parts[nationalityPos]
                }
            } catch (e: Exception) {
                logger.error("Unable to read nationality", e)
                throw e
            }
            val finishTime = try {
                parts[finishTimePos]
            } catch (e: Exception) {
                logger.error("Unable to read finish time", e)
                throw e
            }
            list = listOf(place, gender, age, nationality, finishTime)
        }
        return list
    }

    private fun handleSpecialCases(parts: List<String>): List<String>? {
        if (parts.size <= 2) {
            return null
        }
        return try {
            when (parts[0]) {
                "152" -> listOf("152", "M", "40-44", "CYP", "3:15:57")
                "166" -> listOf("166", "W", "19-34", "TUR", "3:18:41")
                "237" -> listOf("237", "M", "50-54", "GRE", "3:26:12")
                "365" -> listOf("365", "M", "40-44", "GRE", "3:36:17")
                "375" -> listOf("375", "M", "35-39", "GRE", "3:36:56")
                "474" -> listOf("474", "M", "50-54", "GRE", "3:45:03")
                "1497" -> listOf("1497", "M", "35-39", "GRE", "5:40:02")
                "1500" -> listOf("1500", "M", "40-44", "GRE", "5:40:53")
                else -> emptyList()
            }
        } catch (e: Exception) {
            logger.error("Unable to process special case: $parts", e)
            throw e
        }
    }
}