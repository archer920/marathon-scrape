package com.stonesoupprogramming.marathonscrape.scrapers.sites

import com.stonesoupprogramming.marathonscrape.enums.Gender
import com.stonesoupprogramming.marathonscrape.extension.selectComboBoxOption
import com.stonesoupprogramming.marathonscrape.extension.sleepRandom
import com.stonesoupprogramming.marathonscrape.extension.toCss
import com.stonesoupprogramming.marathonscrape.models.*
import com.stonesoupprogramming.marathonscrape.scrapers.*
import com.stonesoupprogramming.marathonscrape.service.MarkCompleteService
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

class TdsLivePreWebScrapeEvent(private val gender : Gender) : PreWebScrapeEvent<MergedAgedGenderColumnPositions, NumberedResultsPage> {

    private val logger = LoggerFactory.getLogger(TdsLivePreWebScrapeEvent::class.java)

    override fun execute(driver: RemoteWebDriver, jsDriver: JsDriver, scrapeInfo: AbstractScrapeInfo<MergedAgedGenderColumnPositions, NumberedResultsPage>, attempt: Int, giveUp: Int) {
        when(gender){
            Gender.MALE -> driver.selectComboBoxOption("#sessi".toCss(), "MASCHILE", logger)
            Gender.FEMALE -> driver.selectComboBoxOption("#sessi".toCss(), "FEMMINILE", logger)
            Gender.UNASSIGNED -> driver.selectComboBoxOption("#sessi".toCss(), "TUTTI", logger)
        }
        sleepRandom(min = 2, max = 5)
    }
}

@Component
class TdsLiveScraper(@Autowired driverFactory: DriverFactory,
                     @Autowired jsDriver: JsDriver,
                     @Autowired markedCompleteService: MarkCompleteService<MergedAgedGenderColumnPositions, NumberedResultsPage>,
                     @Autowired usStateCodes: List<String>,
                     @Autowired canadaProvinceCodes: List<String>) : AbstractPagedResultsScraper<MergedAgedGenderColumnPositions>(driverFactory, jsDriver, markedCompleteService, NumberedResultsPage::class.java, LoggerFactory.getLogger(TdsLiveScraper::class.java), usStateCodes, canadaProvinceCodes) {

    override fun webscrape(driver: RemoteWebDriver, scrapeInfo: PagedScrapeInfo<MergedAgedGenderColumnPositions>, rowProcessor: RowProcessor<MergedAgedGenderColumnPositions, NumberedResultsPage, PagedScrapeInfo<MergedAgedGenderColumnPositions>>?) {
        if(driver is FirefoxDriver){
            throw IllegalArgumentException("TDS Live will not scrape in Firefox due to Javascript bugs on the site")
        }
        super.webscrape(driver, scrapeInfo, rowProcessor)
    }

    override fun findCurrentPageNum(driver: RemoteWebDriver): Int {
        return try {
            driver.findElementByCssSelector("#page").getAttribute("value").toInt()
        } catch (e: java.lang.Exception) {
            logger.error("Unable to read page number", e)
            throw e
        }
    }
}