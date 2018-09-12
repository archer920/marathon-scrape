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

//private val categories = listOf(
//        //2014 has been verified
//        CategoryAthLinks(2014, URL_2014, "F 30-34", CategoryAthLinks.selectOption(1), 21),
//        CategoryAthLinks(2014, URL_2014, "F 35-39", CategoryAthLinks.selectOption(2), 21),
//        CategoryAthLinks(2014, URL_2014, "F 40-44", CategoryAthLinks.selectOption(3), 1),
//        CategoryAthLinks(2014, URL_2014, "F 40-49", CategoryAthLinks.selectOption(4), 25),
//        CategoryAthLinks(2014, URL_2014, "F 45-49", CategoryAthLinks.selectOption(5), 25),
//        CategoryAthLinks(2014, URL_2014, "F 50-54", CategoryAthLinks.selectOption(6), 16),
//        CategoryAthLinks(2014, URL_2014, "F 55-59", CategoryAthLinks.selectOption(7), 8),
//        CategoryAthLinks(2014, URL_2014, "F 60-69", CategoryAthLinks.selectOption(8), 3),
//        CategoryAthLinks(2014, URL_2014, "F 65-69", CategoryAthLinks.selectOption(9), 1),
//        CategoryAthLinks(2014, URL_2014, "F 70-79", CategoryAthLinks.selectOption(10), 1),
//        CategoryAthLinks(2014, URL_2014, "F 75-79", CategoryAthLinks.selectOption(11), 1),
//        CategoryAthLinks(2014, URL_2014, "F N/A", CategoryAthLinks.selectOption(12), 20),
//
//        CategoryAthLinks(2014, URL_2014, "M 30-34", CategoryAthLinks.selectOption(14), 52),
//        CategoryAthLinks(2014, URL_2014, "M 35-39", CategoryAthLinks.selectOption(15), 64),
//        CategoryAthLinks(2014, URL_2014, "M 40-44", CategoryAthLinks.selectOption(16), 41),
//        CategoryAthLinks(2014, URL_2014, "M 40-49", CategoryAthLinks.selectOption(17), 44),
//        CategoryAthLinks(2014, URL_2014, "M 45-49", CategoryAthLinks.selectOption(18), 85),
//        CategoryAthLinks(2014, URL_2014, "M 50-54", CategoryAthLinks.selectOption(19), 64),
//        CategoryAthLinks(2014, URL_2014, "M 55-59", CategoryAthLinks.selectOption(20), 33),
//        CategoryAthLinks(2014, URL_2014, "M 60-64", CategoryAthLinks.selectOption(21), 3),
//        CategoryAthLinks(2014, URL_2014, "M 60-69", CategoryAthLinks.selectOption(22), 14),
//        CategoryAthLinks(2014, URL_2014, "M 65-69", CategoryAthLinks.selectOption(23), 7),
//        CategoryAthLinks(2014, URL_2014, "M 70-74", CategoryAthLinks.selectOption(24), 1),
//        CategoryAthLinks(2014, URL_2014, "M 70-79", CategoryAthLinks.selectOption(25), 3),
//        CategoryAthLinks(2014, URL_2014, "M 75-79", CategoryAthLinks.selectOption(26), 1),
//        CategoryAthLinks(2014, URL_2014, "M 80-99", CategoryAthLinks.selectOption(27), 1),
//        CategoryAthLinks(2014, URL_2014, "M N/A", CategoryAthLinks.selectOption(28), 37),
//        CategoryAthLinks(2014, URL_2014, "U N/A", CategoryAthLinks.selectOption(30), 1),
//        CategoryAthLinks(2014, URL_2014, "Unspecified", CategoryAthLinks.selectOption(31), 1),
//
//        CategoryAthLinks(2015, URL_2015, "F 00-00", CategoryAthLinks.selectOption(1), 1),
//        CategoryAthLinks(2015, URL_2015, "F 30-34", CategoryAthLinks.selectOption(2), 27),
//        CategoryAthLinks(2015, URL_2015, "F 35-39", CategoryAthLinks.selectOption(3), 26),
//        CategoryAthLinks(2015, URL_2015, "F 40-44", CategoryAthLinks.selectOption(4), 32),
//        CategoryAthLinks(2015, URL_2015, "F 45-49", CategoryAthLinks.selectOption(5), 31),
//        CategoryAthLinks(2015, URL_2015, "F 50-54", CategoryAthLinks.selectOption(6), 23),
//        CategoryAthLinks(2015, URL_2015, "F 55-59", CategoryAthLinks.selectOption(7), 10),
//        CategoryAthLinks(2015, URL_2015, "F 60-64", CategoryAthLinks.selectOption(8), 4),
//        CategoryAthLinks(2015, URL_2015, "F 65-69", CategoryAthLinks.selectOption(9), 2),
//        CategoryAthLinks(2015, URL_2015, "F 70-74", CategoryAthLinks.selectOption(10), 1),
//        CategoryAthLinks(2015, URL_2015, "F 75-79", CategoryAthLinks.selectOption(11), 1),
//        CategoryAthLinks(2015, URL_2015, "F JR", CategoryAthLinks.selectOption(12), 1),
//
//        CategoryAthLinks(2015, URL_2015, "M 00-00", CategoryAthLinks.selectOption(14), 1),
//        CategoryAthLinks(2015, URL_2015, "M 30-34", CategoryAthLinks.selectOption(15), 66),
//        CategoryAthLinks(2015, URL_2015, "M 35-39", CategoryAthLinks.selectOption(16), 82),
//        CategoryAthLinks(2015, URL_2015, "M 40-44", CategoryAthLinks.selectOption(17), 101),
//        CategoryAthLinks(2015, URL_2015, "M 45-49", CategoryAthLinks.selectOption(18), 105),
//        CategoryAthLinks(2015, URL_2015, "M 50-54", CategoryAthLinks.selectOption(19), 82),
//        CategoryAthLinks(2015, URL_2015, "M 55-59", CategoryAthLinks.selectOption(20), 43),
//        CategoryAthLinks(2015, URL_2015, "M 60-64", CategoryAthLinks.selectOption(21), 21),
//        CategoryAthLinks(2015, URL_2015, "M 65-69", CategoryAthLinks.selectOption(22), 8),
//        CategoryAthLinks(2015, URL_2015, "M 70-74", CategoryAthLinks.selectOption(23), 3),
//        CategoryAthLinks(2015, URL_2015, "M 75-79", CategoryAthLinks.selectOption(24), 2),
//        CategoryAthLinks(2015, URL_2015, "M 80-99", CategoryAthLinks.selectOption(25), 1),
//        CategoryAthLinks(2015, URL_2015, "M JR", CategoryAthLinks.selectOption(26), 2),
//        CategoryAthLinks(2015, URL_2015, "Senior", CategoryAthLinks.selectOption(28), 76),
//
//        CategoryAthLinks(2016, URL_2016, "F 30-34", CategoryAthLinks.selectOption(1), 29),
//        CategoryAthLinks(2016, URL_2016, "F 35-39", CategoryAthLinks.selectOption(2), 30),
//        CategoryAthLinks(2016, URL_2016, "F 40-44", CategoryAthLinks.selectOption(3), 32),
//        CategoryAthLinks(2016, URL_2016, "F 45-49", CategoryAthLinks.selectOption(4), 33),
//        CategoryAthLinks(2016, URL_2016, "F 50-54", CategoryAthLinks.selectOption(5), 23),
//        CategoryAthLinks(2016, URL_2016, "F 55-59", CategoryAthLinks.selectOption(6), 11),
//        CategoryAthLinks(2016, URL_2016, "F 60-64", CategoryAthLinks.selectOption(7), 4),
//        CategoryAthLinks(2016, URL_2016, "F 65-69", CategoryAthLinks.selectOption(8), 2),
//        CategoryAthLinks(2016, URL_2016, "F 70-74", CategoryAthLinks.selectOption(9), 1),
//        CategoryAthLinks(2016, URL_2016, "F 75-79", CategoryAthLinks.selectOption(10), 1),
//        CategoryAthLinks(2016, URL_2016, "F Junior", CategoryAthLinks.selectOption(11), 1),
//        CategoryAthLinks(2016, URL_2016, "F Open", CategoryAthLinks.selectOption(12), 26),
//
//        CategoryAthLinks(2016, URL_2016, "M 30-34", CategoryAthLinks.selectOption(14), 64),
//        CategoryAthLinks(2016, URL_2016, "M 35-39", CategoryAthLinks.selectOption(15), 82),
//        CategoryAthLinks(2016, URL_2016, "M 40-44", CategoryAthLinks.selectOption(16), 91),
//        CategoryAthLinks(2016, URL_2016, "M 45-49", CategoryAthLinks.selectOption(17), 98),
//        CategoryAthLinks(2016, URL_2016, "M 50-54", CategoryAthLinks.selectOption(18), 82),
//        CategoryAthLinks(2016, URL_2016, "M 55-59", CategoryAthLinks.selectOption(19), 44),
//        CategoryAthLinks(2016, URL_2016, "M 60-64", CategoryAthLinks.selectOption(20), 20),
//        CategoryAthLinks(2016, URL_2016, "M 65-69", CategoryAthLinks.selectOption(21), 8),
//        CategoryAthLinks(2016, URL_2016, "M 70-74", CategoryAthLinks.selectOption(22), 3),
//        CategoryAthLinks(2016, URL_2016, "M 75-79", CategoryAthLinks.selectOption(23), 1),
//        CategoryAthLinks(2016, URL_2016, "M 80-99", CategoryAthLinks.selectOption(24), 1),
//        CategoryAthLinks(2016, URL_2016, "M Junior", CategoryAthLinks.selectOption(25), 2),
//        CategoryAthLinks(2016, URL_2016, "M N/A", CategoryAthLinks.selectOption(26), 1),
//        CategoryAthLinks(2016, URL_2016, "M Open", CategoryAthLinks.selectOption(27), 47),
//
//        CategoryAthLinks(2017, URL_2017, "F 00-00", CategoryAthLinks.selectOption(1), 1),
//        CategoryAthLinks(2017, URL_2017, "F 30-34", CategoryAthLinks.selectOption(2), 35),
//        CategoryAthLinks(2017, URL_2017, "F 35-39", CategoryAthLinks.selectOption(3), 36),
//        CategoryAthLinks(2017, URL_2017, "F 40-44", CategoryAthLinks.selectOption(4), 37),
//        CategoryAthLinks(2017, URL_2017, "F 45-49", CategoryAthLinks.selectOption(5), 36),
//        CategoryAthLinks(2017, URL_2017, "F 50-54", CategoryAthLinks.selectOption(6), 28),
//        CategoryAthLinks(2017, URL_2017, "F 55-59", CategoryAthLinks.selectOption(7), 13),
//        CategoryAthLinks(2017, URL_2017, "F 60-64", CategoryAthLinks.selectOption(8), 6),
//        CategoryAthLinks(2017, URL_2017, "F 65-69", CategoryAthLinks.selectOption(9), 2),
//        CategoryAthLinks(2017, URL_2017, "F 70-74", CategoryAthLinks.selectOption(10), 1),
//        CategoryAthLinks(2017, URL_2017, "F 75-99", CategoryAthLinks.selectOption(11), 1),
//        CategoryAthLinks(2017, URL_2017, "F Junior", CategoryAthLinks.selectOption(12), 1),
//        CategoryAthLinks(2017, URL_2017, "F Open", CategoryAthLinks.selectOption(13), 31),
//
//        CategoryAthLinks(2017, URL_2017, "M 00-00", CategoryAthLinks.selectOption(15), 1),
//        CategoryAthLinks(2017, URL_2017, "M 30-34", CategoryAthLinks.selectOption(16), 70),
//        CategoryAthLinks(2017, URL_2017, "M 35-39", CategoryAthLinks.selectOption(17), 84),
//        CategoryAthLinks(2017, URL_2017, "M 40-44", CategoryAthLinks.selectOption(18), 95),
//        CategoryAthLinks(2017, URL_2017, "M 45-49", CategoryAthLinks.selectOption(19), 101),
//        CategoryAthLinks(2017, URL_2017, "M 50-54", CategoryAthLinks.selectOption(20), 85),
//        CategoryAthLinks(2017, URL_2017, "M 55-59", CategoryAthLinks.selectOption(21), 48),
//        CategoryAthLinks(2017, URL_2017, "M 60-64", CategoryAthLinks.selectOption(22), 21),
//        CategoryAthLinks(2017, URL_2017, "M 65-69", CategoryAthLinks.selectOption(23), 10),
//        CategoryAthLinks(2017, URL_2017, "M 70-74", CategoryAthLinks.selectOption(24), 3),
//        CategoryAthLinks(2017, URL_2017, "M 75-79", CategoryAthLinks.selectOption(25), 2),
//        CategoryAthLinks(2017, URL_2017, "M 80-99", CategoryAthLinks.selectOption(26), 1),
//        CategoryAthLinks(2017, URL_2017, "M Junior", CategoryAthLinks.selectOption(27), 2),
//        CategoryAthLinks(2017, URL_2017, "M Open", CategoryAthLinks.selectOption(28), 47)
//)

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