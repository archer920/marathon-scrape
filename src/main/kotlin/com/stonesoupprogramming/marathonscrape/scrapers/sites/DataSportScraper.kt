package com.stonesoupprogramming.marathonscrape.scrapers.sites

import com.stonesoupprogramming.marathonscrape.extension.UNAVAILABLE
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

@Component
class DataSportScraper(@Autowired driverFactory: DriverFactory,
                       @Autowired jsDriver: JsDriver,
                       @Autowired markedCompleteService: MarkCompleteService<AgeGenderColumnPositions, ResultsPage>,
                       @Autowired usStateCodes: List<String>,
                       @Autowired canadaProvinceCodes: List<String>) : StandardWebScraperAgeGender(driverFactory, jsDriver, markedCompleteService, usStateCodes, canadaProvinceCodes, LoggerFactory.getLogger(DataSportScraper::class.java)) {

    override fun processPage(driver: RemoteWebDriver, scrapeInfo: AbstractScrapeInfo<AgeGenderColumnPositions, ResultsPage>) {
        val body = jsDriver.readText(driver, ".m-rich-text > pre:nth-child(8)")
        val lines = readBody(body)

        val resultSet = lines.mapIndexed { _, row -> processRow(row, AgeGenderColumnPositions(place = 0, age = 1, nationality = 2, finishTime = 3, gender = 4), scrapeInfo, emptyList()) }.filterNotNull()
        markCompleteService.markComplete(ResultsPage::class.java, scrapeInfo, resultSet)
    }

    private fun readBody(body: String): List<List<String>> {
        var parts = body.split("\n")
        parts = parts.subList(5, parts.size - 2)
                .filterNot { line -> line.startsWith("          ") }
        return parts.map { line ->
            try {
                val sections = line.split(" ").filter { it -> it.isNotBlank() }

                val place = parsePlace(sections)
                val age = parseAge(sections)
                val nationality = parseNationality(sections)
                val finishTime = parseFinishTime(sections)
                val gender = parseGender(sections)

                listOf(place, age, nationality, finishTime, gender)
            } catch (e: Exception) {
                logger.error("Failed to read line", e)
                throw e
            }

        }
    }

    private fun parseGender(sections: List<String>): String {
        return try {
            sections.find { it -> (it.length == 3 || it.length == 4) && (it.startsWith("M") || it.startsWith("F")) }?.get(0)?.toString()
                    ?: throw IllegalStateException("Unable to find gender: $sections")
        } catch (e: Exception) {
            logger.error("Unable to read gender", e)
            throw e
        }
    }

    private fun parseFinishTime(sections: List<String>): String {
        return try {
            if (sections.first() == "DNF") {
                return UNAVAILABLE
            } else {
                sections.find { it -> it.contains(".") && it.contains(",") }
                        ?: throw IllegalStateException("Unable to find finish time: $sections")
            }
        } catch (e: Exception) {
            logger.error("Unable to find finish time", e)
            throw e
        }
    }

    private fun parseNationality(sections: List<String>): String {
        return try {
            var index = sections.indexOfFirst { it -> it.length == 4 && it.all { c -> c.isDigit() } }
            if (index == -1) {
                index = sections.indexOfFirst { it -> it == "xxxx" }
            }
            if (sections[index + 1].length == 3) {
                sections[index + 1]
            } else {
                throw IllegalStateException("Failed to parse nationality")
            }
        } catch (e: Exception) {
            logger.error("Unable to read nationality", e)
            throw e
        }
    }

    private fun parseAge(sections: List<String>): String {
        return try {
            if (sections.any { it -> it == "xxxx" }) {
                UNAVAILABLE
            } else {
                sections.find { it -> it.length == 4 && it.all { c -> c.isDigit() } }?.calcAge(logger, false)
                        ?: throw IllegalStateException("Unable to find age: $sections")
            }
        } catch (e: Exception) {
            logger.error("Unable to read age", e)
            throw e
        }
    }

    private fun parsePlace(sections: List<String>): String {
        return try {
            sections.first().replace(".", "")
        } catch (e: Exception) {
            logger.error("Unable to read place", e)
            throw e
        }
    }
}