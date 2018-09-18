package com.stonesoupprogramming.marathonscrape.scrapers.sites

import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.NumberedResultsPage
import com.stonesoupprogramming.marathonscrape.models.PagedScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.RunnerData
import com.stonesoupprogramming.marathonscrape.scrapers.AbstractPagedResultsScraper
import com.stonesoupprogramming.marathonscrape.scrapers.DriverFactory
import com.stonesoupprogramming.marathonscrape.scrapers.JsDriver
import com.stonesoupprogramming.marathonscrape.service.MarkCompleteService
import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CracoviaScraper(@Autowired driverFactory: DriverFactory,
                      @Autowired jsDriver: JsDriver,
                      @Autowired markedCompleteService: MarkCompleteService<MergedAgedGenderColumnPositions, NumberedResultsPage>,
                      @Autowired usStateCodes: List<String>,
                      @Autowired canadaProvinceCodes: List<String>) : AbstractPagedResultsScraper<MergedAgedGenderColumnPositions>(driverFactory, jsDriver, markedCompleteService, NumberedResultsPage::class.java, LoggerFactory.getLogger(CracoviaScraper::class.java), usStateCodes, canadaProvinceCodes) {

    override fun processRow(row: List<String>, columnPositions: MergedAgedGenderColumnPositions, scrapeInfo: PagedScrapeInfo<MergedAgedGenderColumnPositions>, rowHtml: List<String>): RunnerData? {
        return try {
            val place = row[columnPositions.place]
            val finishTime = row[columnPositions.finishTime]
            val nationality = parseNationality(rowHtml[columnPositions.nationality])
            val ageGender = row[columnPositions.ageGender]
            val age = parseAge(ageGender)
            val gender = parseGender(ageGender)

            try {
                RunnerData.createRunnerData(logger, age, finishTime, gender, scrapeInfo.marathonYear, nationality, place, scrapeInfo.marathonSources)
            } catch (e: Exception) {
                logger.error("Unable to create runner data", e)
                throw e
            }

        } catch (e: Exception) {
            logger.error("Failed to process row", e)
            throw e
        }
    }

    private fun parseGender(ageGender: String): String {
        return try {
            ageGender[0].toString()
        } catch (e: Exception) {
            logger.error("Unable to parse gender", e)
            throw e
        }
    }

    private fun parseAge(ageGender: String): String {
        return try {
            ageGender.substring(1)
        } catch (e: Exception) {
            logger.error("Unable to parse age", e)
            throw e
        }
    }

    private fun parseNationality(html: String): String {
        return try {
            val parts = html.split(" ")
            parts[2].replace("alt=", "").replace("\"", "")
        } catch (e: Exception) {
            logger.error("Unable to parse the nationality", e)
            throw e
        }
    }

    override fun findCurrentPageNum(driver: RemoteWebDriver): Int {
        return try {
            jsDriver.readText(driver, "#table2 > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(28) > tbody:nth-child(1) > tr:nth-child(12) > td:nth-child(2) > center:nth-child(1) > big:nth-child(1) > big:nth-child(1)").split(" ").first().toInt() / 10
        } catch (e: Exception) {
            logger.error("Unable to read page number", e)
            throw e
        }
    }
}