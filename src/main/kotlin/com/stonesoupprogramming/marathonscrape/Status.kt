package com.stonesoupprogramming.marathonscrape

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.text.NumberFormat

@Component
class StatusReporter(@Autowired private val runnerDataRepository: RunnerDataRepository){

    private val logger = LoggerFactory.getLogger(StatusReporter::class.java)

    @Async
    fun reportStatus(){
        while (true){
            val percentFormat = NumberFormat.getPercentInstance()

            val berlinCount = runnerDataRepository.countBySource(Sources.BERLIN)
            val berlinTotal = 146965
            val berlinPercent = berlinCount.toDouble() / berlinTotal.toDouble()
            logger.info("Berlin at ${percentFormat.format(berlinPercent)}: $berlinCount / $berlinTotal")

//            val bostonCount = runnerDataRepository.countBySource(Sources.BOSTON)
//            val bostonTotal =

            val chicagoCount = runnerDataRepository.countBySource(Sources.CHICAGO)
            val chicagoTotal = 403878
            val chicagoPercent = chicagoCount.toDouble() / chicagoTotal.toDouble()
            logger.info("Chicago at ${percentFormat.format(chicagoPercent)}: $chicagoCount / $chicagoTotal")

            val laCount = runnerDataRepository.countBySource(Sources.LA)
            val laTotal = 82974
            val laPercent = laCount.toDouble() / laTotal.toDouble()
            logger.info("LA at ${percentFormat.format(laPercent)}: $laCount / $laTotal")

            val marinesCount = runnerDataRepository.countBySource(Sources.MARINES)
            val marinesTotal = 82591
            val marinesPercent = marinesCount.toDouble() / marinesTotal.toDouble()
            logger.info("Marines at ${percentFormat.format(marinesPercent)}: $marinesCount / $marinesTotal")

            val nyMarathonCount = runnerDataRepository.countBySource(Sources.NY_MARATHON_GUIDE)
            val nyMarathonTotal = 199372
            val nyPercent = nyMarathonCount.toDouble() / nyMarathonTotal.toDouble()
            logger.info("NY at ${percentFormat.format(nyPercent)}: $nyMarathonCount / $nyMarathonTotal")


            val viennaCount = runnerDataRepository.countBySource(Sources.VIENNA)
            val viennaTotal = 30127
            val viennaPercent = viennaCount.toDouble() / viennaTotal.toDouble()
            logger.info("Vienna at ${percentFormat.format(viennaPercent)}: $viennaCount / $viennaTotal")

            Thread.sleep(1000 * 60 * 5)
        }
    }
}