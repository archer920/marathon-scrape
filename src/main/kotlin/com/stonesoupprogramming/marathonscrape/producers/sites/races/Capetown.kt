package com.stonesoupprogramming.marathonscrape.producers.sites.races

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.StandardScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.sites.SequenceAthLinks
import com.stonesoupprogramming.marathonscrape.producers.AbstractBaseProducer
import com.stonesoupprogramming.marathonscrape.producers.AbstractResultsPageProducer
import com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.AbstractNumberedAthSequenceProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.repository.ResultsRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.AthLinksMarathonScraper
import com.stonesoupprogramming.marathonscrape.scrapers.sites.MultisportAustraliaScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CapetownAthComponent(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                           @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(CapetownAthComponent::class.java),
        MarathonSources.Capetown,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/35337/results/Event/358090/Course/472610/Results", 68),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/35337/results/Event/481613/Course/716466/Results", 86),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/35337/results/Event/591130/Course/889079/Results", 124)))

//TODO: FIXME
@Component
class CapetownUrlComponent(
        @Autowired private val multisportAustraliaScraper: MultisportAustraliaScraper,
        @Autowired pagedResultsRepository: ResultsRepository<ResultsPage>)
    : AbstractResultsPageProducer<ResultsPage>(pagedResultsRepository, LoggerFactory.getLogger(CapetownUrlComponent::class.java), MarathonSources.Capetown) {

    //NOTE: 227 is the last page with results, 440 is the last page for 2017
    private val urls = Array(227) { i -> "https://www.multisportaustralia.com.au/races/14730/events/1?page=$i" }.toList()

    override fun buildThreads() {
        val scrapeInfo = StandardScrapeInfo<MergedAgedGenderColumnPositions, ResultsPage>(
                url = "",
                marathonSources = marathonSources,
                marathonYear = 2017,
                tableBodySelector = ".table > tbody:nth-child(2)",
                skipRowCount = 0,
                columnPositions = MergedAgedGenderColumnPositions(nationality = -1, finishTime = 4, place = 0, ageGender = 6),
                category = null,
                gender = null)
        urls.filter { completed.none { cp -> cp.url == it } }.forEach { url ->
            //multisportAustraliaScraper.scrape(scrapeInfo.copy(url = url))
        }
    }
}

@Component
class CapetownProducer(@Autowired private val capetownAthComponent: CapetownAthComponent,
                       @Autowired private val capetownUrlComponent: CapetownUrlComponent) : AbstractBaseProducer(LoggerFactory.getLogger(CapetownProducer::class.java), MarathonSources.Capetown) {

    override fun buildThreads() {
        threads.addAll(capetownAthComponent.process())
        threads.addAll(capetownUrlComponent.process())
    }
}