package com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.races

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumPositions
import com.stonesoupprogramming.marathonscrape.models.PagedScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.sites.CategoryAthLinks
import com.stonesoupprogramming.marathonscrape.producers.AbstractBaseProducer
import com.stonesoupprogramming.marathonscrape.producers.AbstractNumberedResultsPageProducer
import com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.AbstractNumberedAthCategoryProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.scrapers.philadelphia.PhiladelphiaPreWebScrapeEvent
import com.stonesoupprogramming.marathonscrape.scrapers.sites.AthLinksMarathonScraper
import com.stonesoupprogramming.marathonscrape.scrapers.sites.XacteMarathonScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

private const val URL_2014 = "https://www.athlinks.com/event/156909/results/Event/406017/Course/610531/Results"
private const val URL_2015 = "https://www.athlinks.com/event/156909/results/Event/495263/Course/739431/Results"
private val categories = listOf(
       CategoryAthLinks(2014, URL_2014, "F 0-0", "#option-1 > div > div > div", 1),
        CategoryAthLinks(2014, URL_2014, "F 12-15", "#option-2 > div > div > div", 1),
        CategoryAthLinks(2014, URL_2014, "F 16-19", "#option-3 > div > div > div", 2),
        CategoryAthLinks(2014, URL_2014, "F 20-24", "#option-4 > div > div > div", 11),
        CategoryAthLinks(2014, URL_2014, "F 25-29", "#option-5 > div > div > div", 20),
        CategoryAthLinks(2014, URL_2014, "F 30-34", "#option-6 > div > div > div", 16),
        CategoryAthLinks(2014, URL_2014, "F 35-39", "#option-7 > div > div > div", 15),
        CategoryAthLinks(2014, URL_2014, "F 40-44", "#option-8 > div > div > div", 13),
        CategoryAthLinks(2014, URL_2014, "F 45-49", "#option-9 > div > div > div", 9),
        CategoryAthLinks(2014, URL_2014, "F 50-54", "#option-10 > div > div > div", 6),
        CategoryAthLinks(2014, URL_2014, "F 55-59", "#option-11 > div > div > div", 3),
        CategoryAthLinks(2014, URL_2014, "F 60-64", "#option-12 > div > div > div", 1),
        CategoryAthLinks(2014, URL_2014, "F 65-69", "#option-13 > div > div > div", 1),
        CategoryAthLinks(2014, URL_2014, "F 70-99", "#option-14 > div > div > div", 1),
        CategoryAthLinks(2014, URL_2014, "M 12-15", "#option-16 > div > div > div", 1),
        CategoryAthLinks(2014, URL_2014, "M 16-19", "#option-17 > div > div > div", 2),
        CategoryAthLinks(2014, URL_2014, "M 20-24", "#option-18 > div > div > div", 9),
        CategoryAthLinks(2014, URL_2014, "M 25-29", "#option-19 > div > div > div", 18),
        CategoryAthLinks(2014, URL_2014, "M 30-34", "#option-20 > div > div > div", 17),
        CategoryAthLinks(2014, URL_2014, "M 35-39", "#option-21 > div > div > div", 18),
        CategoryAthLinks(2014, URL_2014, "M 40-44", "#option-22 > div > div > div", 18),
        CategoryAthLinks(2014, URL_2014, "M 45-49", "#option-23 > div > div > div", 15),
        CategoryAthLinks(2014, URL_2014, "M 50-54", "#option-24 > div > div > div", 11),
        CategoryAthLinks(2014, URL_2014, "M 55-59", "#option-25 > div > div > div", 7),
        CategoryAthLinks(2014, URL_2014, "M 60-64", "#option-26 > div > div > div", 3),
        CategoryAthLinks(2014, URL_2014, "M 65-69", "#option-27 > div > div > div", 1),
        CategoryAthLinks(2014, URL_2014, "M 70-99", "#option-28 > div > div > div", 1),

        CategoryAthLinks(2015, URL_2015, "F 0-0", "#option-1 > div > div > div", 1),
        CategoryAthLinks(2015, URL_2015, "F 12-15", "#option-2 > div > div > div", 1),
        CategoryAthLinks(2015, URL_2015, "F 16-19", "#option-3 > div > div > div", 2),
        CategoryAthLinks(2015, URL_2015, "F 20-24", "#option-4 > div > div > div", 11),
        CategoryAthLinks(2015, URL_2015, "F 25-29", "#option-5 > div > div > div", 18),
        CategoryAthLinks(2015, URL_2015, "F 30-34", "#option-6 > div > div > div", 13),
        CategoryAthLinks(2015, URL_2015, "F 35-39", "#option-7 > div > div > div", 13),
        CategoryAthLinks(2015, URL_2015, "F 40-44", "#option-8 > div > div > div", 11),
        CategoryAthLinks(2015, URL_2015, "F 45-49", "#option-9 > div > div > div", 8),
        CategoryAthLinks(2015, URL_2015, "F 50-54", "#option-10 > div > div > div", 5),
        CategoryAthLinks(2015, URL_2015, "F 55-59", "#option-11 > div > div > div", 2),
        CategoryAthLinks(2015, URL_2015, "F 60-64", "#option-12 > div > div > div", 1),
        CategoryAthLinks(2015, URL_2015, "F 65-69", "#option-13 > div > div > div", 1),
        CategoryAthLinks(2015, URL_2015, "F 70-99", "#option-14 > div > div > div", 1),
        CategoryAthLinks(2015, URL_2015, "M 12-15", "#option-16 > div > div > div", 1),
        CategoryAthLinks(2015, URL_2015, "M 16-19", "#option-17 > div > div > div", 2),
        CategoryAthLinks(2015, URL_2015, "M 20-24", "#option-18 > div > div > div", 8),
        CategoryAthLinks(2015, URL_2015, "M 25-29", "#option-19 > div > div > div", 16),
        CategoryAthLinks(2015, URL_2015, "M 30-34", "#option-20 > div > div > div", 15),
        CategoryAthLinks(2015, URL_2015, "M 35-39", "#option-21 > div > div > div", 16),
        CategoryAthLinks(2015, URL_2015, "M 40-44", "#option-22 > div > div > div", 16),
        CategoryAthLinks(2015, URL_2015, "M 45-49", "#option-23 > div > div > div", 13),
        CategoryAthLinks(2015, URL_2015, "M 50-54", "#option-24 > div > div > div", 9),
        CategoryAthLinks(2015, URL_2015, "M 55-59", "#option-25 > div > div > div", 6),
        CategoryAthLinks(2015, URL_2015, "M 60-64", "#option-26 > div > div > div", 3),
        CategoryAthLinks(2015, URL_2015, "M 65-69", "#option-27 > div > div > div", 2),
        CategoryAthLinks(2015, URL_2015, "M 70-99", "#option-28 > div > div > div", 1)
)


