package com.stonesoupprogramming.marathonscrape.producers.sites.races

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.PagedScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.SequenceLinks
import com.stonesoupprogramming.marathonscrape.producers.AbstractBaseProducer
import com.stonesoupprogramming.marathonscrape.producers.AbstractNumberedResultsPageProducer
import com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.AbstractNumberedAthSequenceProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.AthLinksMarathonScraper
import com.stonesoupprogramming.marathonscrape.scrapers.sites.RunToPixScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TaipeiAthComponent(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                     @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(TaipeiAthComponent::class.java),
        MarathonSources.Taipei,
        listOf(SequenceLinks(2014, "https://www.athlinks.com/event/34450/results/Event/410756/Course/617603/Results", 107, false),
                SequenceLinks(2015, "https://www.athlinks.com/event/34450/results/Event/512311/Course/669211/Results", 94, false),
                SequenceLinks(2017, "https://www.athlinks.com/event/34450/results/Event/701640/Course/1142522/Results", 120, false)))

@Component
class TaipeiRunToPixComponent(@Autowired private val runToPixScraper: RunToPixScraper,
                             @Autowired numberedResultsPageRepository: NumberedResultsPageRepository) : AbstractNumberedResultsPageProducer(numberedResultsPageRepository, LoggerFactory.getLogger(TaipeiRunToPixComponent::class.java), MarathonSources.Taipei){

    private val scrapeInfo = PagedScrapeInfo(
            url = "https://www.run2pix.com/report/report_w.php?EventCode=20161218&Race=MA&sn=136",
            marathonSources = marathonSources,
            marathonYear = 2016,
            tableBodySelector = "body > table:nth-child(4) > tbody:nth-child(1)",
            skipRowCount = 8,
            clipRows = 5,
            columnPositions = MergedAgedGenderColumnPositions(
                    nationality = -1,
                    finishTime = 7,
                    place = 5,
                    ageGender = 3
            ),
            clickNextSelector = "body > table:nth-child(4) > tbody > tr:nth-child(6) > td > table > tbody > tr > td > table > tbody > tr > td:nth-child(2) > table > tbody > tr > td:nth-child(3) > a",
            secondaryClickNextSelector = "body > table:nth-child(4) > tbody:nth-child(1) > tr:nth-child(6) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(3) > a:nth-child(1)",
            clickPreviousSelector = "body > table:nth-child(4) > tbody:nth-child(1) > tr:nth-child(6) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2) > a:nth-child(1)",
            currentPage = 0,
            startPage = 0,
            endPage = 12
    )

    override fun buildYearlyThreads(year: Int, lastPage: Int) {
        if(year == 2016){
            threads.add(runToPixScraper.scrape(scrapeInfo.copy(startPage = lastPage)))
        }
    }
}

@Component
class TaipeiProducer(@Autowired private val taipeiAthComponent: TaipeiAthComponent,
                     @Autowired private val taipeiRunToPixComponent: TaipeiRunToPixComponent) : AbstractBaseProducer(LoggerFactory.getLogger(TaipeiProducer::class.java), MarathonSources.Taipei){

    override fun buildThreads() {
        threads.addAll(taipeiAthComponent.process())
        threads.addAll(taipeiRunToPixComponent.process())
    }
}