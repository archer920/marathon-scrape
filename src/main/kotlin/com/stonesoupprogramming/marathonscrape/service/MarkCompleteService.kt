package com.stonesoupprogramming.marathonscrape.service

import com.stonesoupprogramming.marathonscrape.models.AbstractColumnPositions
import com.stonesoupprogramming.marathonscrape.models.AbstractScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.RunnerData
import com.stonesoupprogramming.marathonscrape.repository.ResultsRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface MarkCompleteService<T : AbstractColumnPositions, V : ResultsPage> {

    fun markComplete(clazz: Class<V>, scrapeInfo: AbstractScrapeInfo<T, V>, runnerDataResults: List<RunnerData>)
}

@Service
class MarkCompleteServiceImpl<T : AbstractColumnPositions, V : ResultsPage>(
        @Autowired private val resultsRepository: ResultsRepository<V>) : MarkCompleteService<T, V> {

    private val logger = LoggerFactory.getLogger(MarkCompleteServiceImpl::class.java)

    override fun markComplete(clazz: Class<V>, scrapeInfo: AbstractScrapeInfo<T, V>, runnerDataResults: List<RunnerData>) {
        try {
            val entity = scrapeInfo.toEntity(clazz)
            entity.runnerData.addAll(runnerDataResults)
            resultsRepository.save(entity)
        } catch (e: Exception) {
            logger.error("Failed to save results page", e)
            throw e
        }
    }
}