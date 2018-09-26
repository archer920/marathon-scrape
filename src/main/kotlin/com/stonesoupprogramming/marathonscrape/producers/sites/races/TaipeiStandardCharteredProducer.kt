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
class TaipeiStandardCharteredAthComponent(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                                          @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(TaipeiStandardCharteredAthComponent::class.java),
        MarathonSources.TaipeiStandardChartered,
        listOf(SequenceLinks(2014, "https://www.athlinks.com/event/166309/results/Event/477860/Course/711268/Results", 37, false),
                SequenceLinks(2015, "https://www.athlinks.com/event/166309/results/Event/687986/Course/1112604/Results", 44, false),
                SequenceLinks(2016, "https://www.athlinks.com/event/166309/results/Event/528734/Course/785986/Results", 40, false)))

@Component
class TaipeiStandardCharteredRunToPixComponent(@Autowired private val runToPixScraper: RunToPixScraper,
                                               @Autowired numberedResultsPageRepository: NumberedResultsPageRepository) : AbstractNumberedResultsPageProducer(numberedResultsPageRepository, LoggerFactory.getLogger(TaipeiRunToPixComponent::class.java), MarathonSources.TaipeiStandardChartered) {

    private val scrapeInfo = PagedScrapeInfo(
            url = "https://www.run2pix.com/report/report_w.php?EventCode=20170212&Race=MA&sn=14",
            marathonSources = marathonSources,
            marathonYear = 2017,
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
            endPage = 7
    )

    override fun buildYearlyThreads(year: Int, lastPage: Int) {
        if (year == 2017) {
            threads.add(runToPixScraper.scrape(scrapeInfo.copy(startPage = lastPage)))
        }
    }
}

@Component
class TaipeiStandardCharteredProducer(@Autowired private val taipeiStandardCharteredAthComponent: TaipeiStandardCharteredAthComponent,
                                      @Autowired private val taipeiStandardCharteredRunToPixComponent: TaipeiStandardCharteredRunToPixComponent) : AbstractBaseProducer(LoggerFactory.getLogger(TaipeiStandardCharteredProducer::class.java), MarathonSources.TaipeiStandardChartered) {

    override fun buildThreads() {
        //threads.addAll(taipeiStandardCharteredAthComponent.process())
        threads.addAll(taipeiStandardCharteredRunToPixComponent.process())
    }
}