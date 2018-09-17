package com.stonesoupprogramming.marathonscrape.producers.sites.races

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.sites.MarathonGuideInfo
import com.stonesoupprogramming.marathonscrape.models.sites.SequenceAthLinks
import com.stonesoupprogramming.marathonscrape.producers.AbstractBaseProducer
import com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.AbstractNumberedAthSequenceProducer
import com.stonesoupprogramming.marathonscrape.producers.sites.marathonguide.AbstractMarathonGuideProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.repository.ResultsRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.AthLinksMarathonScraper
import com.stonesoupprogramming.marathonscrape.scrapers.sites.MarathonGuideScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

private val standardColumnPositions = MergedAgedGenderColumnPositions(nationality = 6, finishTime = 5, place = 2, ageGender = 0, backupAge = 4)

@Component
class AirForceMarathonGuideComponent(@Autowired marathonGuideScraper: MarathonGuideScraper,
                       @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(AirForceMarathonGuideComponent::class.java),
        MarathonSources.AirForce,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=37140920", 2014, standardColumnPositions, 2912),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=37150919", 2015, standardColumnPositions.copy(nationality = 5, finishTime = 1), 2156),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=37160917", 2016, standardColumnPositions.copy(nationality = 5, finishTime = 1), 2042)))

@Component
class AirForceAthComponent(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                          @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(AirForceAthComponent::class.java),
        MarathonSources.AirForce,
        listOf(SequenceAthLinks(2017, "https://www.athlinks.com/event/20370/results/Event/602585/Course/911911/Results", 30)))

@Component
class AirForceProducer(@Autowired private val airForceMarathonGuideComponent: AirForceMarathonGuideComponent,
                       @Autowired private val airForceAthComponent: AirForceAthComponent) : AbstractBaseProducer(LoggerFactory.getLogger(AirForceProducer::class.java), MarathonSources.AirForce){

    override fun buildThreads() {
        //threads.addAll(airForceMarathonGuideComponent.process())
        threads.addAll(airForceAthComponent.process())
    }
}