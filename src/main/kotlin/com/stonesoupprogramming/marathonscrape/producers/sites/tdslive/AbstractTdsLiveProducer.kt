package com.stonesoupprogramming.marathonscrape.producers.sites.tdslive

import com.stonesoupprogramming.marathonscrape.enums.Gender
import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.extension.UNAVAILABLE
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.PagedScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.sites.TdsScrapeInfo
import com.stonesoupprogramming.marathonscrape.producers.AbstractNumberedResultsPageProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.scrapers.StandardMergedAgeGenderRowProcessor
import com.stonesoupprogramming.marathonscrape.scrapers.sites.TdsLiveScraper
import org.slf4j.Logger
import java.util.function.BiFunction

abstract class AbstractTdsLiveProducer(private val scraper: TdsLiveScraper,
                                       numberedPagedResultsRepository: NumberedResultsPageRepository,
                                       logger : Logger,
                                       marathonSources: MarathonSources,
                                       private val tdsScrapeInfoList : List<TdsScrapeInfo>) : AbstractNumberedResultsPageProducer(numberedPagedResultsRepository, logger, marathonSources) {

    private val scrapeInfo = PagedScrapeInfo(
            url = "",
            marathonSources = marathonSources,
            marathonYear = -1,
            tableBodySelector = "#results > table > tbody",
            skipRowCount = 1,
            columnPositions = MergedAgedGenderColumnPositions(
                    place = 0,
                    nationality = 6,
                    nationalityFunction = BiFunction { _, html ->
                        try {
                            val parts = html.split(" ")
                            val alt = parts.find { it -> it.startsWith("alt") } ?: UNAVAILABLE
                            if(alt != UNAVAILABLE){
                                alt.replace("alt=", "").replace("\"", "")
                            } else {
                                alt
                            }
                        } catch (e : Exception){
                            logger.error("Failed to parse Nationality", e)
                            throw e
                        }
                    },
                    ageGender = 7,
                    genderFunction = BiFunction { text, _ ->
                        try {
                            if(text == "N/D"){
                                UNAVAILABLE
                            } else {
                                when {
                                    text.endsWith("M") -> Gender.MALE.code
                                    text.endsWith("F") -> Gender.FEMALE.code
                                    else -> UNAVAILABLE
                                }
                            }
                        } catch (e : Exception){
                            logger.error("Failed to parse gender", e)
                            throw e
                        }

                    },
                    ageFunction = BiFunction { text, _ ->
                        try {
                            if(text == "N/D" || text.isBlank()){
                                UNAVAILABLE
                            } else {
                                val age = text.replace("OVER", "").replace("M", "").replace("F", "").replace("S", "")
                                if(age.isBlank()){
                                    UNAVAILABLE
                                } else {
                                    age
                                }
                            }
                        } catch (e : Exception){
                            logger.error("Failed to parse age", e)
                            throw e
                        }

                    },
                    finishTime = 7
            ),
            startPage = 0,
            currentPage = 0,
            endPage = -1,
            clickNextSelector = "#rankingBottomBar > center > table > tbody > tr > td:nth-child(10) > div > a",
            clickPreviousSelector = "#rankingBottomBar > center > table > tbody > tr > td:nth-child(4) > div > a"
    )


    override fun buildYearlyThreads(year: Int, lastPage: Int) {

        tdsScrapeInfoList.find { tds -> tds.sequenceLinks.year == year }?.let { tds ->
            if(tds.preWebScrapeEvent != null){
                val columnPositions = scrapeInfo.columnPositions.copy(ageGender = -1)
                threads.add(scraper.scrape(scrapeInfo.copy(marathonYear = year, url = tds.sequenceLinks.url, endPage = tds.sequenceLinks.endPage, startPage = lastPage, columnPositions = columnPositions, gender = tds.gender), preWebScrapeEvent = tds.preWebScrapeEvent, rowProcessor = StandardMergedAgeGenderRowProcessor()))
            } else {
                val columnPositions = scrapeInfo.columnPositions.copy(finishTime = 8)
                threads.add(scraper.scrape(scrapeInfo.copy(marathonYear = year, url = tds.sequenceLinks.url, endPage = tds.sequenceLinks.endPage, startPage = lastPage, columnPositions = columnPositions), rowProcessor = StandardMergedAgeGenderRowProcessor()))
            }
        }
    }
}