package com.stonesoupprogramming.marathonscrape.scrapers.sites

import com.stonesoupprogramming.marathonscrape.extension.click
import com.stonesoupprogramming.marathonscrape.extension.toCss
import com.stonesoupprogramming.marathonscrape.extension.toNationality
import com.stonesoupprogramming.marathonscrape.extension.unavailableIfBlank
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumPositions
import com.stonesoupprogramming.marathonscrape.models.NumberedResultsPage
import com.stonesoupprogramming.marathonscrape.models.PagedScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.RunnerData
import com.stonesoupprogramming.marathonscrape.scrapers.AbstractPagedResultsScraper
import com.stonesoupprogramming.marathonscrape.scrapers.DriverFactory
import com.stonesoupprogramming.marathonscrape.scrapers.JsDriver
import com.stonesoupprogramming.marathonscrape.service.MarkCompleteService
import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class XacteMarathonScraper(@Autowired driverFactory: DriverFactory,
                           @Autowired jsDriver: JsDriver,
                           @Autowired markedCompleteService: MarkCompleteService<MergedAgedGenderColumPositions, NumberedResultsPage>,
                           @Autowired usStateCodes: List<String>,
                           @Autowired canadaProvinceCodes: List<String>)

    : AbstractPagedResultsScraper<MergedAgedGenderColumPositions>(driverFactory, jsDriver, markedCompleteService, NumberedResultsPage::class.java, LoggerFactory.getLogger(XacteMarathonScraper::class.java), usStateCodes, canadaProvinceCodes) {

    override fun findCurrentPageNum(driver: RemoteWebDriver): Int {
        return try {
            jsDriver.readText(driver, "#xact_results_agegroup_results_wrapper > div.fg-toolbar.ui-toolbar.ui-widget-header.ui-corner-bl.ui-corner-br.ui-helper-clearfix > div.dataTables_paginate.fg-buttonset.ui-buttonset.fg-buttonset-multi.ui-buttonset-multi.paging_full_numbers > span > .ui-state-disabled").toInt()
        } catch (e: Exception) {
            logger.error("Unable to read page number", e)
            throw e
        }
    }

    override fun processRow(row: List<String>, columnPositions: MergedAgedGenderColumPositions, scrapeInfo: PagedScrapeInfo<MergedAgedGenderColumPositions>, rowHtml: List<String>): RunnerData? {
        val place = row[columnPositions.place].unavailableIfBlank()
        val ageGender = row[columnPositions.ageGender]
        val gender = ageGender[0].toString()
        val age = ageGender.split("/").last().trim()
        val nationality = row[columnPositions.nationality].toNationality(usStateCodes, canadaProvinceCodes, " ")
        val finishTime = row[columnPositions.finishTime]

        return try {
            RunnerData.createRunnerData(logger, age, finishTime, gender, scrapeInfo.marathonYear, nationality, place, scrapeInfo.marathonSources)
        } catch (e: Exception) {
            logger.error("Failed to create runner data", e)
            throw e
        }
    }

    override fun scrollPage(driver: RemoteWebDriver, scrapeInfo: PagedScrapeInfo<MergedAgedGenderColumPositions>, forward: Boolean) {
        if (forward) {
            driver.click(scrapeInfo.clickNextSelector.toCss(), logger)
        } else {
            driver.click(scrapeInfo.clickPreviousSelector.toCss(), logger)
        }
    }
}