@Component
class PhiladelphiaAthProducerNumbered(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                                      @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthCategoryProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(PhiladelphiaAthProducerNumbered::class.java), MarathonSources.Philadelphia, categories)

@Component
class PhiladelphiaXacteProducerNumbered(@Autowired private val xacteMarathonScraper: XacteMarathonScraper,
                                        @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedResultsPageProducer(numberedResultsPageRepository, LoggerFactory.getLogger(PhiladelphiaXacteProducerNumbered::class.java), MarathonSources.Philadelphia) {

    private val columnPositions = MergedAgedGenderColumPositions(
            place = 8,
            nationality = 3,
            finishTime = 5,
            ageGender = 4)

    private val scrapeInfo = PagedScrapeInfo(
            url = "http://live.xacte.com/templates/philadelphiamarathon.com/for-runners/",
            columnPositions = columnPositions,
            marathonSources = this.marathonSources,
            marathonYear = -1,
            tableBodySelector = "#xact_results_agegroup_results > tbody:nth-child(3)",
            skipRowCount = 0,
            startPage = -1,
            endPage = -1,
            currentPage = -1,
            clickNextSelector = "#xact_results_agegroup_results_wrapper > div.fg-toolbar.ui-toolbar.ui-widget-header.ui-corner-bl.ui-corner-br.ui-helper-clearfix > div.dataTables_paginate.fg-buttonset.ui-buttonset.fg-buttonset-multi.ui-buttonset-multi.paging_full_numbers > a.next.fg-button.ui-button.ui-state-default",
            clickPreviousSelector = "#xact_results_agegroup_results_wrapper > div.fg-toolbar.ui-toolbar.ui-widget-header.ui-corner-bl.ui-corner-br.ui-helper-clearfix > div.dataTables_paginate.fg-buttonset.ui-buttonset.fg-buttonset-multi.ui-buttonset-multi.paging_full_numbers > a.previous.fg-button.ui-button.ui-state-default",
            category = null,
            gender = null,
            secondaryClickNextSelector = null)

    override fun buildYearlyThreads(year: Int, lastPage: Int) {
        val localScrapeInfo = scrapeInfo.copy(marathonYear = year, startPage = lastPage, currentPage = lastPage)
        when (year) {
            2016 -> {
                threads.add(xacteMarathonScraper.scrape(localScrapeInfo.copy(endPage = 51), PhiladelphiaPreWebScrapeEvent("Overall Male")))
                threads.add(xacteMarathonScraper.scrape(localScrapeInfo.copy(endPage = 40), PhiladelphiaPreWebScrapeEvent("Overall Female")))
            }
            2017 -> {
                threads.add(xacteMarathonScraper.scrape(localScrapeInfo.copy(endPage = 46), PhiladelphiaPreWebScrapeEvent("Overall Male")))
                threads.add(xacteMarathonScraper.scrape(localScrapeInfo.copy(endPage = 33), PhiladelphiaPreWebScrapeEvent("Overall Female")))
            }
        }
    }
}

@Component
class PhiladelphiaProducer(@Autowired private val philadelphiaAthProducer: PhiladelphiaAthProducerNumbered,
                           @Autowired private val philadelphiaXacteProducer: PhiladelphiaXacteProducerNumbered) :
        AbstractBaseProducer(LoggerFactory.getLogger(PhiladelphiaProducer::class.java), MarathonSources.Philadelphia) {

    override fun buildThreads() {
        threads.addAll(philadelphiaAthProducer.process())
        threads.addAll(philadelphiaXacteProducer.process())
    }
}