package com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.races

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.sites.CategoryAthLinks
import com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.AbstractNumberedAthCategoryProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.AthLinksMarathonScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

private const val URL_2014 = "https://www.athlinks.com/event/34448/results/Event/358856/Course/523662/Results"
private const val URL_2015 = "https://www.athlinks.com/event/34448/results/Event/406759/Course/609347/Results"
private const val URL_2016 = "https://www.athlinks.com/event/34448/results/Event/488569/Course/726394/Results"
private const val URL_2017 = "https://www.athlinks.com/event/34448/results/Event/602229/Course/911221/Results"

private val categories = listOf(
        CategoryAthLinks(2014, URL_2014, "Female", CategoryAthLinks.selectOption(13), 136),
        CategoryAthLinks(2014, URL_2014, "Male", CategoryAthLinks.selectOption(29), 444),
        CategoryAthLinks(2014, URL_2014, "Unspecified", CategoryAthLinks.selectOption(31), 1),

        CategoryAthLinks(2015, URL_2015, "Female", CategoryAthLinks.selectOption(13), 179),
        CategoryAthLinks(2015, URL_2015, "Male", CategoryAthLinks.selectOption(27), 559),
        CategoryAthLinks(2015, URL_2015, "Senior", CategoryAthLinks.selectOption(28), 76),

        CategoryAthLinks(2016, URL_2016, "Female", CategoryAthLinks.selectOption(13), 186),
        CategoryAthLinks(2016, URL_2016, "Male", CategoryAthLinks.selectOption(28), 537),

        CategoryAthLinks(2017, URL_2017, "Female", CategoryAthLinks.selectOption(14), 221),
        CategoryAthLinks(2017, URL_2017, "Male", CategoryAthLinks.selectOption(29), 563))

@Component
class BerlinProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                             @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthCategoryProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(BerlinProducer::class.java), MarathonSources.Berlin, categories)