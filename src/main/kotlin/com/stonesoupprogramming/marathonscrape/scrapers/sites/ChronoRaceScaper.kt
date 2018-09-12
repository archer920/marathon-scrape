package com.stonesoupprogramming.marathonscrape.scrapers.sites

import com.stonesoupprogramming.marathonscrape.enums.Gender
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
class ChronoRaceScraper(@Autowired driverFactory: DriverFactory,
                        @Autowired jsDriver: JsDriver,
                        @Autowired markedCompleteService: MarkCompleteService<AgeGenderColumnPositions, ResultsPage>,
                        @Autowired usStateCodes: List<String>,
                        @Autowired canadaProvinceCodes: List<String>)
    : AbstractBaseScraper<AgeGenderColumnPositions, ResultsPage, AbstractScrapeInfo<AgeGenderColumnPositions, ResultsPage>>(driverFactory,
        jsDriver,
        markedCompleteService,
        ResultsPage::class.java,
        LoggerFactory.getLogger(ChronoRaceScraper::class.java),
        usStateCodes,
        canadaProvinceCodes) {

    override fun processRow(row: List<String>, columnPositions: AgeGenderColumnPositions, scrapeInfo: AbstractScrapeInfo<AgeGenderColumnPositions, ResultsPage>, rowHtml: List<String>): RunnerData? {
        val place = row[columnPositions.place].unavailableIfBlank()
        val age = row[columnPositions.age]
        val gender = if (row[columnPositions.gender].isBlank()) {
            Gender.MALE.code
        } else {
            Gender.FEMALE.code
        }
        val nationality = row[columnPositions.nationality]
        val finishTime = row[columnPositions.finishTime]

        return try {
            RunnerData.createRunnerData(logger, age, finishTime, gender, scrapeInfo.marathonYear, nationality, place, scrapeInfo.marathonSources)
        } catch (e: Exception) {
            logger.error("Unable to create runner data", e)
            throw e
        }
    }
}