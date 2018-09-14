package com.stonesoupprogramming.marathonscrape.producers.sites.races

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.AgeGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.StandardScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.sites.SequenceAthLinks
import com.stonesoupprogramming.marathonscrape.producers.AbstractResultsPageProducer
import com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.AbstractNumberedAthSequenceProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.repository.ResultsRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.AthLinksMarathonScraper
import com.stonesoupprogramming.marathonscrape.scrapers.sites.DataSportScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class JungfrauAthComponent(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                           @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(JungfrauAthComponent::class.java),
        MarathonSources.Jungfrau,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/34875/results/Event/357512/Course/524308/Results", 80),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/34875/results/Event/478357/Course/711990/Results", 82),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34875/results/Event/584543/Course/877498/Results", 73)))

@Component
class JungfrauProducer(@Autowired resultsRepository: ResultsRepository<ResultsPage>,
                       @Autowired private val jungfrauAthComponent: JungfrauAthComponent,
                       @Autowired private val dataSportScraper: DataSportScraper) : AbstractResultsPageProducer<ResultsPage>(resultsRepository, LoggerFactory.getLogger(JungfrauProducer::class.java), MarathonSources.Jungfrau) {

    private val mensLinks = Array(8) { it -> "https://services.datasport.com/2017/lauf/jungfrau/rang040-00${it + 1}.htm" }
    private val womensLinks = Array(3) { it -> "https://services.datasport.com/2017/lauf/jungfrau/rang050-00${it + 1}.htm" }

    override fun buildThreads() {
        val scrapeInfo = StandardScrapeInfo<AgeGenderColumnPositions, ResultsPage>(
                url = "",
                marathonSources = marathonSources,
                marathonYear = 2017,
                tableBodySelector = "",
                skipRowCount = -1,
                //DataSportScraper does custom DOM parsing so column positions are not necessary
                columnPositions = AgeGenderColumnPositions(nationality = -1, finishTime = -1, age = -1, gender = -1, place = -1))

        mensLinks.filter { link -> completed.none { cp -> cp.url == link } }.forEach { link ->
            threads.add(dataSportScraper.scrape(scrapeInfo.copy(url = link)))
        }
        womensLinks.filter { link -> completed.none { cp -> cp.url == link } }.forEach { link ->
            threads.add(dataSportScraper.scrape(scrapeInfo.copy(url = link)))
        }
        threads.addAll(jungfrauAthComponent.process())
    }
}