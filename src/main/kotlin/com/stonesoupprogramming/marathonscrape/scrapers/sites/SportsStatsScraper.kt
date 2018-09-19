package com.stonesoupprogramming.marathonscrape.scrapers.sites

import com.stonesoupprogramming.marathonscrape.models.AgeGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.NumberedResultsPage
import com.stonesoupprogramming.marathonscrape.scrapers.AbstractPagedResultsScraper
import com.stonesoupprogramming.marathonscrape.scrapers.DriverFactory
import com.stonesoupprogramming.marathonscrape.scrapers.JsDriver
import com.stonesoupprogramming.marathonscrape.service.MarkCompleteService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SportsStatsScraper(@Autowired driverFactory: DriverFactory,
                         @Autowired jsDriver: JsDriver,
                         @Autowired markedCompleteService: MarkCompleteService<AgeGenderColumnPositions, NumberedResultsPage>,
                         @Autowired usStateCodes: List<String>) : AbstractPagedResultsScraper<AgeGenderColumnPositions>(driverFactory, jsDriver, markedCompleteService, NumberedResultsPage::class.java, LoggerFactory.getLogger(SportsStatsScraper::class.java), )