package com.stonesoupprogramming.marathonscrape.scrapers.philadelphia

import com.stonesoupprogramming.marathonscrape.extension.*
import com.stonesoupprogramming.marathonscrape.models.AbstractScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumPositions
import com.stonesoupprogramming.marathonscrape.models.NumberedResultsPage
import com.stonesoupprogramming.marathonscrape.scrapers.JsDriver
import com.stonesoupprogramming.marathonscrape.scrapers.PreWebScrapeEvent
import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.LoggerFactory

class PhiladelphiaPreWebScrapeEvent(private val category: String) : PreWebScrapeEvent<MergedAgedGenderColumPositions, NumberedResultsPage> {

    private val logger = LoggerFactory.getLogger(PhiladelphiaPreWebScrapeEvent::class.java)

    override fun execute(driver: RemoteWebDriver, jsDriver: JsDriver, scrapeInfo: AbstractScrapeInfo<MergedAgedGenderColumPositions, NumberedResultsPage>) {
        try {
            driver.waitUntilVisible("li.ui-state-default:nth-child(2)".toCss())
            if (scrapeInfo.marathonYear == 2016) {
                driver.selectComboBoxOption("#xact_results_event".toCss(), "2016 Philadelphia Marathon Race Weekend")
            }
            driver.click("li.ui-state-default:nth-child(2)".toCss(), logger)

            driver.waitUntilClickable("#xact_results_agegroup_agegroup".toCss())
            driver.selectComboBoxOption("#xact_results_agegroup_agegroup".toCss(), category)

            driver.selectComboBoxOption("#xact_results_agegroup_results_length > label > select".toCss(), "100")
            Thread.sleep(1000)
        } catch (e: Exception) {
            logger.error("Failed to execute pre-webscrape event", e)
            throw e
        }
    }
}