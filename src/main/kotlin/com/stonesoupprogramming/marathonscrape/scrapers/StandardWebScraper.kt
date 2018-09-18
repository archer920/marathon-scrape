package com.stonesoupprogramming.marathonscrape.scrapers

import com.stonesoupprogramming.marathonscrape.extension.unavailableIfBlank
import com.stonesoupprogramming.marathonscrape.models.*
import com.stonesoupprogramming.marathonscrape.service.MarkCompleteService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class StandardWebScraperAgeGender(@Autowired driverFactory: DriverFactory,
                                  @Autowired jsDriver: JsDriver,
                                  @Autowired markedCompleteService: MarkCompleteService<AgeGenderColumnPositions, ResultsPage>,
                                  @Autowired usStateCodes: List<String>,
                                  @Autowired canadaProvinceCodes: List<String>,
                                  logger: Logger = LoggerFactory.getLogger(StandardWebScraperAgeGender::class.java))
    : AbstractBaseScraper<AgeGenderColumnPositions, ResultsPage, AbstractScrapeInfo<AgeGenderColumnPositions, ResultsPage>>(driverFactory,
        jsDriver, markedCompleteService, ResultsPage::class.java, logger, usStateCodes, canadaProvinceCodes) {


    override fun processRow(row: List<String>, columnPositions: AgeGenderColumnPositions, scrapeInfo: AbstractScrapeInfo<AgeGenderColumnPositions, ResultsPage>, rowHtml: List<String>): RunnerData? {
        return try {
            val place = columnPositions.placeFunction?.apply(row[columnPositions.place], rowHtml[columnPositions.place])
                    ?: row[columnPositions.place].unavailableIfBlank()
            val nationality = columnPositions.nationalityFunction?.apply(row[columnPositions.nationality], rowHtml[columnPositions.nationality])
                    ?: row[columnPositions.nationality].unavailableIfBlank()
            val finishTime = columnPositions.finishTimeFunction?.apply(row[columnPositions.finishTime], rowHtml[columnPositions.finishTime])
                    ?: row[columnPositions.finishTime].unavailableIfBlank()
            val age = columnPositions.ageFunction?.apply(row[columnPositions.age], rowHtml[columnPositions.age])
                    ?: row[columnPositions.age].unavailableIfBlank()
            val gender = columnPositions.genderFunction?.apply(row[columnPositions.gender], rowHtml[columnPositions.gender])
                    ?: row[columnPositions.gender].unavailableIfBlank()

            try {
                RunnerData.createRunnerData(logger, age, finishTime, gender, scrapeInfo.marathonYear, nationality, place, scrapeInfo.marathonSources)
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

@Component
class StandardWebScraperMergedAgeGender(@Autowired driverFactory: DriverFactory,
                                        @Autowired jsDriver: JsDriver,
                                        @Autowired markedCompleteService: MarkCompleteService<MergedAgedGenderColumnPositions, ResultsPage>,
                                        @Autowired usStateCodes: List<String>,
                                        @Autowired canadaProvinceCodes: List<String>,
                                        logger: Logger = LoggerFactory.getLogger(StandardWebScraperMergedAgeGender::class.java))
    : AbstractBaseScraper<MergedAgedGenderColumnPositions, ResultsPage, AbstractScrapeInfo<MergedAgedGenderColumnPositions, ResultsPage>>(driverFactory,
        jsDriver, markedCompleteService, ResultsPage::class.java, logger, usStateCodes, canadaProvinceCodes) {


    override fun processRow(row: List<String>, columnPositions: MergedAgedGenderColumnPositions, scrapeInfo: AbstractScrapeInfo<MergedAgedGenderColumnPositions, ResultsPage>, rowHtml: List<String>): RunnerData? {
        return try {
            val place = columnPositions.placeFunction?.apply(row[columnPositions.place], rowHtml[columnPositions.place])
                    ?: row[columnPositions.place].unavailableIfBlank()
            val nationality = columnPositions.nationalityFunction?.apply(row[columnPositions.nationality], rowHtml[columnPositions.nationality])
                    ?: row[columnPositions.nationality].unavailableIfBlank()
            val finishTime = columnPositions.finishTimeFunction?.apply(row[columnPositions.finishTime], rowHtml[columnPositions.finishTime])
                    ?: row[columnPositions.finishTime].unavailableIfBlank()

            val age = columnPositions.ageFunction?.apply(row[columnPositions.ageGender], rowHtml[columnPositions.ageGender])
                    ?: throw IllegalArgumentException("${StandardWebScraperMergedAgeGender::class.java} requires age function")
            val gender = columnPositions.genderFunction?.apply(row[columnPositions.ageGender], rowHtml[columnPositions.ageGender])
                    ?: throw IllegalArgumentException("${StandardWebScraperMergedAgeGender::class.java} requires gender function")

            try {
                RunnerData.createRunnerData(logger, age, finishTime, gender, scrapeInfo.marathonYear, nationality, place, scrapeInfo.marathonSources)
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