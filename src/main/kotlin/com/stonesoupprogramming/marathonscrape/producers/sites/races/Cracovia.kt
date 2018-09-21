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
import com.stonesoupprogramming.marathonscrape.scrapers.sites.CracoviaScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CracoviaAthComponent(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                           @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(CracoviaAthComponent::class.java),
        MarathonSources.Cracovia,
        listOf(SequenceLinks(2014, "https://www.athlinks.com/event/35465/results/Event/409660/Course/615995/Results", 108, false),
                SequenceLinks(2016, "https://www.athlinks.com/event/35465/results/Event/544957/Course/809981/Results", 112, false),
                SequenceLinks(2017, "https://www.athlinks.com/event/35465/results/Event/641783/Course/997566/Results", 113, false)))

@Component
class CracoviaPagedComponent(@Autowired private val cracoviaScraper: CracoviaScraper,
                             @Autowired numberedResultsPageRepository: NumberedResultsPageRepository) : AbstractNumberedResultsPageProducer(numberedResultsPageRepository, LoggerFactory.getLogger(CracoviaPagedComponent::class.java), MarathonSources.Cracovia) {

    private val scrapeInfo = PagedScrapeInfo(
            url = "https://online.datasport.pl/results1432/index.php",
            marathonSources = marathonSources,
            marathonYear = 2015,
            tableBodySelector = "#table2 > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(28) > tbody:nth-child(1)",
            skipRowCount = 1,
            clipRows = 2,
            columnPositions = MergedAgedGenderColumnPositions(
                    nationality = 2,
                    place = 0,
                    finishTime = 6,
                    ageGender = 5),
            startPage = 0,
            currentPage = 0,
            endPage = 458,
            clickNextSelector = "#table2 > tbody > tr > td:nth-child(1) > table > tbody > tr:nth-child(12) > td:nth-child(3) > button:nth-child(3)",
            clickPreviousSelector = "#table2 > tbody > tr > td:nth-child(1) > table > tbody > tr:nth-child(12) > td:nth-child(1) > button:nth-child(3)"
    )

    override fun buildYearlyThreads(year: Int, lastPage: Int) {
        if(year == scrapeInfo.marathonYear){
            threads.add(cracoviaScraper.scrape(scrapeInfo.copy(startPage = lastPage)))
        }
    }
}

@Component
class CracoviaProducer(@Autowired private val cracoviaAthComponent: CracoviaAthComponent,
                       @Autowired private val cracoviaPagedComponent: CracoviaPagedComponent) : AbstractBaseProducer(LoggerFactory.getLogger(CracoviaProducer::class.java), MarathonSources.Cracovia) {
    override fun buildThreads() {
        threads.addAll(cracoviaAthComponent.process())
        threads.addAll(cracoviaPagedComponent.process())
    }

}