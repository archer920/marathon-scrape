package com.stonesoupprogramming.marathonscrape.producers.sites.races

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.PagedScrapeInfo
import com.stonesoupprogramming.marathonscrape.producers.AbstractNumberedResultsPageProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.SportsStatsScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class OttawaProducer(
        @Autowired private val sportsStatsScraper: SportsStatsScraper,
        @Autowired numberedResultsPageRepository: NumberedResultsPageRepository) : AbstractNumberedResultsPageProducer(numberedResultsPageRepository, LoggerFactory.getLogger(OttawaProducer::class.java), MarathonSources.Ottawa) {

    private val scrapeInfo = PagedScrapeInfo(
            url = "https://www.sportstats.ca/display-results.xhtml?raceid=26006",
            marathonSources = marathonSources,
            marathonYear = 2015,
            tableBodySelector = ".results > tbody",
            columnPositions = MergedAgedGenderColumnPositions(
                    nationality = 4,
                    place = 6,
                    ageGender = 5,
                    finishTime = 13
            ),
            startPage = 0,
            skipRowCount = 0,
            currentPage = 0,
            endPage = 117,
            clipRows = 1,
            clickNextSelector = ".pagination > li:nth-child(13) > a",
            clickPreviousSelector = ".pagination > li:nth-child(2) > a"
    )

    override fun buildYearlyThreads(year: Int, lastPage: Int) {
        if (year == 2015) {
            threads += sportsStatsScraper.scrape(scrapeInfo.copy(startPage = lastPage))
        }
    }
}