package com.stonesoupprogramming.marathonscrape.service

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.extension.failResult
import com.stonesoupprogramming.marathonscrape.extension.printBlankLines
import com.stonesoupprogramming.marathonscrape.extension.successResult
import com.stonesoupprogramming.marathonscrape.repository.RunnerDataRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.text.NumberFormat
import java.util.concurrent.CompletableFuture

interface StatusReporterService {
    var shutdown: Boolean

    @Async
    fun reportStatus(source: MarathonSources): CompletableFuture<String>
}

@Service
class StatusReporterServiceImpl(@Autowired private val runnerDataRepository: RunnerDataRepository) : StatusReporterService {

    private val logger = LoggerFactory.getLogger(StatusReporterServiceImpl::class.java)
    override var shutdown = false

    @Async
    override fun reportStatus(source: MarathonSources): CompletableFuture<String> {
        return try {
            while (!shutdown) {
                when (source) {
                    MarathonSources.MyrtleBeach -> logger.printProgress(source, 1624, 1477, 1421, 1239)
                    MarathonSources.Belfast -> logger.printProgress(source, 2329, 2283, 2156, 2147)
                    MarathonSources.Cottonwood -> logger.printProgress(source, 1528, 1265, 1330, 1372)
                    MarathonSources.Philadelphia -> logger.printProgress(source, 10359, 9161, 9000, 7773)
                    MarathonSources.Berlin -> logger.printProgress(source, 28984, 36838, 36084, 39146)
                    MarathonSources.Maritzburg -> logger.printProgress(source, 2296, 2072, 2237, 2442)
                    else -> throw IllegalArgumentException("No status for this marathon: $source")
                }
                Thread.sleep(10000)
            }
            successResult()
        } catch (e: Exception) {
            logger.error("Error in Status Reporter", e)
            failResult()
        }
    }

    private fun Logger.printProgress(source: MarathonSources, vararg yearTotals: Int) {
        printBlankLines()

        val percentFormat = NumberFormat.getPercentInstance()

        val count = runnerDataRepository.countBySource(source)
        val total = sum(*yearTotals)
        val percent = count.toDouble() / total.toDouble()

        info("${source.name} at ${percentFormat.format(percent)}: $count / $total")

        printBlankLines()
    }

    private fun sum(vararg amounts: Int): Int {
        var total = 0
        for (amount in amounts) {
            total += amount
        }
        return total
    }
}