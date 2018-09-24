package com.stonesoupprogramming.marathonscrape.scrapers

import com.stonesoupprogramming.marathonscrape.extension.UNAVAILABLE
import com.stonesoupprogramming.marathonscrape.extension.unavailableIfBlank
import com.stonesoupprogramming.marathonscrape.models.*
import org.slf4j.LoggerFactory

interface RowProcessor<T : AbstractColumnPositions, U : ResultsPage, V : AbstractScrapeInfo<T, U>> {
    fun processRow(row: List<String>, columnPositions: T, scrapeInfo: V, rowHtml: List<String>): RunnerData?
}

class StandardAgeGenderRowProcessor<U : ResultsPage, V : AbstractScrapeInfo<AgeGenderColumnPositions, U>> : RowProcessor<AgeGenderColumnPositions, U, V> {

    private val logger = LoggerFactory.getLogger(StandardAgeGenderRowProcessor::class.java)

    override fun processRow(row: List<String>, columnPositions: AgeGenderColumnPositions, scrapeInfo: V, rowHtml: List<String>): RunnerData? {
        return try {
            val place = columnPositions.placeFunction?.apply(row[columnPositions.place], rowHtml[columnPositions.place])
                    ?: row[columnPositions.place].unavailableIfBlank()
            val nationality = columnPositions.nationalityFunction?.apply(row[columnPositions.nationality], rowHtml[columnPositions.nationality])
                    ?: row[columnPositions.nationality].unavailableIfBlank()
            val finishTime = columnPositions.finishTimeFunction?.apply(row[columnPositions.finishTime], rowHtml[columnPositions.finishTime])
                    ?: row[columnPositions.finishTime].unavailableIfBlank()
            val age = columnPositions.ageFunction?.apply(row[columnPositions.age], rowHtml[columnPositions.age])
                    ?: row[columnPositions.age].unavailableIfBlank()
            val gender = columnPositions.genderFunction?.apply(row[columnPositions.gender], rowHtml[columnPositions.gender])
                    ?: row[columnPositions.gender].unavailableIfBlank()

            try {
                RunnerData.createRunnerData(logger, age, finishTime, gender, scrapeInfo.marathonYear, nationality, place, scrapeInfo.marathonSources)
            } catch (e: Exception) {
                logger.error("Unable to create runner data", e)
                throw e
            }
        } catch (e: Exception) {
            logger.error("Unable to process row", e)
            throw e
        }
    }
}

class StandardMergedAgeGenderRowProcessor<U : ResultsPage, V : AbstractScrapeInfo<MergedAgedGenderColumnPositions, U>> : RowProcessor<MergedAgedGenderColumnPositions, U, V> {

    private val logger = LoggerFactory.getLogger(StandardMergedAgeGenderRowProcessor::class.java)

    override fun processRow(row: List<String>, columnPositions: MergedAgedGenderColumnPositions, scrapeInfo: V, rowHtml: List<String>): RunnerData? {
        return try {
            val place = if(columnPositions.place != -1){
                columnPositions.placeFunction?.apply(row[columnPositions.place], rowHtml[columnPositions.place])
                        ?: row[columnPositions.place].unavailableIfBlank()
            } else {
                UNAVAILABLE
            }
            val nationality = if(columnPositions.nationality != -1){
                columnPositions.nationalityFunction?.apply(row[columnPositions.nationality], rowHtml[columnPositions.nationality])
                        ?: row[columnPositions.nationality].unavailableIfBlank()
            } else {
                UNAVAILABLE
            }
            val finishTime = if(columnPositions.finishTime != -1) {
                columnPositions.finishTimeFunction?.apply(row[columnPositions.finishTime], rowHtml[columnPositions.finishTime])
                        ?: row[columnPositions.finishTime].unavailableIfBlank()
            } else {
                UNAVAILABLE
            }

            val age = if(columnPositions.ageGender != -1){
                columnPositions.ageFunction?.apply(row[columnPositions.ageGender], rowHtml[columnPositions.ageGender])
                        ?: throw IllegalArgumentException("${StandardWebScraperMergedAgeGender::class.java} requires age function")
            } else {
                UNAVAILABLE
            }
            val gender = if(columnPositions.ageGender != -1){
                columnPositions.genderFunction?.apply(row[columnPositions.ageGender], rowHtml[columnPositions.ageGender])
                        ?: throw IllegalArgumentException("${StandardWebScraperMergedAgeGender::class.java} requires gender function")
            } else {
                scrapeInfo.gender?.code ?: UNAVAILABLE
            }

            try {
                RunnerData.createRunnerData(logger, age, finishTime, gender, scrapeInfo.marathonYear, nationality, place, scrapeInfo.marathonSources)
            } catch (e: Exception) {
                logger.error("Unable to create runner data", e)
                throw e
            }
        } catch (e: Exception) {
            logger.error("Unable to process row", e)
            throw e
        }
    }
}