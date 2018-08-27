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
        try {
            while (!shutdown){
                when(source){
                    MarathonSources.Berlin -> logger.printProgress("Berlin", source, 28984, 36838, 36084, 39146)
                    MarathonSources.Unassigned -> throw IllegalArgumentException("No results for Unassigned")
                    MarathonSources.Vienna -> logger.printProgress("Vienna", source,
                            1160, //2014 - W
                            5188, //2014 - M
                            1122, //2015 - W
                            4499, //2015 - M
                            1325, //2016 - W
                            5175, //2016 - M
                            1375, //2017 - W
                            4996, //2017 - M
                            1177, //2018 - W
                            4254) //2018 - M
                    MarathonSources.Boston -> logger.printProgress("Boston", source, 137483)
                    MarathonSources.Chicago -> logger.printProgress("Chicago", source, 403878)
                    MarathonSources.Nyc -> logger.printProgress("New York", source, 50433, 49828, 48468, 50643)
                    MarathonSources.LosAngeles -> logger.printProgress("Los Angeles", source, 82974)
                    MarathonSources.Marines -> logger.printProgress("Marine Corps", source, 19688, 23183, 19768, 20042)
                    MarathonSources.TwinCities -> logger.printProgress("Medtronic", source, 8853, 8546, 8561, 7490)
                    MarathonSources.Disney -> logger.printProgress("Disney", source, 97025, 19235)
                    MarathonSources.Ottawa -> logger.printProgress("Ottawa", source, 5594, 4664, 4370, 4564)
                    MarathonSources.Budapest -> logger.printProgress("Budapest", source, 4348, 5604, 4969, 5415)
                    MarathonSources.SanFranscisco -> logger.printProgress("San Francisco", source, 6624, 6071, 6335, 6586, 5276)
                    MarathonSources.Melbourne -> logger.printProgress("Melbourne", source, 6108, 6083, 6091)
                    MarathonSources.Taipei -> logger.printProgress("Taipei", source, 5317, 4668, 5560, 5998)
                    MarathonSources.Yuengling -> logger.printProgress("Yuengling", source, 2792, 2185, 1841, 1370)
                    MarathonSources.Honolulu -> logger.printProgress("Honolulu", source, 21798, 21540, 20105, 20350)
                    MarathonSources.Jeruselm -> logger.printProgress("Jeruslem", source, 1489, 1120, 1246, 1382)
                    MarathonSources.Eversource -> logger.printProgress("Eversource Hartford", source, 2417, 1977, 1883, 1695)
                    MarathonSources.LittleRock -> logger.printProgress("Little Rock", source, 1755, 2427, 1940, 2176)
                    MarathonSources.FlyingPig -> logger.printProgress("Flying Pig", source, 3902, 3820, 3792, 3363)
                    MarathonSources.KentuckyDerby -> logger.printProgress("Kentucky Derby", source, 2029, 1963, 1809, 1517)
                    MarathonSources.Queenstown -> logger.printProgress("Queenstown", source, 1446, 1609, 1539, 1530)
                    MarathonSources.BigSur -> logger.printProgress("Big Sur", source, 3339, 3430, 3241, 3242)
                    MarathonSources.NewJersey -> logger.printProgress("New Jersey", source, 2128, 1919, 1989, 2107)
                    MarathonSources.KaiserPermanete -> logger.printProgress("Kaiser", source, 1332, 1422, 1263, 1335)
                    else -> throw IllegalArgumentException("No status for this marathon: $source")
                }

                Thread.sleep(10000)
            }
            return successResult()
        } catch (e : Exception) {
            logger.error("Error in Status Reporter", e)
            return failResult()
        }
    }

    private fun Logger.printProgress(name : String, source : MarathonSources, vararg yearTotals : Int){
        printBlankLines()

        val percentFormat = NumberFormat.getPercentInstance()

        val count = runnerDataRepository.countBySource(source)
        val total = sum(*yearTotals)
        val percent = count.toDouble() / total.toDouble()

        info("$name at ${percentFormat.format(percent)}: $count / $total")

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