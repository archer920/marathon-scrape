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
    fun reportStatusAsync(source: MarathonSources): CompletableFuture<String>

    fun reportStatus(source: MarathonSources)
}

@Service
class StatusReporterServiceImpl(@Autowired private val runnerDataRepository: RunnerDataRepository) : StatusReporterService {

    private val logger = LoggerFactory.getLogger(StatusReporterServiceImpl::class.java)
    override var shutdown = false

    override fun reportStatus(source: MarathonSources) {
        when (source) {
            MarathonSources.Seamtown -> logger.printProgress(source, 956, 1522, 1382, 1528)
            MarathonSources.Seamtown -> logger.printProgress(source, 2183, 2227, 1712, 1421)
            MarathonSources.Mohawk -> logger.printProgress(source, 893, 1145, 1113, 890)
            MarathonSources.StLouis -> logger.printProgress(source, 1395, 1377, 1338, 1181)
            MarathonSources.LongBeach -> logger.printProgress(source, 2782, 2324, 1951, 1709)
            MarathonSources.PoweradeMonterrery -> logger.printProgress(source, 4160, 5344, 6242, 6696)
            MarathonSources.Istanbul -> logger.printProgress(source, 3877, 2783, 2783, 1701)
            MarathonSources.Milwaukee -> logger.printProgress(source, 2087, 2281, 2031, 1736)
            MarathonSources.PfChangsArizona -> logger.printProgress(source, 2882, 2592, 2346, 2343)
            MarathonSources.Helsinki -> logger.printProgress(source, 3865, 3541, 2718, 2330)
            MarathonSources.RockRollLasVegas -> logger.printProgress(source, 3228, 3106, 2594, 2987)
            MarathonSources.NoredaRiga -> logger.printProgress(source, 1236, 1484, 1495, 1641)
            MarathonSources.MyrtleBeach -> logger.printProgress(source, 1624, 1477, 1421, 1239)
            MarathonSources.Belfast -> logger.printProgress(source, 2329, 2283, 2156, 2147)
            MarathonSources.Cottonwood -> logger.printProgress(source, 1528, 1265, 1330, 1372)
            MarathonSources.Philadelphia -> logger.printProgress(source, 10359, 9161, 9000, 7773)
            MarathonSources.Berlin -> logger.printProgress(source, 28984, 36838, 36084, 39146)
            MarathonSources.Maritzburg -> logger.printProgress(source, 2296, 2072, 2237, 2442)
            else -> throw IllegalArgumentException("No status for this marathon: $source")
        }
    }

    @Async
    override fun reportStatusAsync(source: MarathonSources): CompletableFuture<String> {
        return try {
            while (!shutdown) {
                reportStatus(source)
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