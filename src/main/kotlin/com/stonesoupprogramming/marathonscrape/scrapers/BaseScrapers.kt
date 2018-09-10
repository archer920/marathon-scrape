package com.stonesoupprogramming.marathonscrape.scrapers

import com.stonesoupprogramming.marathonscrape.extension.failResult
import com.stonesoupprogramming.marathonscrape.extension.successResult
import com.stonesoupprogramming.marathonscrape.models.*
import com.stonesoupprogramming.marathonscrape.service.MarkCompleteService
import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.Logger
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

abstract class AbstractBaseScraper<T : AbstractColumnPositions, U : ResultsPage, V : AbstractScrapeInfo<T, U>>(private val driverFactory: DriverFactory,
                                                                                                               protected val jsDriver: JsDriver,
                                                                                                               protected val markCompleteService: MarkCompleteService<T, U>,
                                                                                                               protected val clazz: Class<U>,
                                                                                                               protected val logger: Logger,
                                                                                                               protected val usStateCodes: List<String>,
                                                                                                               protected val canadaProvinceCodes: List<String>) {

    @Async
    open fun scrape(scrapeInfo: V, preWebScrapeEvent: PreWebScrapeEvent<T, U>? = null): CompletableFuture<String> {
        val driver = driverFactory.createDriver()

        return try {
            driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
            driver.get(scrapeInfo.url)

            preWebScrapeEvent?.execute(driver, jsDriver, scrapeInfo)
            webscrape(driver, scrapeInfo)

            successResult()
        } catch (e: Exception) {
            logger.error("Failed to scrape $scrapeInfo", e)
            failResult()
        } finally {
            driverFactory.destroy(driver)
        }
    }

    protected open fun processPage(driver: RemoteWebDriver, scrapeInfo: V) {
        var table = jsDriver.readTableRows(driver, scrapeInfo.tableBodySelector)
        var tableHtml = jsDriver.readTableRows(driver, scrapeInfo.tableBodySelector, rawHtml = true)

        if (table.isEmpty()) {
            throw IllegalStateException("Failed to gather table information")
        }

        table = table.subList(scrapeInfo.skipRowCount, table.size)
        tableHtml = tableHtml.subList(scrapeInfo.skipRowCount, tableHtml.size)

        val resultSet = table.mapIndexed { index, row -> processRow(row, scrapeInfo.columnPositions, scrapeInfo, tableHtml[index]) }.filterNotNull()
        markCompleteService.markComplete(clazz, scrapeInfo, resultSet)
    }

    abstract fun processRow(row: List<String>, columnPositions: T, scrapeInfo: V, rowHtml: List<String>): RunnerData?

    protected open fun webscrape(driver: RemoteWebDriver, scrapeInfo: V) {
        processPage(driver, scrapeInfo)
    }
}

abstract class AbstractPagedResultsScraper<T : AbstractColumnPositions>(
        driverFactory: DriverFactory,
        jsDriver: JsDriver,
        markedCompleteService: MarkCompleteService<T, NumberedResultsPage>,
        clazz: Class<NumberedResultsPage>,
        logger: Logger,
        usStateCodes: List<String>,
        canadaProvinceCodes: List<String>)
    : AbstractBaseScraper<T, NumberedResultsPage, PagedScrapeInfo<T>>(driverFactory, jsDriver, markedCompleteService, clazz, logger, usStateCodes, canadaProvinceCodes) {

    override fun webscrape(driver: RemoteWebDriver, scrapeInfo: PagedScrapeInfo<T>) {
        if (scrapeInfo.startPage > scrapeInfo.endPage) {
            throw IllegalStateException("Start page can't be after end page: start=${scrapeInfo.startPage}, end=${scrapeInfo.endPage}")
        }

        for (page in 1 until scrapeInfo.startPage) {
            logger.info("On page=$page, advancing to page=${scrapeInfo.startPage}, for year = ${scrapeInfo.marathonYear}")
            synchronizePages(driver, page, findCurrentPageNum(driver), scrapeInfo)
        }

        logger.info("Arrived at starting page! (${scrapeInfo.startPage})")

        var currentScrapeInfo = scrapeInfo
        for (page in scrapeInfo.startPage..scrapeInfo.endPage) {
            synchronizePages(driver, page, findCurrentPageNum(driver), currentScrapeInfo)
            currentScrapeInfo = currentScrapeInfo.copy(currentPage = page)
            processPage(driver, currentScrapeInfo)
        }
    }

    protected abstract fun findCurrentPageNum(driver: RemoteWebDriver): Int

    protected open fun synchronizePages(driver: RemoteWebDriver, currentPage: Int, jsPage: Int, scrapeInfo: PagedScrapeInfo<T>, attempt: Int = 0, giveUp: Int = 10) {
        logger.info("page = $currentPage, ui page = $jsPage, endPage = ${scrapeInfo.endPage}, year = ${scrapeInfo.marathonYear}, scrapeInfo = $scrapeInfo")

        try {
            when {
                jsPage == -1 -> {
                    if (attempt < giveUp) {
                        scrollPage(driver, scrapeInfo)
                        Thread.sleep(1000)
                        synchronizePages(driver, currentPage, findCurrentPageNum(driver), scrapeInfo, attempt + 1)
                    } else {
                        driver.navigate().refresh()
                        synchronizePages(driver, currentPage, findCurrentPageNum(driver), scrapeInfo)
                    }
                }
                currentPage < jsPage -> {
                    scrollPage(driver, scrapeInfo, forward = false)
                    Thread.sleep(1000)
                    synchronizePages(driver, currentPage, findCurrentPageNum(driver), scrapeInfo)

                }
                currentPage > jsPage -> {
                    scrollPage(driver, scrapeInfo)
                    Thread.sleep(1000)
                    synchronizePages(driver, currentPage, findCurrentPageNum(driver), scrapeInfo)
                }
            }
        } catch (e: Exception) {
            if (attempt < giveUp) {
                driver.get(scrapeInfo.url)
                synchronizePages(driver, currentPage, findCurrentPageNum(driver), scrapeInfo, attempt + 1)
            } else {
                logger.error("Unable to synchronize pages", e)
                throw e
            }
        }

    }

    private fun pickSelector(driver: RemoteWebDriver, scrapeInfo: PagedScrapeInfo<T>): String {
        return if (findCurrentPageNum(driver) <= 1) {
            scrapeInfo.clickNextSelector
        } else {
            scrapeInfo.secondaryClickNextSelector ?: scrapeInfo.clickNextSelector
        }
    }

    protected open fun scrollPage(driver: RemoteWebDriver, scrapeInfo: PagedScrapeInfo<T>, forward: Boolean = true) {
        if (forward) {
            jsDriver.clickElement(driver, pickSelector(driver, scrapeInfo))
        } else {
            jsDriver.clickElement(driver, scrapeInfo.clickPreviousSelector)
        }
    }
}