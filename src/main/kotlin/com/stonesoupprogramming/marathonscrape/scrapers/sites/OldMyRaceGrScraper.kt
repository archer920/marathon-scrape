package com.stonesoupprogramming.marathonscrape.scrapers.sites

import com.stonesoupprogramming.marathonscrape.extension.UNAVAILABLE
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
        if (row.size == 1) {
            return null
        }

        return try {
            val place = row[columnPositions.place].unavailableIfBlank()
            val age = parseAge(row[columnPositions.ageGender])
            val gender = parseGender(row[columnPositions.ageGender])
            val finishTime = parseFinishTime(row[columnPositions.finishTime])
            val nationality = parseNationality(row[columnPositions.ageGender]).unavailableIfBlank()

            try {
                RunnerData.createRunnerData(logger, age, finishTime, gender, scrapeInfo.marathonYear, nationality, place, scrapeInfo.marathonSources)
            } catch (e: Exception) {
                logger.error("Unable to create runner data", e)
                throw e
            }
        } catch (e: java.lang.Exception) {
            logger.error("Failed to parse results page", e)
            throw e
        }
    }

    private fun parseAge(input : String) : String{
        return try {
            val parts = input.split(" - ")
            val ageParts = parts.find { it -> it.contains("19") }
            val age = ageParts?.split(" ")?.first()?.calcAge(logger, false) ?: UNAVAILABLE
            age
        } catch (e : Exception){
            try {
                val parts = input.split(" - ")
                parts[2].split(" ").first().calcAge(logger, false)
            } catch (e : Exception){
                logger.error("Failed to extract age form $input", e)
                throw e
            }
        }
    }

    private fun parseGender(input : String) : String {
        val parts = input.split(" - ")
        return try {
            parts[0].split("\n")[1].replace("\t", "")
        } catch (e : Exception){
            try {
                val parts = input.split(" - ")
                parts[1].split("\n").last().replace("\t", "")
            } catch (e : Exception){
                logger.error("Failed to extract gender form $input", e)
                throw e
            }
        }
    }

    private fun parseNationality(input : String) : String {
        val parts = input.split(" - ")
        return try {
            val nationality = parts[2]
            if(nationality.startsWith("19")){
                parts[3]
            } else {
                nationality
            }
        } catch (e : Exception){
            if(parts.size == 2){
                UNAVAILABLE
            } else {
                logger.error("Failed to extract nationality from $input", e)
                throw e
            }
        }
    }

    private fun parseFinishTime(input: String): String {
        return try {
            val parts = input.replace("\t", "").split("\n")
            parts[parts.size - 2].trim()
        } catch (e: Exception) {
            logger.error("Failed to find finish time", e)
            throw e
        }
    }
}