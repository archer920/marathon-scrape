package com.stonesoupprogramming.marathonscrape.producers.sites.races

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
class DresdenAthComponent(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                          @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(DresdenAthComponent::class.java),
        MarathonSources.Dresden,
        listOf(SequenceLinks(2014, "https://www.athlinks.com/event/34752/results/Event/360266/Course/529172/Results", 29, false),
                SequenceLinks(2016, "https://www.athlinks.com/event/34752/results/Event/527370/Course/784221/Results", 24, false)))

@Component
class DredenMikatimingComponent(@Autowired private val mikatimingDeScraper: MikatimingDeScraper,
                                @Autowired pagedResultsRepository: ResultsRepository<ResultsPage>) :
        AbstractResultsPageProducer<ResultsPage>(pagedResultsRepository, LoggerFactory.getLogger(DredenMikatimingComponent::class.java), MarathonSources.Dresden) {

    private val mens2015 = Array(10) { it -> "http://dresden.r.mikatiming.de/2015/?page=${it + 1}&event=M&num_results=100&pid=list&search%5Bage_class%5D=%25&search%5Bsex%5D=M" }
    private val womens2015 = Array(3) { it -> "http://dresden.r.mikatiming.de/2015/?page=${it + 1}&event=M&num_results=100&pid=list&search%5Bage_class%5D=%25&search%5Bsex%5D=W" }

    private val mens2017 = Array(9) { it -> "http://dresden.r.mikatiming.de/2017/?page=${it + 1}&event=M&num_results=100&pid=list&search%5Bage_class%5D=%25&search%5Bsex%5D=M" }
    private val womens2017 = Array(2) { it -> "http://dresden.r.mikatiming.de/2017/?page=${it + 1}&event=M&num_results=100&pid=list&search%5Bage_class%5D=%25&search%5Bsex%5D=W" }

    override fun buildThreads() {
        val scrapeInfo = StandardScrapeInfo<AgeGenderColumnPositions, ResultsPage>(
                url = "",
                marathonSources = marathonSources,
                marathonYear = 0,
                tableBodySelector = ".list-table > tbody:nth-child(2)",
                skipRowCount = 0,
                columnPositions = AgeGenderColumnPositions(
                        nationality = 3,
                        finishTime = 7,
                        place = 0,
                        age = 4,
                        gender = -1
                )
        )

        mens2015.filter { url -> completed.none { cp -> cp.url == url } }.forEach { url ->
            threads.add(mikatimingDeScraper.scrape(scrapeInfo.copy(url = url, marathonYear = 2015, gender = Gender.MALE)))
        }
        womens2015.filter { url -> completed.none { cp -> cp.url == url } }.forEach { url ->
            threads.add(mikatimingDeScraper.scrape(scrapeInfo.copy(url = url, marathonYear = 2015, gender = Gender.FEMALE)))
        }

        mens2017.filter { url -> completed.none { cp -> cp.url == url } }.forEach { url ->
            threads.add(mikatimingDeScraper.scrape(scrapeInfo.copy(url = url, marathonYear = 2017, gender = Gender.MALE)))
        }
        womens2017.filter { url -> completed.none { cp -> cp.url == url } }.forEach { url ->
            threads.add(mikatimingDeScraper.scrape(scrapeInfo.copy(url = url, marathonYear = 2017, gender = Gender.FEMALE)))
        }
    }
}

@Component
class DresdenProducer(@Autowired private val dresdenAthComponent: DresdenAthComponent,
                      @Autowired private val dredenMikatimingComponent: DredenMikatimingComponent) : AbstractBaseProducer(LoggerFactory.getLogger(DresdenProducer::class.java), MarathonSources.Dresden) {

    override fun buildThreads() {
        threads.addAll(dredenMikatimingComponent.process())
        threads.addAll(dresdenAthComponent.process())
    }
}

