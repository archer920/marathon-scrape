package com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.races

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.sites.CategoryAthLinks
import com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.AbstractAthCategoryProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.AthLinksMarathonScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

private const val URL_2014 = "https://www.athlinks.com/event/34514/results/Event/343336/Course/568563/Results"
private const val URL_2015 = "https://www.athlinks.com/event/34514/results/Event/404856/Course/608010/Results"
private const val URL_2016 = "https://www.athlinks.com/event/34514/results/Event/546690/Course/822657/Results"
private const val URL_2017 = "https://www.athlinks.com/event/34514/results/Event/644023/Course/1016123/Results"

private val categories = listOf(
        CategoryAthLinks(2014, URL_2014, "Female", CategoryAthLinks.selectOption(8), 57),
        CategoryAthLinks(2014, URL_2014, "Male", CategoryAthLinks.selectOption(16), 117),
        CategoryAthLinks(2014, URL_2014, "U 50-54", CategoryAthLinks.selectOption(17), 1),
        CategoryAthLinks(2014, URL_2014, "U N/A", CategoryAthLinks.selectOption(18), 1),
        CategoryAthLinks(2014, URL_2014, "Unspecified", CategoryAthLinks.selectOption(19), 1),

        CategoryAthLinks(2015, URL_2015, "Female", CategoryAthLinks.selectOption(9), 48),
        CategoryAthLinks(2015, URL_2015, "Male", CategoryAthLinks.selectOption(18), 96),
        CategoryAthLinks(2015, URL_2015, "Senior", CategoryAthLinks.selectOption(19), 59),


        CategoryAthLinks(2016, URL_2016, "Female", CategoryAthLinks.selectOption(10), 47),
        CategoryAthLinks(2016, URL_2016, "Male", CategoryAthLinks.selectOption(20), 86),


        CategoryAthLinks(2017, URL_2017, "Female", CategoryAthLinks.selectOption(10), 42),
        CategoryAthLinks(2017, URL_2017, "Male", CategoryAthLinks.selectOption(20), 81)
)

@Component
class EdinburgProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                     @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractAthCategoryProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(EdinburgProducer::class.java), MarathonSources.Edinburg, categories)