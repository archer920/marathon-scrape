package com.stonesoupprogramming.marathonscrape.scrapers.sites

import com.stonesoupprogramming.marathonscrape.extension.UNAVAILABLE
import com.stonesoupprogramming.marathonscrape.extension.unavailableIfBlank
import com.stonesoupprogramming.marathonscrape.models.AbstractScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumnPositions
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
                                 @Autowired markedCompleteService: MarkCompleteService<MergedAgedGenderColumnPositions, ResultsPage>,
                                 @Autowired usStateCodes: List<String>,
                                 @Autowired canadaProvinceCodes: List<String>)
    : AbstractBaseScraper<MergedAgedGenderColumnPositions, ResultsPage, AbstractScrapeInfo<MergedAgedGenderColumnPositions, ResultsPage>>(driverFactory,
        jsDriver,
        markedCompleteService,
        ResultsPage::class.java,
        LoggerFactory.getLogger(MultisportAustraliaScraper::class.java),
        usStateCodes,
        canadaProvinceCodes) {

    override fun processRow(row: List<String>, columnPositions: MergedAgedGenderColumnPositions, scrapeInfo: AbstractScrapeInfo<MergedAgedGenderColumnPositions, ResultsPage>, rowHtml: List<String>): RunnerData? {
        val pos = row[columnPositions.place].unavailableIfBlank()
        val finishTime = row[columnPositions.finishTime].unavailableIfBlank()
        val ageGender = row[columnPositions.ageGender]
        val age = ageGender.split(" ")[1].replace("(", "").replace(")", "")
        val gender = ageGender.split(" ")[0]

        return try {
            RunnerData.createRunnerData(logger, age, finishTime, gender, scrapeInfo.marathonYear, UNAVAILABLE, pos, scrapeInfo.marathonSources)
        } catch (e: Exception) {
            logger.error("Unable to create runner data", e)
            throw e
        }
    }
}