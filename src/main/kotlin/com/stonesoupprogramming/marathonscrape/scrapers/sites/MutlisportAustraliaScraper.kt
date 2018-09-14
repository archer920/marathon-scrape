package com.stonesoupprogramming.marathonscrape.scrapers.sites

import com.stonesoupprogramming.marathonscrape.extension.UNAVAILABLE
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
        return try {
            val pos = columnPositions.placeFunction?.apply(row[columnPositions.place], rowHtml[columnPositions.place])
                    ?: row[columnPositions.place].unavailableIfBlank()
            val finishTime = columnPositions.finishTimeFunction?.apply(row[columnPositions.finishTime], rowHtml[columnPositions.finishTime])
                    ?: row[columnPositions.finishTime].unavailableIfBlank()
            val age = columnPositions.ageFunction?.apply(row[columnPositions.age], rowHtml[columnPositions.age])
                    ?: row[columnPositions.age].unavailableIfBlank()
            val gender = columnPositions.genderFunction?.apply(row[columnPositions.gender], rowHtml[columnPositions.gender])
                    ?: row[columnPositions.gender].split("\n")[0].trim().unavailableIfBlank()

            fun nationalityFunc(): String {
                var nationality = row[columnPositions.nationality].split("\n")[0].trim()
                if (nationality == "|") {
                    nationality = UNAVAILABLE
                }
                return nationality
            }

            val nationality = columnPositions.nationalityFunction?.apply(row[columnPositions.nationality], rowHtml[columnPositions.nationality])
                    ?: nationalityFunc()

            try {
                RunnerData.createRunnerData(logger, age, finishTime, gender, scrapeInfo.marathonYear, nationality, pos, scrapeInfo.marathonSources)
            } catch (e: Exception) {
                logger.error("Unable to create runner data", e)
                throw e
            }
        } catch (e: Exception) {
            logger.error("Unable to process row", e)
            throw e
        }
    }
}