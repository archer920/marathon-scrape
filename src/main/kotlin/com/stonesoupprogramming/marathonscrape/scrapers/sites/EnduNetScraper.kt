package com.stonesoupprogramming.marathonscrape.scrapers.sites

import com.stonesoupprogramming.marathonscrape.extension.click
import com.stonesoupprogramming.marathonscrape.extension.scrollIntoView
import com.stonesoupprogramming.marathonscrape.extension.sleepRandom
import com.stonesoupprogramming.marathonscrape.extension.toCss
import com.stonesoupprogramming.marathonscrape.models.*
import com.stonesoupprogramming.marathonscrape.scrapers.AbstractPagedResultsScraper
import com.stonesoupprogramming.marathonscrape.scrapers.DriverFactory
import com.stonesoupprogramming.marathonscrape.scrapers.JsDriver
import com.stonesoupprogramming.marathonscrape.scrapers.PreWebScrapeEvent
import com.stonesoupprogramming.marathonscrape.service.MarkCompleteService
import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

class EnduNetPreWebScrapeEvent(private var reloadHack : Boolean = false,
                               private val yearMap : Map<Int, String> = mapOf(2014 to "body > div.modal.ng-scope.center.am-fade-and-scale > div > div > form > div.modal-body.settings.settings-modal.mobileModal > div > table > tbody > tr:nth-child(4) > td:nth-child(1)",
                                       2015 to "body > div.modal.ng-scope.center.am-fade-and-scale > div > div > form > div.modal-body.settings.settings-modal.mobileModal > div > table > tbody > tr:nth-child(3) > td:nth-child(1)",
                                       2016 to "body > div.modal.ng-scope.center.am-fade-and-scale > div > div > form > div.modal-body.settings.settings-modal.mobileModal > div > table > tbody > tr:nth-child(2) > td:nth-child(1)",
                                       2017 to "body > div.modal.ng-scope.center.am-fade-and-scale > div > div > form > div.modal-body.settings.settings-modal.mobileModal > div > table > tbody > tr:nth-child(1) > td:nth-child(1)"),
                               private val yearView : String = "#contenitore > div.view.ng-scope > div.intero.eventoris.bgwhite.ng-scope > div.ng-scope > div:nth-child(1) > div > div > a > span",
                               private val resultsView : String = "#contenitore > div.view.ng-scope > div.intero.eventoris.bgwhite.ng-scope > div.ng-scope > div:nth-child(9) > div.bootstrap-table > div.fixed-table-container.table-no-bordered > div.fixed-table-pagination > div.pull-left.pagination-detail > span.page-list > span > button"): PreWebScrapeEvent<MergedAgedGenderColumnPositions, NumberedResultsPage>{

    private val logger = LoggerFactory.getLogger(EnduNetPreWebScrapeEvent::class.java)

    override fun execute(driver: RemoteWebDriver, jsDriver: JsDriver, scrapeInfo: AbstractScrapeInfo<MergedAgedGenderColumnPositions, NumberedResultsPage>, attempt: Int, giveUp: Int) {
        sleepRandom(min = 1, max = 10)
        pickYear(driver, jsDriver, scrapeInfo, attempt, giveUp)
        sleepRandom(min = 1, max = 10)
        pickResultsSize(driver, jsDriver, scrapeInfo, attempt, giveUp)
        sleepRandom(min = 1, max = 10)
    }

    private fun pickResultsSize(driver: RemoteWebDriver, jsDriver: JsDriver, scrapeInfo: AbstractScrapeInfo<MergedAgedGenderColumnPositions, NumberedResultsPage>, attempt: Int, giveUp: Int) {
        logger.info("Picking Results Size")
        try {
            driver.scrollIntoView(resultsView.toCss(), logger, top = false)
            if(reloadHack){
                reloadHack(driver, jsDriver, scrapeInfo, attempt, giveUp)
            }
            sleepRandom(1, 10)
                        //#contenitore > div.view.ng-scope > div.intero.eventoris.bgwhite.ng-scope > div.ng-scope > div:nth-child(9) > div.bootstrap-table > div.fixed-table-container.table-no-bordered > div.fixed-table-pagination > div.pull-left.pagination-detail > span.page-list > span > button
            driver.click("#contenitore > div.view.ng-scope > div.intero.eventoris.bgwhite.ng-scope > div.ng-scope > div:nth-child(9) > div.bootstrap-table > div.fixed-table-container.table-no-bordered > div.fixed-table-pagination > div.pull-left.pagination-detail > span.page-list > span > button".toCss(), logger)
            driver.click("#contenitore > div.view.ng-scope > div.intero.eventoris.bgwhite.ng-scope > div.ng-scope > div:nth-child(9) > div.bootstrap-table > div.fixed-table-container.table-no-bordered > div.fixed-table-pagination > div.pull-left.pagination-detail > span.page-list > span > ul > li:nth-child(4) > a".toCss(), logger)
        } catch (e : Exception){
            if (attempt < giveUp){
                driver.navigate().refresh()
                execute(driver, jsDriver,scrapeInfo, attempt + 1, giveUp)
            } else {
                logger.error("Unable to pick results page size", e)
                throw e
            }
        }
    }

    private fun reloadHack(driver: RemoteWebDriver, jsDriver: JsDriver, scrapeInfo: AbstractScrapeInfo<MergedAgedGenderColumnPositions, NumberedResultsPage>, attempt: Int, giveUp: Int){
        sleepRandom(min = 1, max = 5)
        pickYear(driver, jsDriver, scrapeInfo, attempt, giveUp)
        sleepRandom(min = 1, max = 5)
        driver.scrollIntoView(".fixed-table-pagination".toCss(), logger)
    }

    private fun pickYear(driver: RemoteWebDriver, jsDriver: JsDriver, scrapeInfo: AbstractScrapeInfo<MergedAgedGenderColumnPositions, NumberedResultsPage>, attempt: Int, giveUp: Int) {
        logger.info("Selecting year = ${scrapeInfo.marathonYear}")
        try {
            driver.scrollIntoView(yearView.toCss(), logger)
            sleepRandom(min = 2, max = 5)
            driver.scrollIntoView(yearView.toCss(), logger)

            driver.click("#contenitore > div.view.ng-scope > div.intero.eventoris.bgwhite.ng-scope > div.ng-scope > div:nth-child(1) > div > div > a > span".toCss(), logger)
            val yearCssSelector = yearMap[scrapeInfo.marathonYear] ?: throw IllegalArgumentException("Acceptable years are ${yearMap.keys}")
            sleepRandom(1, 10)
            driver.click(yearCssSelector.toCss(), logger)
        } catch (e : Exception){
            if(attempt < giveUp){
                driver.navigate().refresh()
                execute(driver, jsDriver,scrapeInfo, attempt + 1, giveUp)
            } else {
                logger.error("Unable to select year ${scrapeInfo.marathonYear}", e)
                throw e
            }
        }
    }
}
@Component
open class EnduNetScraper(@Autowired driverFactory: DriverFactory,
                     @Autowired jsDriver: JsDriver,
                     @Autowired markedCompleteService: MarkCompleteService<MergedAgedGenderColumnPositions, NumberedResultsPage>,
                     @Autowired usStateCodes: List<String>,
                     @Autowired canadaProvinceCodes: List<String>) : AbstractPagedResultsScraper<MergedAgedGenderColumnPositions>(driverFactory, jsDriver, markedCompleteService, NumberedResultsPage::class.java, LoggerFactory.getLogger(EnduNetScraper::class.java), usStateCodes, canadaProvinceCodes) {

    override fun processRow(row: List<String>, columnPositions: MergedAgedGenderColumnPositions, scrapeInfo: PagedScrapeInfo<MergedAgedGenderColumnPositions>, rowHtml: List<String>): RunnerData? {
        throw UnsupportedOperationException("Use a RowScraperObject")
    }

    override fun findCurrentPageNum(driver: RemoteWebDriver): Int {
        return try {
            jsDriver.readText(driver, ".pagination > .active > a").toInt()
        } catch (e: java.lang.Exception) {
            logger.error("Unable to read page number", e)
            throw e
        }
    }

    override fun scrollPage(driver: RemoteWebDriver, scrapeInfo: PagedScrapeInfo<MergedAgedGenderColumnPositions>, forward: Boolean) {
        val selector = if(forward){
            scrapeInfo.clickNextSelector
        } else {
            scrapeInfo.clickPreviousSelector
        }
        driver.scrollIntoView(selector.toCss(), logger)
        sleepRandom(min = 1, max = 2)
        driver.click(selector.toCss(), logger)
        sleepRandom(min = 1, max = 10)
    }

    override fun synchronizePages(driver: RemoteWebDriver, currentPage: Int, jsPage: Int, scrapeInfo: PagedScrapeInfo<MergedAgedGenderColumnPositions>, attempt: Int, giveUp: Int) {
        if(readLastPage(driver) != scrapeInfo.endPage){
            throw IllegalStateException("The end page provided does not match the UI, year = ${scrapeInfo.marathonYear}, scrapeInfo = $scrapeInfo")
        }
        if(readMarathonYear(driver) != scrapeInfo.marathonYear){
            throw java.lang.IllegalStateException("The marathon year does not match the UI, year = ${scrapeInfo.marathonYear}, scrapeInfo = $scrapeInfo")
        }
        super.synchronizePages(driver, currentPage, jsPage, scrapeInfo, attempt, giveUp)
    }

    protected open fun readLastPage(driver: RemoteWebDriver, attemptNum : Int = 0, giveUp: Int = 10) : Int {
        return try {
            jsDriver.readText(driver, ".pagination > .page-last > a").toInt()
        } catch (e : Exception){
            try {
                jsDriver.readText(driver, "li.page-number:nth-child(8)").toInt()
            } catch ( e : Exception){
                if(attemptNum < giveUp){
                    sleepRandom(min = 1, max = 5)
                    readLastPage(driver, attemptNum + 1, giveUp)
                } else {
                    logger.error("Unable to read last page number", e)
                    throw e
                }
            }
        }
    }

    protected open fun readMarathonYear(driver: RemoteWebDriver) : Int {
        return try {
            jsDriver.readText(driver, "#contenitore > div.view.ng-scope > div.intero.eventoris.bgwhite.ng-scope > div.ng-scope > div:nth-child(1) > div > div > strong").toInt()
        } catch (e : Exception){
            logger.error("Unable to read marathon year", e)
            throw e
        }
    }
}

@Component
class PadovaScraper(@Autowired driverFactory: DriverFactory,
                    @Autowired jsDriver: JsDriver,
                    @Autowired markedCompleteService: MarkCompleteService<MergedAgedGenderColumnPositions, NumberedResultsPage>,
                    @Autowired usStateCodes: List<String>,
                    @Autowired canadaProvinceCodes: List<String>) : EnduNetScraper(driverFactory, jsDriver, markedCompleteService, usStateCodes, canadaProvinceCodes){

    override fun scrollPage(driver: RemoteWebDriver, scrapeInfo: PagedScrapeInfo<MergedAgedGenderColumnPositions>, forward: Boolean) {
        val selector = if(forward){
            scrapeInfo.clickNextSelector
        } else {
            scrapeInfo.clickPreviousSelector
        }
        driver.scrollIntoView("div.intero:nth-child(20) > div:nth-child(1) > div:nth-child(11) > div:nth-child(2) > a:nth-child(1) > div:nth-child(1)".toCss(), logger, top = false)
        sleepRandom(min = 1, max = 2)
        driver.click(selector.toCss(), logger)
        sleepRandom(min = 1, max = 10)
    }
}