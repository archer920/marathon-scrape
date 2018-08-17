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
    fun reportStatus(): CompletableFuture<String> {
        try {
            while (!shutdown){
                logger.info("")

                logger.printProgress("Berlin", Sources.BERLIN, 146965)
                logger.printProgress("Boston", Sources.BOSTON, 137483)
                logger.printProgress("Chicago", Sources.CHICAGO, 403878)
                logger.printProgress("Los Angelas", Sources.LA, 82974)
                logger.printProgress("Marine Corps", Sources.MARINES, 82591)
                logger.printProgress("New York", Sources.NY_MARATHON_GUIDE, 199372)
                logger.printProgress("Vienna", Sources.VIENNA, 30127)
                logger.printProgress("San Francisco", Sources.SAN_FRANSCISO, 6624, 6071, 6335, 6586, 5276)
                logger.printProgress("Medtronic", Sources.MEDTRONIC, 8853, 8546, 8561, 7490)
                logger.printProgress("Disney", Sources.DISNEY, 97025, 19235)
                logger.printProgress("Ottawa", Sources.OTTAWA, 5594, 4664, 4370, 4564)

                logger.info("")
                Thread.sleep(10000)
            }
            return successResult()
        } catch (e : Exception) {
            logger.error("Error in Status Reporter", e)
            return failResult()
        }
    }

    private fun Logger.printProgress(name : String, source : String, vararg yearTotals : Int){
        val percentFormat = NumberFormat.getPercentInstance()

        val count = runnerDataRepository.countBySource(source)
        val total = yearTotals.sum()
        val percent = count.toDouble() / total.toDouble()

        info("$name at ${percentFormat.format(percent)}: $count / $total")
    }
}