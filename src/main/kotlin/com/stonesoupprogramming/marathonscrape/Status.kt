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
                    MarathonSources.Taipei -> logger.printProgress(source, 5317, 4668, 5560, 5988)
                    MarathonSources.Amsterdam -> logger.info("TODO: Amsterdam")
                    MarathonSources.Santiago -> logger.printProgress(source, 3667, 4516, 4647, 4619)
                    MarathonSources.Copenhagen -> logger.printProgress(source, 9621, 9214, 8371, 8153)
                    MarathonSources.Geneva -> logger.printProgress(source, 1501, 1523, 1477, 1840)
                    MarathonSources.RheinEnergie -> logger.printProgress(source, 3952, 4370, 5098, 4524)
                    MarathonSources.Bournemouth -> logger.printProgress(source, 1968, 1880, 2025, 2029)
                    MarathonSources.Memphis -> logger.printProgress(source, 2657, 2510, 2506, 2346)
                    MarathonSources.Indianapolis -> logger.printProgress(source, 3735, 4026, 4137, 4677)
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