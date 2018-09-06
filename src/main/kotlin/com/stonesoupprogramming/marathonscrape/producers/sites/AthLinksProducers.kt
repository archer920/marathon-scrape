package com.stonesoupprogramming.marathonscrape.producers.sites

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.AbstractColumnPositions
import com.stonesoupprogramming.marathonscrape.models.AgeGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.PagedScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.sites.CategoryAthLinks
import com.stonesoupprogramming.marathonscrape.models.sites.SequenceAthLinks
import com.stonesoupprogramming.marathonscrape.producers.AbstractResultsPageProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.AthLinksMarathonScraper
import com.stonesoupprogramming.marathonscrape.scrapers.sites.AthLinksPreWebScrapeEvent
import org.slf4j.Logger
import javax.annotation.PostConstruct

abstract class AbstractBaseAthProducer(
        protected val athLinksMarathonScraper: AthLinksMarathonScraper,
        numberedResultsPageRepository: NumberedResultsPageRepository,
        logger: Logger,
        marathonSources: MarathonSources) : AbstractResultsPageProducer<AbstractColumnPositions>(numberedResultsPageRepository, logger, marathonSources) {

    protected val baseScrapeInfo = PagedScrapeInfo<AbstractColumnPositions>(
            url = "",
            marathonSources = marathonSources,
            marathonYear = -1,
            tableBodySelector = "",
            headerRow = false,
            columnPositions = AgeGenderColumnPositions(-1, -1, -1, null, -1, -1), //Column positions are irrelevant since the scraping is done by the js
            startPage = -1,
            currentPage = -1,
            endPage = -1,
            clickNextSelector = "#pager > div:nth-child(1) > div:nth-child(6) > button:nth-child(1)",
            clickPreviousSelector = "#pager > div:nth-child(1) > div:nth-child(1) > button:nth-child(1)",
            secondaryClickNextSelector = "#pager > div:nth-child(1) > div:nth-child(7) > button:nth-child(1)",
            category = null,
            gender = null)
}

abstract class AbstractAthSequenceProducer(
        athLinksMarathonScraper: AthLinksMarathonScraper,
        numberedResultsPageRepository: NumberedResultsPageRepository,
        logger: Logger,
        marathonSources: MarathonSources,
        private val sequenceAthLinks: List<SequenceAthLinks>) : AbstractBaseAthProducer(athLinksMarathonScraper, numberedResultsPageRepository, logger, marathonSources) {

    override fun buildYearlyThreads(year: Int, lastPage: Int) {
        sequenceAthLinks.forEach { it ->
            if (it.year == year) {
                startScrape(baseScrapeInfo.copy(url = it.url,
                        marathonYear = year,
                        startPage = lastPage,
                        currentPage = lastPage,
                        endPage = it.endPage))
            }
        }
    }

    private fun startScrape(pagedScrapeInfo: PagedScrapeInfo<AbstractColumnPositions>) {
        threads.add(athLinksMarathonScraper.scrape(pagedScrapeInfo))
    }
}

abstract class AbstractAthCategoryProducer(
        athLinksMarathonScraper: AthLinksMarathonScraper,
        numberedResultsPageRepository: NumberedResultsPageRepository,
        logger: Logger,
        marathonSources: MarathonSources,
        private val categoryAthLinks: List<CategoryAthLinks>) : AbstractBaseAthProducer(athLinksMarathonScraper, numberedResultsPageRepository, logger, marathonSources) {

    private val categoryLastPageMap = mutableMapOf<CategoryAthLinks, Int>()

    @PostConstruct
    private fun init() {
        categoryAthLinks.forEach { it ->
            var startPage = numberedResultsPageRepository.queryLastPage(it.year, marathonSources, it.category) ?: 0
            startPage++
            categoryLastPageMap[it] = startPage
        }
    }

    override fun buildThreads() {
        categoryAthLinks.forEach { it ->
            val page = categoryLastPageMap[it] ?: 0
            threads.add(athLinksMarathonScraper.scrape(
                    baseScrapeInfo.copy(url = it.url, marathonYear = it.year, startPage = page, currentPage = page,
                            endPage = it.endPage, category = it.category),
                    AthLinksPreWebScrapeEvent(it.divisionCss)))
        }
    }

    override fun buildYearlyThreads(year: Int, lastPage: Int) {
        throw NotImplementedError("This method isn't used in this class")
    }
}