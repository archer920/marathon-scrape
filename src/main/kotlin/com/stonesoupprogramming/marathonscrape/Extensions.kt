package com.stonesoupprogramming.marathonscrape

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.extension.saveToCSV
import com.stonesoupprogramming.marathonscrape.models.RunnerData
import com.stonesoupprogramming.marathonscrape.repository.CategoryResultsRepository
import com.stonesoupprogramming.marathonscrape.repository.PagedResultsRepository
import com.stonesoupprogramming.marathonscrape.repository.RunnerDataRepository
import com.stonesoupprogramming.marathonscrape.repository.UrlPageRepository
import org.slf4j.Logger
import javax.validation.ConstraintViolationException

const val UNAVAILABLE = "Unavailable"

fun UrlPage.markComplete(urlPageRepository: UrlPageRepository, runnerDataRepository: RunnerDataRepository, resultsPage: List<RunnerData>, logger: Logger){
    try {
        val runnerDataViolations = mutableListOf<RunnerData>()
        for(rd in resultsPage){
            try {
                runnerDataRepository.save(rd)
            } catch (e : ConstraintViolationException){
                runnerDataViolations.add(rd)
            }
        }
        if(runnerDataViolations.isNotEmpty()){
            logger.info("Saving violations for review")
            runnerDataViolations.saveToCSV("Violations-${System.currentTimeMillis()}.csv")
        }
        urlPageRepository.save(this)
        logger.info("Successfully scraped: $this")
    } catch (e : Exception){
        when(e) {
            is ConstraintViolationException -> {

            }
        }
        logger.error("Failed to mark complete: $this)")
    }
}

fun Array<out String>.toMarathonSources() : List<MarathonSources?> {
    return this.map { arg -> MarathonSources.values().find { arg == it.arg } }.toList()
}

fun CategoryResultsRepository.markPageComplete(runnerDataRepository: RunnerDataRepository, resultsPage: List<RunnerData>, categoryScrapeInfo: CategoryScrapeInfo, logger: Logger){
    try {
        runnerDataRepository.saveAll(resultsPage)
        this.save(categoryScrapeInfo.toCategoryResults())
    } catch (e : Exception){
        logger.error("Failed to make complete $categoryScrapeInfo", e)
        throw e
    }
}

fun PagedResultsRepository.markPageComplete(runnerDataRepository: RunnerDataRepository, resultsPage: List<RunnerData>, pagedResultsScrapeInfo: PagedResultsScrapeInfo, logger: Logger){
    try {
        runnerDataRepository.saveAll(resultsPage)
        this.save(pagedResultsScrapeInfo.toPagedResults())
    } catch (e : Exception){
        logger.error("Failed to make complete $pagedResultsScrapeInfo", e)
        throw e
    }
}

fun UrlPageRepository.markPageComplete(runnerDataRepository: RunnerDataRepository, resultsPage: List<RunnerData>, urlScrapeInfo: UrlScrapeInfo, logger: Logger){
    try {
        runnerDataRepository.saveAll(resultsPage)
        this.save(urlScrapeInfo.toUrlResults())
    } catch (e : Exception){
        logger.error("Failed to make complete $urlScrapeInfo", e)
        throw e
    }
}

