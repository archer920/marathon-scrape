package com.stonesoupprogramming.marathonscrape.scrapers.sites

import com.stonesoupprogramming.marathonscrape.extension.unavailableIfBlank
import com.stonesoupprogramming.marathonscrape.models.AbstractScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.AgeGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.RunnerData
import com.stonesoupprogramming.marathonscrape.scrapers.AbstractBaseScraper
import com.stonesoupprogramming.marathonscrape.scrapers.DriverFactory
import com.stonesoupprogramming.marathonscrape.scrapers.JsDriver
import com.stonesoupprogramming.marathonscrape.service.MarkCompleteService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MikatimingDeScraper(@Autowired driverFactory: DriverFactory,
                          @Autowired jsDriver: JsDriver,
                          @Autowired markedCompleteService: MarkCompleteService<AgeGenderColumnPositions, ResultsPage>,
                          @Autowired usStateCodes: List<String>,
                          @Autowired canadaProvinceCodes: List<String>)
    : AbstractBaseScraper<AgeGenderColumnPositions, ResultsPage, AbstractScrapeInfo<AgeGenderColumnPositions, ResultsPage>>(
        driverFactory,
        jsDriver, markedCompleteService,
        ResultsPage::class.java,
        LoggerFactory.getLogger(MikatimingDeScraper::class.java),
        usStateCodes,
        canadaProvinceCodes) {

    override fun processRow(row: List<String>, columnPositions: AgeGenderColumnPositions, scrapeInfo: AbstractScrapeInfo<AgeGenderColumnPositions, ResultsPage>, rowHtml: List<String>): RunnerData? {
        val place = row[columnPositions.place].unavailableIfBlank()
        val nationality = processNationality(row[columnPositions.nationality])
        val age = row[columnPositions.age]
        val finishTime = row[columnPositions.finishTime]
        val gender = scrapeInfo.gender?.code ?: throw IllegalArgumentException("Gender is required")

        return try {
            RunnerData.createRunnerData(logger, age, finishTime, gender, scrapeInfo.marathonYear, nationality, place, scrapeInfo.marathonSources)
        } catch (e: Exception) {
            logger.error("Unable to create runner data", e)
            throw e
        }
    }

    private fun processNationality(nationalityStr: String): String {
        return try {
            val nationality = nationalityStr.split(" ").last()
            nationality.replace("(", "".replace(")", ""))
        } catch (e: Exception) {
            logger.error("Unable to parse nationality", e)
            throw e
        }
    }
}