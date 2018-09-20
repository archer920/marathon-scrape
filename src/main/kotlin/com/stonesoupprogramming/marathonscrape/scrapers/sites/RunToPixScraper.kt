package com.stonesoupprogramming.marathonscrape.scrapers.sites

import com.stonesoupprogramming.marathonscrape.enums.Gender
import com.stonesoupprogramming.marathonscrape.extension.UNAVAILABLE
import com.stonesoupprogramming.marathonscrape.extension.click
import com.stonesoupprogramming.marathonscrape.extension.toLatin
import com.stonesoupprogramming.marathonscrape.extension.unavailableIfBlank
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.NumberedResultsPage
import com.stonesoupprogramming.marathonscrape.models.PagedScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.RunnerData
import com.stonesoupprogramming.marathonscrape.scrapers.AbstractPagedResultsScraper
import com.stonesoupprogramming.marathonscrape.scrapers.DriverFactory
import com.stonesoupprogramming.marathonscrape.scrapers.JsDriver
import com.stonesoupprogramming.marathonscrape.service.MarkCompleteService
import org.openqa.selenium.By
import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RunToPixScraper(@Autowired driverFactory: DriverFactory,
                      @Autowired jsDriver: JsDriver,
                      @Autowired markedCompleteService: MarkCompleteService<MergedAgedGenderColumnPositions, NumberedResultsPage>,
                      @Autowired usStateCodes: List<String>,
                      @Autowired canadaProvinceCodes: List<String>) :
        AbstractPagedResultsScraper<MergedAgedGenderColumnPositions>(
                driverFactory = driverFactory,
                jsDriver = jsDriver,
                markedCompleteService = markedCompleteService,
                clazz = NumberedResultsPage::class.java,
                logger = LoggerFactory.getLogger(RunToPixScraper::class.java),
                usStateCodes = usStateCodes,
                canadaProvinceCodes = canadaProvinceCodes) {

    override fun processRow(row: List<String>, columnPositions: MergedAgedGenderColumnPositions, scrapeInfo: PagedScrapeInfo<MergedAgedGenderColumnPositions>, rowHtml: List<String>): RunnerData? {
        return try {
            val place = row[columnPositions.place].unavailableIfBlank()
            val nationality = if(columnPositions.nationality == -1) { UNAVAILABLE } else { row[columnPositions.nationality] }
            val finishTime = row[columnPositions.finishTime]
            val ageGender = row[columnPositions.ageGender]
            val age = ageGender.substring(1).toLatin()
            val gender = Gender.Lookup.fromCode(ageGender[0].toString()).code

            try {
                RunnerData.createRunnerData(logger = logger, age = age, finishTime = finishTime, gender = gender, year = scrapeInfo.marathonYear, nationality = nationality, place = place, source = scrapeInfo.marathonSources)
            } catch (e : Exception){
                logger.error("Failed to create runner data", e)
                throw e
            }
        } catch (e : Exception){
            logger.error("Failed to process the row", e)
            throw e
        }
    }

    override fun findCurrentPageNum(driver: RemoteWebDriver): Int {
        return try {
            val parts = jsDriver.readText(driver, "body > table:nth-child(4) > tbody:nth-child(1) > tr:nth-child(6) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1)")
            val pageNum = parts.split(" ").last().split(":").last().split("/").first()
            pageNum.toInt()
        } catch (e : Exception){
            logger.error("Failed to read page number", e)
            throw e
        }
    }

    override fun scrollPage(driver: RemoteWebDriver, scrapeInfo: PagedScrapeInfo<MergedAgedGenderColumnPositions>, forward: Boolean) {
        if (forward) {
            driver.click(By.linkText("Next"), logger)
        } else {
            driver.click(By.linkText("Previous"), logger)
        }
    }
}

