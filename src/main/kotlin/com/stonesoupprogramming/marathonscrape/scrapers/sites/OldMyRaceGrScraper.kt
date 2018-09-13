package com.stonesoupprogramming.marathonscrape.scrapers.sites

import com.stonesoupprogramming.marathonscrape.extension.calcAge
import com.stonesoupprogramming.marathonscrape.extension.unavailableIfBlank
import com.stonesoupprogramming.marathonscrape.models.AbstractScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.RunnerData
import com.stonesoupprogramming.marathonscrape.scrapers.AbstractBaseScraper
import com.stonesoupprogramming.marathonscrape.scrapers.DriverFactory
import com.stonesoupprogramming.marathonscrape.scrapers.JsDriver
import com.stonesoupprogramming.marathonscrape.service.MarkCompleteService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class OldMyRaceGrScraper(@Autowired driverFactory: DriverFactory,
                         @Autowired jsDriver: JsDriver,
                         @Autowired markedCompleteService: MarkCompleteService<MergedAgedGenderColumnPositions, ResultsPage>,
                         @Autowired usStateCodes : List<String>,
                         @Autowired canadaProvinceCodes : List<String>)
    : AbstractBaseScraper<MergedAgedGenderColumnPositions, ResultsPage, AbstractScrapeInfo<MergedAgedGenderColumnPositions, ResultsPage>>(driverFactory,
        jsDriver,
        markedCompleteService,
        ResultsPage::class.java,
        LoggerFactory.getLogger(OldMyRaceGrScraper::class.java),
        usStateCodes,
        canadaProvinceCodes) {

    override fun processRow(row: List<String>, columnPositions: MergedAgedGenderColumnPositions, scrapeInfo: AbstractScrapeInfo<MergedAgedGenderColumnPositions, ResultsPage>, rowHtml: List<String>): RunnerData? {
        val place = row[columnPositions.place].unavailableIfBlank()
        val age = parseAge(row[columnPositions.ageGender])
        val gender = parseGender(row[columnPositions.ageGender])
        val finishTime = row[columnPositions.finishTime]
        val nationality = parseNationality(row[columnPositions.ageGender])

        return try {
            RunnerData.createRunnerData(logger, age, finishTime, gender, scrapeInfo.marathonYear, nationality, place, scrapeInfo.marathonSources)
        } catch (e : Exception){
            logger.error("Unable to create runner data", e)
            throw e
        }
    }

    private fun parseAge(input : String) : String{
        return try {
            val parts = input.split(" - ")
            parts[1].split(" ").first().calcAge(logger, false)
        } catch (e : Exception){
            logger.error("Failed to extract age form $input", e)
            throw e
        }
    }

    private fun parseGender(input : String) : String {
        return try {
            val parts = input.split(" - ")
            parts[0].split(" ").last()
        } catch (e : Exception){
            logger.error("Failed to extract gender form $input", e)
            throw e
        }
    }

    private fun parseNationality(input : String) : String {
        return try {
            input.split(" - ")[2]
        } catch (e : Exception){
            logger.error("Failed to extract nationality form $input", e)
            throw e
        }
    }
}