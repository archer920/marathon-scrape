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
import com.stonesoupprogramming.marathonscrape.scrapers.sites.OldMyRaceGrScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class AthensAthComponent(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                         @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(AthensAthComponent::class.java),
        MarathonSources.Athens,
        listOf(SequenceAthLinks(2015, "https://www.athlinks.com/event/34504/results/Event/470893/Course/701290/Results", 238),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34504/results/Event/597971/Course/903287/Results", 276),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34504/results/Event/604206/Course/1119762/Results", 295)))

@Component
class AthensUrlComponent(@Autowired private val oldMyRaceGrScraper: OldMyRaceGrScraper,
                         @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractResultsPageProducer<ResultsPage>(resultsRepository, LoggerFactory.getLogger(AthensUrlComponent::class.java), MarathonSources.Athens) {

    private val links = Array(524) {it -> "https://old.myrace.gr/results2014/results.asp?p=1091&s=0&a=0&e=0&l=1&x=$it"}

    override fun buildThreads() {
        val scrapeInfo = StandardScrapeInfo<MergedAgedGenderColumnPositions, ResultsPage>(
                url = "",
                marathonSources = marathonSources,
                marathonYear = 2014,
                tableBodySelector = ".raceResults > tbody:nth-child(1)",
                skipRowCount = 1,
                columnPositions = MergedAgedGenderColumnPositions(
                        nationality = -1, //It's in the ageGenderColumn
                        finishTime = 3,
                        place = 0,
                        ageGender = 2))
        links.filter { link -> completed.none { cp -> cp.url == link } }.forEach { link ->
            threads.add(oldMyRaceGrScraper.scrape(scrapeInfo.copy(url = link)))
        }
    }
}

@Component
class AthensProducer(@Autowired private val athensAthComponent: AthensAthComponent,
                     @Autowired private val athensUrlComponent: AthensUrlComponent) : AbstractBaseProducer(LoggerFactory.getLogger(AthensProducer::class.java), MarathonSources.Athens){

    override fun buildThreads() {
        threads.addAll(athensAthComponent.process())
        threads.addAll(athensUrlComponent.process())
    }
}

