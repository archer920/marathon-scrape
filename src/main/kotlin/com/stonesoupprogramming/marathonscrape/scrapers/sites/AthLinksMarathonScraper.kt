package com.stonesoupprogramming.marathonscrape.scrapers.sites

import com.stonesoupprogramming.marathonscrape.extension.*
import com.stonesoupprogramming.marathonscrape.models.*
import com.stonesoupprogramming.marathonscrape.scrapers.*
import com.stonesoupprogramming.marathonscrape.service.MarkCompleteService
import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

class AthLinksPreWebScrapeEvent(private val divisionCss: String) : PreWebScrapeEvent<AbstractColumnPositions, NumberedResultsPage> {

    private val logger = LoggerFactory.getLogger(AthLinksPreWebScrapeEvent::class.java)

    override fun execute(driver: RemoteWebDriver, jsDriver: JsDriver, scrapeInfo: AbstractScrapeInfo<AbstractColumnPositions, NumberedResultsPage>, attempt: Int, giveUp: Int) {
        try {
            driver.click("#division > div:nth-child(1) > svg".toCss(), logger)

            driver.scrollIntoView(divisionCss.toCss(), logger)
            driver.waitUntilClickable(divisionCss.toCss())
            driver.click(divisionCss.toCss(), logger)
        } catch (e : Exception){
            if(attempt < giveUp){
                Thread.sleep(1000)
                execute(driver, jsDriver, scrapeInfo, attempt + 1)
            } else {
                logger.error("Unable to click element", e)
                throw e
            }
        }

    }
}

@Component
class AthLinksMarathonScraper(@Autowired driverFactory: DriverFactory,
                              @Autowired private val athJsDriver: AthJsDriver,
                              @Autowired markedCompleteService: MarkCompleteService<AbstractColumnPositions, NumberedResultsPage>,
                              @Autowired usStateCodes: List<String>,
                              @Autowired canadaProvinceCodes: List<String>)
    : AbstractPagedResultsScraper<AbstractColumnPositions>(driverFactory, athJsDriver, markedCompleteService, NumberedResultsPage::class.java, LoggerFactory.getLogger(AthLinksMarathonScraper::class.java), usStateCodes, canadaProvinceCodes) {

    override fun processPage(driver: RemoteWebDriver, scrapeInfo: PagedScrapeInfo<AbstractColumnPositions>) {
        sleepRandom(1, 3)

        val tableData = athJsDriver.readPage(driver)
        if (tableData.isEmpty()) {
            throw IllegalStateException("pageData is empty on year=${scrapeInfo.marathonYear}, page = ${scrapeInfo.currentPage}")
        }

        val tableRows = tableData.map { it -> listOf(it["nationality"]!!, it["place"]!!, it["age"]!!, it["gender"]!!, it["finishTime"]!!) }
        val resultSet = tableRows.mapNotNull { processRow(it, scrapeInfo.columnPositions, scrapeInfo, emptyList()) }
        markCompleteService.markComplete(clazz, scrapeInfo, resultSet)
    }

    override fun processRow(row: List<String>, columnPositions: AbstractColumnPositions, scrapeInfo: PagedScrapeInfo<AbstractColumnPositions>, rowHtml: List<String>): RunnerData? {
        val nationality = row[0].toNationality(usStateCodes, canadaProvinceCodes)
        val place = row[1].unavailableIfBlank()
        val finishTime = row[4].unavailableIfBlank()
        val age = row[2].unavailableIfBlank()
        val gender = row[3].unavailableIfBlank()
        return try {
            RunnerData.createRunnerData(logger, age, finishTime, gender, scrapeInfo.marathonYear, nationality, place, scrapeInfo.marathonSources)
        } catch (e: Exception) {
            logger.error("Failed to create runner data", e)
            throw e
        }
    }

    override fun findCurrentPageNum(driver: RemoteWebDriver): Int = athJsDriver.findCurrentPage(driver)
}