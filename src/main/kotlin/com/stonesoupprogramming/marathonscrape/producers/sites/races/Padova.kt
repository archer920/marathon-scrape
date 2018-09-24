package com.stonesoupprogramming.marathonscrape.producers.sites.races

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.SequenceLinks
import com.stonesoupprogramming.marathonscrape.producers.AbstractBaseProducer
import com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.AbstractNumberedAthSequenceProducer
import com.stonesoupprogramming.marathonscrape.producers.sites.endunet.AbstractEnduNetProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.AthLinksMarathonScraper
import com.stonesoupprogramming.marathonscrape.scrapers.sites.EnduNetPreWebScrapeEvent
import com.stonesoupprogramming.marathonscrape.scrapers.sites.PadovaScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PadovaAthComponent(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                         @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(PadovaAthComponent::class.java),
        MarathonSources.Padova,
        listOf(SequenceLinks(2014, "https://www.athlinks.com/event/93380/results/Event/607573/Course/921589/Results", 29),
                SequenceLinks(2015, "https://www.athlinks.com/event/93380/results/Event/538797/Course/921526/Results", 28)))

@Component
class PadovaEnduComponent(@Autowired private val pradovaScraper: PadovaScraper,
                          @Autowired numberedResultsPageRepository: NumberedResultsPageRepository) :
        AbstractEnduNetProducer(pradovaScraper, numberedResultsPageRepository, LoggerFactory.getLogger(PadovaEnduComponent::class.java), MarathonSources.Padova,
                listOf(SequenceLinks(2016, "https://www.endu.net/en/events/padovamarathon/results", 18),
                        SequenceLinks(2017, "https://www.endu.net/en/events/padovamarathon/results", 16, true)),
                mapOf(2016 to EnduNetPreWebScrapeEvent(false, mapOf(2016 to "tr.ng-scope:nth-child(3)"), "#menu", resultsView = "div.intero:nth-child(20) > div:nth-child(1) > div:nth-child(11) > div:nth-child(2) > a:nth-child(1) > div:nth-child(1)"),
                        2017 to EnduNetPreWebScrapeEvent(false, mapOf(2017 to "tr.ng-scope:nth-child(2)"), "#menu", resultsView = "div.intero:nth-child(20) > div:nth-child(1) > div:nth-child(11) > div:nth-child(2) > a:nth-child(1) > div:nth-child(1)")))

@Component
class PadovaProducer(@Autowired private val padovaAthComponent: PadovaAthComponent,
                     @Autowired private val padovaEnduComponent: PadovaEnduComponent) : AbstractBaseProducer(LoggerFactory.getLogger(PadovaProducer::class.java), MarathonSources.Padova) {
    override fun buildThreads() {
        threads.addAll(padovaAthComponent.process())
        threads.addAll(padovaEnduComponent.process())
    }
}