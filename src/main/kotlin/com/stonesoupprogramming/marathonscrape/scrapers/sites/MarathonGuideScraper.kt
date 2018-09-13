package com.stonesoupprogramming.marathonscrape.scrapers.sites

import com.stonesoupprogramming.marathonscrape.extension.*
import com.stonesoupprogramming.marathonscrape.models.AbstractScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.RunnerData
import com.stonesoupprogramming.marathonscrape.scrapers.AbstractBaseScraper
import com.stonesoupprogramming.marathonscrape.scrapers.DriverFactory
import com.stonesoupprogramming.marathonscrape.scrapers.JsDriver
import com.stonesoupprogramming.marathonscrape.scrapers.PreWebScrapeEvent
import com.stonesoupprogramming.marathonscrape.service.MarkCompleteService
import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

class MarathonGuidePreWebScrapeEvent(private val category: String) : PreWebScrapeEvent<MergedAgedGenderColumnPositions, ResultsPage> {

    private val logger = LoggerFactory.getLogger(MarathonGuidePreWebScrapeEvent::class.java)

    override fun execute(driver: RemoteWebDriver, jsDriver: JsDriver, scrapeInfo: AbstractScrapeInfo<MergedAgedGenderColumnPositions, ResultsPage>, attempt: Int, giveUp: Int) {
        try {
            val selectCss = "select[name=RaceRange]"
            val buttonCss = "input[name=SubmitButton]"

            driver.waitUntilClickable(selectCss.toCss())
            sleepRandom(0, 2)
            driver.selectComboBoxOption(selectCss.toCss(), category)
            driver.click(buttonCss.toCss(), logger)
            sleepRandom(0, 2)

        } catch (e: Exception) {
            logger.error("Pre web scrape event for category=$category failed, ScrapeInfo = $scrapeInfo", e)
            throw e
        }
    }
}

@Component
class MarathonGuideScraper(@Autowired driverFactory: DriverFactory,
                           @Autowired jsDriver: JsDriver,
                           @Autowired markedCompleteService: MarkCompleteService<MergedAgedGenderColumnPositions, ResultsPage>,
                           @Autowired usStateCodes: List<String>,
                           @Autowired canadaProvinceCodes: List<String>)
    : AbstractBaseScraper<MergedAgedGenderColumnPositions, ResultsPage, AbstractScrapeInfo<MergedAgedGenderColumnPositions, ResultsPage>>(driverFactory, jsDriver, markedCompleteService, ResultsPage::class.java, LoggerFactory.getLogger(MarathonGuideScraper::class.java), usStateCodes, canadaProvinceCodes) {

    override fun processRow(row: List<String>, columnPositions: MergedAgedGenderColumnPositions, scrapeInfo: AbstractScrapeInfo<MergedAgedGenderColumnPositions, ResultsPage>, rowHtml: List<String>): RunnerData? {
        val ageGender = row[columnPositions.ageGender]
        val gender = if (ageGender.isNotBlank()) {
            ageGender[ageGender.lastIndexOf("(") + 1].toString()
        } else {
            UNAVAILABLE
        }
        val age = if (ageGender.isNotBlank()) {
            val parts = ageGender.split(" ")
            val result = parts.last()
                    .replace("(", "")
                    .replace("M", "")
                    .replace("F", "")
                    .replace(")", "")
                    .trim()
            if(result.isBlank()){
                columnPositions.backupAge?.let {
                    try {
                        if(row[it].isNotBlank() && row[it][1].isDigit()){
                            row[it].substring(1)
                        } else {
                            UNAVAILABLE
                        }
                    } catch (e : Exception){
                        logger.debug(row.joinToString(", "), e)
                        UNAVAILABLE
                    }

                } ?: UNAVAILABLE
            } else {
                result.unavailableIfBlank()
            }
        } else {
            UNAVAILABLE
        }
        val place = row[columnPositions.place].unavailableIfBlank()
        val finishTime = row[columnPositions.finishTime].unavailableIfBlank()

        val nationality = scrapeInfo.columnPositions.splitFunc?.apply(row[columnPositions.nationality])
                ?: if (columnPositions.nationality != -1) {
                    row[columnPositions.nationality].toNationality(usStateCodes, canadaProvinceCodes, ", ")
                } else {
                    UNAVAILABLE
                }

        return try {
            RunnerData.createRunnerData(logger, age, finishTime, gender, scrapeInfo.marathonYear, nationality, place, scrapeInfo.marathonSources)
        } catch (e: Exception) {
            logger.error("Unable to create runner data", e)
            throw e
        }
    }
}