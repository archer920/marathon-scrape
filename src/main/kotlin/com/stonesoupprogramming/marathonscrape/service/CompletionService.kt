package com.stonesoupprogramming.marathonscrape.service

import com.stonesoupprogramming.marathonscrape.models.AbstractColumnPositions
import com.stonesoupprogramming.marathonscrape.models.PagedScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.RunnerData
import com.stonesoupprogramming.marathonscrape.repository.ResultsRepository
import com.stonesoupprogramming.marathonscrape.repository.RunnerDataRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface CompletionService<T : AbstractColumnPositions, U : PagedScrapeInfo<T>, V : ResultsPage> {

    fun markComplete(pagedScrapeInfo: PagedScrapeInfo<T>, runnerDataResults : List<RunnerData>)
}

@Service
class CompletionServiceImpl<T : AbstractColumnPositions, U : PagedScrapeInfo<T>, V : ResultsPage>(@Autowired private val runnerDataRepository: RunnerDataRepository,
                            @Autowired private val resultsRepository: ResultsRepository<V>) : CompletionService<T, U, V> {

    private val logger = LoggerFactory.getLogger(CompletionServiceImpl::class.java)

    override fun markComplete(pagedScrapeInfo: PagedScrapeInfo<T>, runnerDataResults: List<RunnerData>) {
        try {
            runnerDataRepository.saveAll(runnerDataResults)

        }
    }

}