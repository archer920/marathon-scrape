package com.stonesoupprogramming.marathonscrape.scrapers.sites

import com.stonesoupprogramming.marathonscrape.extension.UNAVAILABLE
import com.stonesoupprogramming.marathonscrape.extension.unavailableIfBlank
import com.stonesoupprogramming.marathonscrape.models.*
import com.stonesoupprogramming.marathonscrape.scrapers.AbstractBaseScraper
import com.stonesoupprogramming.marathonscrape.scrapers.DriverFactory
import com.stonesoupprogramming.marathonscrape.scrapers.JsDriver
import com.stonesoupprogramming.marathonscrape.service.MarkCompleteService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MultisportAustraliaScraper(@Autowired driverFactory: DriverFactory,
                                 @Autowired jsDriver: JsDriver,
                                 @Autowired markedCompleteService: MarkCompleteService<AgeGenderColumnPositions, ResultsPage>,
                                 @Autowired usStateCodes: List<String>,
                                 @Autowired canadaProvinceCodes: List<String>)
    : AbstractBaseScraper<AgeGenderColumnPositions, ResultsPage, AbstractScrapeInfo<AgeGenderColumnPositions, ResultsPage>>(driverFactory,
        jsDriver,
        markedCompleteService,
        ResultsPage::class.java,
        LoggerFactory.getLogger(MultisportAustraliaScraper::class.java),
        usStateCodes,
        canadaProvinceCodes) {

    override fun processRow(row: List<String>, columnPositions: AgeGenderColumnPositions, scrapeInfo: AbstractScrapeInfo<AgeGenderColumnPositions, ResultsPage>, rowHtml: List<String>): RunnerData? {
        val pos = row[columnPositions.place].unavailableIfBlank()
        val finishTime = row[columnPositions.finishTime].unavailableIfBlank()
        val age = row[columnPositions.age]
        val gender = row[columnPositions.gender]

        return try {
            RunnerData.createRunnerData(logger, age, finishTime, gender, scrapeInfo.marathonYear, UNAVAILABLE, pos, scrapeInfo.marathonSources)
        } catch (e: Exception) {
            logger.error("Unable to create runner data", e)
            throw e
        }
    }
}