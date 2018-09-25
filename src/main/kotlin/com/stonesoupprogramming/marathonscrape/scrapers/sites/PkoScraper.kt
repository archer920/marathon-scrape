package com.stonesoupprogramming.marathonscrape.scrapers.sites

import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.NumberedResultsPage
import com.stonesoupprogramming.marathonscrape.scrapers.AbstractPagedResultsScraper
import com.stonesoupprogramming.marathonscrape.scrapers.DriverFactory
import com.stonesoupprogramming.marathonscrape.scrapers.JsDriver
import com.stonesoupprogramming.marathonscrape.service.MarkCompleteService
import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PkoScraper(@Autowired driverFactory: DriverFactory,
                      @Autowired jsDriver: JsDriver,
                      @Autowired markedCompleteService: MarkCompleteService<MergedAgedGenderColumnPositions, NumberedResultsPage>,
                      @Autowired usStateCodes: List<String>,
                      @Autowired canadaProvinceCodes: List<String>) : AbstractPagedResultsScraper<MergedAgedGenderColumnPositions>(driverFactory, jsDriver, markedCompleteService, NumberedResultsPage::class.java, LoggerFactory.getLogger(PkoScraper::class.java), usStateCodes, canadaProvinceCodes) {

    override fun findCurrentPageNum(driver: RemoteWebDriver): Int {
        return try {
            jsDriver.readText(driver, "#table2 > tbody > tr > td:nth-child(1) > table > tbody > tr:nth-child(12) > td:nth-child(2) > center > big > big").split(" ").first().toInt() / 10 + 1
        } catch (e: Exception) {
            logger.error("Unable to read page number", e)
            throw e
        }
    }
}