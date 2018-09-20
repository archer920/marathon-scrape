package com.stonesoupprogramming.marathonscrape.scrapers.sites

import com.stonesoupprogramming.marathonscrape.extension.unavailableIfBlank
import com.stonesoupprogramming.marathonscrape.models.*
import com.stonesoupprogramming.marathonscrape.scrapers.AbstractPagedResultsScraper
import com.stonesoupprogramming.marathonscrape.scrapers.DriverFactory
import com.stonesoupprogramming.marathonscrape.scrapers.JsDriver
import com.stonesoupprogramming.marathonscrape.service.MarkCompleteService
import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SportsStatsScraper(@Autowired driverFactory: DriverFactory,
                         @Autowired jsDriver: JsDriver,
                         @Autowired markedCompleteService: MarkCompleteService<MergedAgedGenderColumnPositions, NumberedResultsPage>,
                         @Autowired usStateCodes: List<String>,
                         @Autowired canadaProvinceCodes : List<String> ) : AbstractPagedResultsScraper<MergedAgedGenderColumnPositions>(driverFactory, jsDriver, markedCompleteService, NumberedResultsPage::class.java, LoggerFactory.getLogger(SportsStatsScraper::class.java), usStateCodes, canadaProvinceCodes){

    override fun processRow(row: List<String>, columnPositions: MergedAgedGenderColumnPositions, scrapeInfo: PagedScrapeInfo<MergedAgedGenderColumnPositions>, rowHtml: List<String>): RunnerData? {
        return try {
            val place = row[columnPositions.place].unavailableIfBlank()
            val finishTime = row[columnPositions.finishTime].unavailableIfBlank()
            val nationality = row[columnPositions.nationality].unavailableIfBlank()

            val ageGender = row[columnPositions.ageGender]
            val gender = ageGender[0].toString()
            val age = ageGender.substring(1)

            try {
                RunnerData.createRunnerData(logger, age, finishTime, gender, scrapeInfo.marathonYear, nationality, place, scrapeInfo.marathonSources)
            } catch (e : Exception){
                logger.error("Failed to create runner data $row", e)
                throw e
            }
        } catch (e : Exception){
            logger.error("Failed to process row", e)
            throw e
        }
    }

    override fun findCurrentPageNum(driver: RemoteWebDriver): Int {
        return try {
            jsDriver.readText(driver, "li[class=active]").replace("\n", "").replace("\t", "").toInt()
        } catch (e : Exception){
            logger.error("Unable to read page number", e)
            throw e
        }
    }
}