package com.stonesoupprogramming.marathonscrape.producers.sites.races

import com.stonesoupprogramming.marathonscrape.enums.Gender
import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.SequenceLinks
import com.stonesoupprogramming.marathonscrape.models.sites.TdsScrapeInfo
import com.stonesoupprogramming.marathonscrape.producers.AbstractBaseProducer
import com.stonesoupprogramming.marathonscrape.producers.sites.endunet.AbstractEnduNetProducer
import com.stonesoupprogramming.marathonscrape.producers.sites.tdslive.AbstractTdsLiveProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.EnduNetScraper
import com.stonesoupprogramming.marathonscrape.scrapers.sites.TdsLiveScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TrevisoTdsLiveComponent(@Autowired tdsLiveScraper: TdsLiveScraper,
                   @Autowired numberedResultsPageRepository: NumberedResultsPageRepository) : AbstractTdsLiveProducer(tdsLiveScraper, numberedResultsPageRepository, LoggerFactory.getLogger(TrevisoTdsLiveComponent::class.java), MarathonSources.Treviso,
        listOf(
                TdsScrapeInfo(Gender.UNASSIGNED, null, SequenceLinks(2014, "https://www.tds-live.com/ns/index.jsp?serviziol=&pageType=1&id=5771&servizio=000", 184)),
                TdsScrapeInfo(Gender.UNASSIGNED, null, SequenceLinks(2015, "https://www.tds-live.com/ns/index.jsp?serviziol=&pageType=1&id=6489&servizio=000", 156))))

@Component
class TrevisoEdhComponent(@Autowired enduNetScraper: EnduNetScraper,
                    @Autowired numberedResultsPageRepository: NumberedResultsPageRepository) :
        AbstractEnduNetProducer(enduNetScraper, numberedResultsPageRepository, LoggerFactory.getLogger(TrevisoEdhComponent::class.java), MarathonSources.Treviso,
                listOf(
                        //SequenceLinks(2016, "https://www.endu.net/en/events/treviso-marathon/results", 12)))
                        SequenceLinks(2017, "https://www.endu.net/en/events/treviso-marathon/results", 9)))

@Component
class TrevisoProducer(@Autowired private val trevisoEdhComponent: TrevisoEdhComponent,
                      @Autowired private val trevisoTdsLiveComponent: TrevisoTdsLiveComponent) : AbstractBaseProducer(LoggerFactory.getLogger(TrevisoProducer::class.java), MarathonSources.Treviso){
    override fun buildThreads() {
        threads.addAll(trevisoEdhComponent.process())
        //threads.addAll(trevisoTdsLiveComponent.process())
    }

}