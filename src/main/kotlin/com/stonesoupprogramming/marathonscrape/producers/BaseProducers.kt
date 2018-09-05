package com.stonesoupprogramming.marathonscrape.producers

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.AbstractColumnPositions
import com.stonesoupprogramming.marathonscrape.models.AbstractScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.repository.ResultsRepository
import org.slf4j.Logger
import java.util.concurrent.CompletableFuture
import javax.annotation.PostConstruct

abstract class AbstractBaseProducer(protected val logger: Logger, protected val marathonSources: MarathonSources) {

    protected val threads = mutableListOf<CompletableFuture<String>>()

    fun process(): List<CompletableFuture<String>> {
        return try {
            logger.info("Starting $marathonSources")

            buildThreads()

            threads.toList()
        } catch (e: Exception) {
            logger.error("Failed to start $marathonSources", e)
            emptyList()
        }
    }

    protected abstract fun buildThreads()
}

abstract class AbstractUrlProducer<T : ResultsPage, U : AbstractColumnPositions>(private val pagedResultsRepository: ResultsRepository<T>,
                                                                                 logger: Logger,
                                                                                 marathonSources: MarathonSources) : AbstractBaseProducer(logger, marathonSources) {

    private lateinit var completed: List<ResultsPage>

    @PostConstruct
    private fun init() {
        completed = pagedResultsRepository.findBySource(marathonSources)
    }

    protected fun filterCompleted(inputs: List<AbstractScrapeInfo<U, T>>) =
            inputs.filter { it -> completed.none { cp -> it.url == cp.url } }
}

abstract class AbstractResultsPageProducer<T : AbstractColumnPositions>(protected val numberedResultsPageRepository: NumberedResultsPageRepository,
                                                                        logger: Logger,
                                                                        marathonSources: MarathonSources) : AbstractBaseProducer(logger, marathonSources) {
    private var lastPageNum2014: Int = 0
    private var lastPageNum2015: Int = 0
    private var lastPageNum2016: Int = 0
    private var lastPageNum2017: Int = 0
    private var lastPageNum2018: Int = 0

    @PostConstruct
    private fun init() {
        lastPageNum2014 = numberedResultsPageRepository.queryLastPage(2014, marathonSources) ?: 1
        lastPageNum2015 = numberedResultsPageRepository.queryLastPage(2015, marathonSources) ?: 1
        lastPageNum2016 = numberedResultsPageRepository.queryLastPage(2016, marathonSources) ?: 1
        lastPageNum2017 = numberedResultsPageRepository.queryLastPage(2017, marathonSources) ?: 1
        lastPageNum2018 = numberedResultsPageRepository.queryLastPage(2018, marathonSources) ?: 1
    }

    override fun buildThreads() {
        buildYearlyThreads(2014, lastPageNum2014)
        buildYearlyThreads(2015, lastPageNum2015)
        buildYearlyThreads(2016, lastPageNum2016)
        buildYearlyThreads(2017, lastPageNum2017)
        buildYearlyThreads(2018, lastPageNum2018)
    }

    abstract fun buildYearlyThreads(year: Int, lastPage: Int)
}