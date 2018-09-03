package com.stonesoupprogramming.marathonscrape

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.text.NumberFormat
import java.util.concurrent.CompletableFuture

@Component
class StatusReporter(@Autowired private val runnerDataRepository: RunnerDataRepository){

    private val logger = LoggerFactory.getLogger(StatusReporter::class.java)
    var shutdown = false

    @Async
    fun reportStatus(source: MarathonSources): CompletableFuture<String> {
        return try {
            while (!shutdown){
                when(source){
                    MarathonSources.Stockholm -> logger.printProgress(source,
                            11947, 4121, 10889, 3922, 9462, 3393, 9044, 3527, 10182, 4177)
                    MarathonSources.Amsterdam -> logger.printProgress(source, 12217, 12359, 12194, 11443)
                    MarathonSources.Santiago -> logger.printProgress(source, 3667, 4516, 4647, 4619)
                    MarathonSources.Copenhagen -> logger.printProgress(source, 9621, 9214, 8371, 8153)
                    MarathonSources.Geneva -> logger.printProgress(source, 1501, 1523, 1477, 1840)
                    MarathonSources.Bayshore -> logger.printProgress(source, 2017, 2043, 2013, 1708)
                    MarathonSources.RheinEnergie -> logger.printProgress(source, 3952, 4370, 5098, 4524)
                    MarathonSources.Bournemouth -> logger.printProgress(source, 1968, 1880, 2025, 2029)
                    MarathonSources.Memphis -> logger.printProgress(source, 2657, 2510, 2506, 2346)
                    MarathonSources.Indianapolis -> logger.printProgress(source, 3735, 4026, 4137, 4677)
                    MarathonSources.Munchen -> logger.printProgress(source, 6228, 5903, 4882, 4359)
                    MarathonSources.Fargo -> logger.printProgress(source, 1655, 1535, 1494, 1303)
                    MarathonSources.Brighton -> logger.printProgress(source, 8686, 9214, 10666, 12158)
                    MarathonSources.Vancouver -> logger.printProgress(source, 3784, 3578, 3778, 3577)
                    MarathonSources.SurfCity -> logger.printProgress(source, 2379, 2170, 1949, 1582)
                    MarathonSources.Liverpool -> logger.printProgress(source, 2326, 2486, 2531, 3009)
                    MarathonSources.SanDiego -> logger.printProgress(source, 5331, 4309, 5823, 5353)
                    MarathonSources.Akron -> logger.printProgress(source, 1576, 1473, 1154, 927)
                    MarathonSources.Venice -> logger.printProgress(source, 5358, 6645, 4611, 5908)
                    MarathonSources.Route66 -> logger.printProgress(source, 1671, 2562, 1500, 1564)
                    MarathonSources.RiverRock -> logger.printProgress(source, 2330, 2282, 2191, 2148)
                    else -> throw IllegalArgumentException("No status for this marathon: $source")
                }
                Thread.sleep(10000)
            }
            successResult()
        } catch (e : Exception) {
            logger.error("Error in Status Reporter", e)
            failResult()
        }
    }

    private fun Logger.printProgress(source : MarathonSources, vararg yearTotals : Int){
        printBlankLines()

        val percentFormat = NumberFormat.getPercentInstance()

        val count = runnerDataRepository.countBySource(source)
        val total = sum(*yearTotals)
        val percent = count.toDouble() / total.toDouble()

        info("${source.name} at ${percentFormat.format(percent)}: $count / $total")

        printBlankLines()
    }

    private fun sum(vararg amounts : Int) : Int{
        var total = 0
        for(amount in amounts){
            total += amount
        }
        return total
    }
}