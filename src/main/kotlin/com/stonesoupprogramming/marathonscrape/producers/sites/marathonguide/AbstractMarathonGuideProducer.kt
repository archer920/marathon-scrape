package com.stonesoupprogramming.marathonscrape.producers.sites.marathonguide

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.extension.filterCompleteByUrlAndCategory
import com.stonesoupprogramming.marathonscrape.models.AbstractScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumPositions
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.StandardScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.sites.MarathonGuideInfo
import com.stonesoupprogramming.marathonscrape.producers.AbstractResultsPageProducer
import com.stonesoupprogramming.marathonscrape.repository.ResultsRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.MarathonGuidePreWebScrapeEvent
import com.stonesoupprogramming.marathonscrape.scrapers.sites.MarathonGuideScraper
import org.slf4j.Logger

abstract class AbstractMarathonGuideProducer(
        private val marathonGuideScraper: MarathonGuideScraper,
        pagedResultsRepository: ResultsRepository<ResultsPage>,
        logger: Logger,
        sources: MarathonSources,
        private val marathonGuideInfo: List<MarathonGuideInfo>)
    : AbstractResultsPageProducer<ResultsPage>(pagedResultsRepository, logger, sources) {

    private fun buildCategories(info: MarathonGuideInfo): List<String> {
        val list = mutableListOf<String>()
        with(info) {
            for (i in 1..numRecords step categoryIncrement) {
                val top = if (i + (categoryIncrement - 1) < numRecords) {
                    i + categoryIncrement - 1
                } else {
                    numRecords
                }
                list.add("$i - $top")
            }
        }

        return list.toList()
    }

    override fun buildThreads() {
        val scrapeInfoList = buildScrapeInfo(marathonGuideInfo)
        completed.filterCompleteByUrlAndCategory(scrapeInfoList).forEach {
            threads.add(marathonGuideScraper.scrape(it, MarathonGuidePreWebScrapeEvent(it.category
                    ?: throw IllegalStateException("Category is still null"))))
        }
    }

    private fun buildScrapeInfo(marathonGuideInfo: List<MarathonGuideInfo>): List<AbstractScrapeInfo<MergedAgedGenderColumPositions, ResultsPage>> {
        return marathonGuideInfo.map { info ->
            val scrapeInfo = StandardScrapeInfo<MergedAgedGenderColumPositions, ResultsPage>(
                    url = info.url,
                    marathonSources = this.marathonSources,
                    marathonYear = info.year,
                    tableBodySelector = "table.BoxTitleOrange > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1)",
                    skipRowCount = 2,
                    columnPositions = info.columnPositions,
                    category = null,
                    gender = null)
            buildCategories(info).map { it ->
                scrapeInfo.copy(category = it)
            }.toList()
        }.flatMap { it -> it.toList() }
    }
}