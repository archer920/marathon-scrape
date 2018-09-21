package com.stonesoupprogramming.marathonscrape.producers.sites.endunet

import com.stonesoupprogramming.marathonscrape.enums.Gender
import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.extension.UNAVAILABLE
import com.stonesoupprogramming.marathonscrape.extension.isNumeric
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.PagedScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.SequenceLinks
import com.stonesoupprogramming.marathonscrape.producers.AbstractNumberedResultsPageProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.scrapers.StandardMergedAgeGenderRowProcessor
import com.stonesoupprogramming.marathonscrape.scrapers.sites.EnduNetPreWebScrapeEvent
import com.stonesoupprogramming.marathonscrape.scrapers.sites.EnduNetScraper
import org.slf4j.Logger
import java.util.function.BiFunction

abstract class AbstractEnduNetProducer(private val enduNetScraper: EnduNetScraper,
                              numberedPagedResultsRepository: NumberedResultsPageRepository,
                              logger : Logger,
                              marathonSources: MarathonSources,
                              private val sequenceLinks : List<SequenceLinks>) : AbstractNumberedResultsPageProducer(numberedPagedResultsRepository, logger, marathonSources) {

    private val scrapeInfo = PagedScrapeInfo(
            url = "",
            marathonSources = marathonSources,
            marathonYear = -1,
            tableBodySelector = "#ranksTable > tbody:nth-child(2)",
            skipRowCount = 0,
            columnPositions = MergedAgedGenderColumnPositions(
                    place = 0,
                    nationality = 2,
                    nationalityFunction = BiFunction { _, html ->
                        try {
                            val line = html.split("<br>").last()
                            val nationality = line.replace("<span style=\"text-transform: uppercase;\">", "").replace("</span>", "")
                            if(nationality.contains(",")){
                                nationality.split(",").first()
                            } else {
                                nationality
                            }
                        } catch (e : Exception){
                            logger.error("Failed to parse Nationality", e)
                            throw e
                        }
                    },
                    ageGender = 3,
                    genderFunction = BiFunction { _, html ->
                        try {
                            val gender = html.split("<br>").last()
                            when {
                                gender.contains("M") -> Gender.MALE.code
                                gender.contains("F") -> Gender.FEMALE.code
                                else -> Gender.UNASSIGNED.code
                            }
                        } catch (e : Exception){
                            logger.error("Failed to parse gender", e)
                            throw e
                        }

                    },
                    ageFunction = BiFunction { _, html ->
                        try {
                            var age = html.split("<br>").last()
                            if(age.isBlank() || age == "."){
                                UNAVAILABLE
                            } else {
                                age = age.substring(age.length - 2)
                                if(age.isNumeric()){
                                    age
                                } else {
                                    UNAVAILABLE
                                }
                            }
                        } catch (e : Exception){
                            logger.error("Failed to parse age", e)
                            throw e
                        }

                    },
                    finishTime = 5,
                    finishTimeFunction = BiFunction { txt, _ ->
                        try {
                            val time = txt.replace("-", "")
                            if(time.contains("+")){
                                time.split("+").first()
                            } else{
                                time
                            }
                        } catch (e : Exception){
                            logger.error("Failed to parse finish time", e)
                            throw e
                        }
                    }
            ),
            startPage = 0,
            currentPage = 0,
            endPage = -1,
            clickNextSelector = ".pagination > .page-next > a:nth-child(1)",
            clickPreviousSelector = ".pagination > .page-pre > a:nth-child(1)"
    )


    override fun buildYearlyThreads(year: Int, lastPage: Int) {
        sequenceLinks.find { sl -> sl.year == year }?.let { sl ->
            threads.add(enduNetScraper.scrape(
                    scrapeInfo.copy(url = sl.url, marathonYear = sl.year, startPage = lastPage, endPage = sl.endPage),
                    EnduNetPreWebScrapeEvent(sl.reloadHack),
                    StandardMergedAgeGenderRowProcessor()))
        }
    }
}