package com.stonesoupprogramming.marathonscrape.producers.sites.tdslive

import com.stonesoupprogramming.marathonscrape.enums.Gender
import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.SequenceLinks
import com.stonesoupprogramming.marathonscrape.models.sites.TdsScrapeInfo
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.TdsLivePreWebScrapeEvent
import com.stonesoupprogramming.marathonscrape.scrapers.sites.TdsLiveScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RomeProducer(@Autowired tdsLiveScraper: TdsLiveScraper,
                   @Autowired numberedResultsPageRepository: NumberedResultsPageRepository) : AbstractTdsLiveProducer(tdsLiveScraper, numberedResultsPageRepository, LoggerFactory.getLogger(RomeProducer::class.java), MarathonSources.Rome,
        listOf(
                TdsScrapeInfo(Gender.MALE, TdsLivePreWebScrapeEvent(Gender.MALE), SequenceLinks(2014, "https://www.tds-live.com/ns/index.jsp?login=&password=&is_domenica=0&nextRaceId=&dpbib=&dpcat=&dpsex=&serviziol=&pageType=1&id=5653&servizio=000&locale=1040", 1202)),
                TdsScrapeInfo(Gender.FEMALE, TdsLivePreWebScrapeEvent(Gender.FEMALE), SequenceLinks(2014, "https://www.tds-live.com/ns/index.jsp?login=&password=&is_domenica=0&nextRaceId=&dpbib=&dpcat=&dpsex=&serviziol=&pageType=1&id=5653&servizio=000&locale=1040", 287)),

                //2015
                TdsScrapeInfo(Gender.UNASSIGNED, null, SequenceLinks(2015, "https://www.tds-live.com/ns/index.jsp?serviziol=&pageType=1&id=6504&servizio=000", 1149), ageGender = 7, finishTime = 8),

                //2016
                TdsScrapeInfo(Gender.MALE, TdsLivePreWebScrapeEvent(Gender.MALE), SequenceLinks(2016, "https://www.tds-live.com/ns/index.jsp?login=&password=&is_domenica=0&nextRaceId=&dpbib=&dpcat=&dpsex=&serviziol=&pageType=1&id=7410&servizio=000&locale=1040", 1111)),
                TdsScrapeInfo(Gender.FEMALE, TdsLivePreWebScrapeEvent(Gender.FEMALE), SequenceLinks(2016, "https://www.tds-live.com/ns/index.jsp?login=&password=&is_domenica=0&nextRaceId=&dpbib=&dpcat=&dpsex=&serviziol=&pageType=1&id=7410&servizio=000&locale=1040", 278)),

                //2017
                TdsScrapeInfo(Gender.UNASSIGNED, null, SequenceLinks(2017, "https://www.tds-live.com/ns/index.jsp?login=&password=&is_domenica=0&nextRaceId=&dpbib=&dpcat=&dpsex=&serviziol=&pageType=1&id=8252&servizio=000&locale=1040", 1332), ageGender = 7, finishTime = 8)))