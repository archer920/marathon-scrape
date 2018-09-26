package com.stonesoupprogramming.marathonscrape.scrapers.sites

import com.stonesoupprogramming.marathonscrape.models.AgeGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.NumberedResultsPage
import com.stonesoupprogramming.marathonscrape.models.PagedScrapeInfo
import com.stonesoupprogramming.marathonscrape.scrapers.AbstractPagedResultsScraper
import com.stonesoupprogramming.marathonscrape.scrapers.DriverFactory
import com.stonesoupprogramming.marathonscrape.scrapers.JsDriver
import com.stonesoupprogramming.marathonscrape.scrapers.RowProcessor
import com.stonesoupprogramming.marathonscrape.service.MarkCompleteService
import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class KalendarzBiegowyScraper(@Autowired driverFactory: DriverFactory,
                     @Autowired jsDriver: JsDriver,
                     @Autowired markedCompleteService: MarkCompleteService<AgeGenderColumnPositions, NumberedResultsPage>,
                     @Autowired usStateCodes: List<String>,
                     @Autowired canadaProvinceCodes: List<String>) : AbstractPagedResultsScraper<AgeGenderColumnPositions>(driverFactory, jsDriver, markedCompleteService, NumberedResultsPage::class.java, LoggerFactory.getLogger(KalendarzBiegowyScraper::class.java), usStateCodes, canadaProvinceCodes) {

    private var currentPage = 0

    override fun processPage(driver: RemoteWebDriver, scrapeInfo: PagedScrapeInfo<AgeGenderColumnPositions>, rowProcessor: RowProcessor<AgeGenderColumnPositions, NumberedResultsPage, PagedScrapeInfo<AgeGenderColumnPositions>>?) {
        val tbody = "#page$currentPage > tbody"

        var table = jsDriver.readTableRows(driver, tbody)
        var tableHtml = jsDriver.readTableRows(driver, tbody, rawHtml = true)

        if (table.isEmpty()) {
            throw IllegalStateException("Failed to gather table information")
        }

        table = table.subList(scrapeInfo.skipRowCount, table.size - scrapeInfo.clipRows)
        table = scrapeInfo.tableRowFilter?.apply(table) ?: table

        tableHtml = tableHtml.subList(scrapeInfo.skipRowCount, tableHtml.size - scrapeInfo.clipRows)
        tableHtml = scrapeInfo.tableRowFilter?.apply(tableHtml) ?: tableHtml

        val resultSet = table.asSequence().mapIndexed { index, row ->
            rowProcessor?.processRow(row, scrapeInfo.columnPositions, scrapeInfo, tableHtml[index])
                    ?: throw IllegalArgumentException("Row processor is required") }.filterNotNull().toList()
        markCompleteService.markComplete(clazz, scrapeInfo, resultSet)
    }

    override fun synchronizePages(driver: RemoteWebDriver, currentPage: Int, jsPage: Int, scrapeInfo: PagedScrapeInfo<AgeGenderColumnPositions>, attempt: Int, giveUp: Int) {
        //Not needed for this site
    }

    override fun scrollPage(driver: RemoteWebDriver, scrapeInfo: PagedScrapeInfo<AgeGenderColumnPositions>, forward: Boolean) {
        //Not needed for this site
    }

    override fun findCurrentPageNum(driver: RemoteWebDriver): Int {
        currentPage++
        return currentPage
    }
}