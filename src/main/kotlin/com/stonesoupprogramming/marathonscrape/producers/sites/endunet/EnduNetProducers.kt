package com.stonesoupprogramming.marathonscrape.producers.sites.endunet

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.SequenceLinks
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.EnduNetScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TurinProducer(@Autowired enduNetScraper: EnduNetScraper,
                    @Autowired numberedResultsPageRepository: NumberedResultsPageRepository) :
        AbstractEnduNetProducer(enduNetScraper, numberedResultsPageRepository, LoggerFactory.getLogger(TurinProducer::class.java), MarathonSources.Turin,
        listOf(SequenceLinks(2014, "https://www.endu.net/en/events/turin-marathon/results", 36, true),
                SequenceLinks(2015, "https://www.endu.net/en/events/turin-marathon/results", 16),
                SequenceLinks(2016, "https://www.endu.net/en/events/turin-marathon/results", 16),
                SequenceLinks(2017, "https://www.endu.net/en/events/turin-marathon/results", 14)))