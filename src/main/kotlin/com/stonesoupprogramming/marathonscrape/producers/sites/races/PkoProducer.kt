package com.stonesoupprogramming.marathonscrape.producers.sites.races

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.extension.UNAVAILABLE
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.NumberedResultsPage
import com.stonesoupprogramming.marathonscrape.models.PagedScrapeInfo
import com.stonesoupprogramming.marathonscrape.producers.AbstractNumberedResultsPageProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.scrapers.StandardMergedAgeGenderRowProcessor
import com.stonesoupprogramming.marathonscrape.scrapers.sites.PkoScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.function.BiFunction

@Component
class PkoProducer(@Autowired private val pkoScraper: PkoScraper,
                  @Autowired numberedResultsPageRepository: NumberedResultsPageRepository) : AbstractNumberedResultsPageProducer(numberedResultsPageRepository, LoggerFactory.getLogger(PkoProducer::class.java), MarathonSources.PKO){

    val url2014 = "https://online.datasport.pl/results1284/index.php"
    val url2015 = "https://wyniki.datasport.pl/results1581/index.php"

    private val scrapeInfo = PagedScrapeInfo(
            url = "",
            marathonSources = marathonSources,
            marathonYear = -1,
            tableBodySelector = "#table2 > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(28) > tbody:nth-child(1)",
            clipRows = 2,
            skipRowCount = 1,
            clickNextSelector = "#table2 > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(28) > tbody:nth-child(1) > tr:nth-child(12) > td:nth-child(3) > button:nth-child(3)",
            clickPreviousSelector = "#table2 > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(28) > tbody:nth-child(1) > tr:nth-child(12) > td:nth-child(1) > button:nth-child(3)",
            columnPositions = MergedAgedGenderColumnPositions(
                    nationality = 2,
                    finishTime = 6,
                    ageGender = 5,
                    place = 0,
                    ageFunction = BiFunction { txt, html ->
                        try {
                            val first = txt.split("-").first()
                            val age = first.replace("M", "").replace("K", "")
                            if(age.isBlank()){
                                UNAVAILABLE
                            } else {
                                age
                            }
                        } catch (e : Exception){
                            logger.error("Unable to find age", e)
                            throw e
                        }
                    },
                    genderFunction = BiFunction { txt, html ->
                        try {
                            val letter = txt[0].toString()
                            if(letter == "K"){
                                "F"
                            } else {
                                letter
                            }
                        } catch (e : Exception){
                            logger.error("Unable to determine gender", e)
                            throw e
                        }
                    }
            ),
            currentPage = 0,
            startPage = 0,
            endPage = 0
    )

    override fun buildYearlyThreads(year: Int, lastPage: Int) {
        when(year){
            2014 -> {
                threads.add(pkoScraper.scrape(scrapeInfo.copy(marathonYear = year, url = url2014, startPage = lastPage, endPage = 385), rowProcessor = StandardMergedAgeGenderRowProcessor()))
            }
            2015 -> {
                threads.add(pkoScraper.scrape(scrapeInfo.copy(marathonYear = year,
                        tableBodySelector = "#table2 > tbody > tr > td:nth-child(1) > table > tbody",
                        clickNextSelector = "#table2 > tbody > tr > td:nth-child(1) > table > tbody > tr:nth-child(12) > td:nth-child(3) > button:nth-child(3)",
                        clickPreviousSelector = "#table2 > tbody > tr > td:nth-child(1) > table > tbody > tr:nth-child(12) > td:nth-child(1) > button:nth-child(3)",
                        url = url2015,
                        startPage = lastPage,
                        endPage = 476), rowProcessor = StandardMergedAgeGenderRowProcessor()))
            }
        }
    }
}