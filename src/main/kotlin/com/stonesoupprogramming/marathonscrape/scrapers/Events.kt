package com.stonesoupprogramming.marathonscrape.scrapers

import com.stonesoupprogramming.marathonscrape.models.AbstractColumnPositions
import com.stonesoupprogramming.marathonscrape.models.AbstractScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import org.openqa.selenium.remote.RemoteWebDriver

interface PreWebScrapeEvent<T : AbstractColumnPositions, U : ResultsPage> {
    fun execute(driver: RemoteWebDriver, jsDriver: JsDriver, scrapeInfo: AbstractScrapeInfo<T, U>)
}