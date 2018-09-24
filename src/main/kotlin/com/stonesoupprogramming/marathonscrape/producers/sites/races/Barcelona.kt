package com.stonesoupprogramming.marathonscrape.producers.sites.races

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.extension.UNAVAILABLE
import com.stonesoupprogramming.marathonscrape.models.AgeGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.StandardScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.sites.SequenceAthLinks
import com.stonesoupprogramming.marathonscrape.producers.AbstractResultsPageProducer
import com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.AbstractNumberedAthSequenceProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.repository.ResultsRepository
import com.stonesoupprogramming.marathonscrape.scrapers.StandardWebScraperAgeGender
import com.stonesoupprogramming.marathonscrape.scrapers.sites.AthLinksMarathonScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.function.BiFunction

@Component
class BarcelonaAthComponent(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                               @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(BarcelonaAthComponent::class.java),
        MarathonSources.Barcelona,
        listOf(SequenceAthLinks(2015, "https://www.athlinks.com/event/34535/results/Event/430781/Course/641229/Results", 308),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34535/results/Event/527324/Course/784158/Results", 331),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34535/results/Event/623704/Course/901798/Results", 324)))

@Component
class BarcelonaProducer(@Autowired pagedResultsRepository: ResultsRepository<ResultsPage>,
                        @Autowired private val barcelonaAthComponent: BarcelonaAthComponent,
                        @Autowired private val standardWebScraperAgeGender: StandardWebScraperAgeGender) : AbstractResultsPageProducer<ResultsPage>(pagedResultsRepository, LoggerFactory.getLogger(BarcelonaProducer::class.java), MarathonSources.Barcelona) {

    private val urls = Array(3) {it -> "https://www.runbritainrankings.com/results/results.aspx?meetingid=94897&pagenum=${it + 1}" }
    private val scrapeInfo = StandardScrapeInfo<AgeGenderColumnPositions, ResultsPage>(
            url = "",
            marathonSources = marathonSources,
            marathonYear = 2014,
            tableBodySelector = "#cphBody_gvP > tbody:nth-child(1)",
            skipRowCount = 3,
            columnPositions = AgeGenderColumnPositions(
                    nationality = 1,
                    nationalityFunction = BiFunction { _, _ -> UNAVAILABLE },
                    place = 1,
                    finishTime = 2,
                    age = 9,
                    gender = 10
            )
    )

    override fun buildThreads() {
        //threads.addAll(barcelonaAthComponent.process())
        urls.filter { url -> completed.none { cp -> cp.url == url } }.forEach {it ->
            threads.add(standardWebScraperAgeGender.scrape(scrapeInfo.copy(url = it)))
        }
    }
}