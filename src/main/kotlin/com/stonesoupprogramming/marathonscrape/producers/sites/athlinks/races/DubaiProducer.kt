package com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.races

import com.stonesoupprogramming.marathonscrape.enums.Gender
import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.AgeGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.StandardScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.SequenceLinks
import com.stonesoupprogramming.marathonscrape.producers.AbstractBaseProducer
import com.stonesoupprogramming.marathonscrape.producers.AbstractResultsPageProducer
import com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.AbstractNumberedAthSequenceProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.repository.ResultsRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.AthLinksMarathonScraper
import com.stonesoupprogramming.marathonscrape.scrapers.sites.MikatimingDeScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class DubaiAthComponent(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                        @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(DubaiAthComponent::class.java),
        MarathonSources.Dubai,
        listOf(SequenceLinks(2014, "https://www.athlinks.com/event/34575/results/Event/325046/Course/470682/Results", 44, false),
                SequenceLinks(2015, "https://www.athlinks.com/event/34575/results/Event/401502/Course/602453/Results", 40, false),
                SequenceLinks(2016, "https://www.athlinks.com/event/34575/results/Event/513994/Course/764191/Results", 41, false)))

@Component
class DubaiMiktaimingComponent(@Autowired private val mikatimingDeScraper: MikatimingDeScraper,
                               @Autowired resultsPageRepository: ResultsRepository<ResultsPage>)
    : AbstractResultsPageProducer<ResultsPage>(resultsPageRepository, LoggerFactory.getLogger(DubaiMiktaimingComponent::class.java), MarathonSources.Dubai) {

    private val mens = Array(15) { it -> "http://dubai.mikatiming.de/2017/?page=$it&event=M&num_results=100&pid=list&search[age_class]=%25&search[sex]=M" }
    private val womens = Array(5) { it -> "http://dubai.mikatiming.de/2017/?page=$it&event=M&num_results=100&pid=list&search%5Bage_class%5D=%25&search%5Bsex%5D=W" }

    override fun buildThreads() {
        val scrapeInfo = StandardScrapeInfo<AgeGenderColumnPositions, ResultsPage>(
                url = "",
                marathonSources = marathonSources,
                marathonYear = 2017,
                tableBodySelector = ".list-table > tbody:nth-child(2)",
                columnPositions = AgeGenderColumnPositions(
                        nationality = 3,
                        finishTime = 7,
                        place = 0,
                        gender = -1,
                        age = 6),
                skipRowCount = 0)
        mens.filter { link -> completed.none { cp -> cp.url == link } }.forEach { link ->
            threads.add(mikatimingDeScraper.scrape(scrapeInfo.copy(url = link, gender = Gender.MALE)))
        }
        womens.filter { link -> completed.none { cp -> cp.url == link } }.forEach { link ->
            threads.add(mikatimingDeScraper.scrape(scrapeInfo.copy(url = link, gender = Gender.FEMALE)))
        }
    }
}

@Component
class DubaiProducer(@Autowired private val dubaiAthComponent: DubaiAthComponent,
                    @Autowired private val dubaiMiktaimingComponent: DubaiMiktaimingComponent)
    : AbstractBaseProducer(LoggerFactory.getLogger(DubaiProducer::class.java), MarathonSources.Dubai) {

    override fun buildThreads() {
        threads.addAll(dubaiAthComponent.process())
        threads.addAll(dubaiMiktaimingComponent.process())
    }
}