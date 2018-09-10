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
    fun reportStatusAsync(source: MarathonSources, blankLines: Boolean = true): CompletableFuture<String>

    fun reportStatus(source: MarathonSources, blankLines: Boolean = true)

    fun reportBulkStatus(sources: List<MarathonSources>)

    @Async
    fun reportBulkStatusAsync(sources: List<MarathonSources>)
}

@Service
class StatusReporterServiceImpl(@Autowired private val runnerDataRepository: RunnerDataRepository) : StatusReporterService {


    private val logger = LoggerFactory.getLogger(StatusReporterServiceImpl::class.java)
    override var shutdown = false

    override fun reportBulkStatus(sources: List<MarathonSources>) {
        logger.printBlankLines()

        sources.forEach { reportStatus(it, false) }

        logger.printBlankLines()
    }

    override fun reportBulkStatusAsync(sources: List<MarathonSources>) {
        reportBulkStatus(sources)
    }

    override fun reportStatus(source: MarathonSources, blankLines: Boolean) {
        when (source) {
            MarathonSources.Chester -> logger.printProgress(source, blankLines, 2468, 2287, 2155, 2587)
            MarathonSources.Snowdonia -> logger.printProgress(source, blankLines, 1719, 1846, 2066, 2220)
            MarathonSources.California -> logger.printProgress(source, blankLines, 5777, 5629, 6174, 6543)
            MarathonSources.RocketCity -> logger.printProgress(source, blankLines, 1463, 1272, 1028, 897)
            MarathonSources.Dallas -> logger.printProgress(source, blankLines, 3950, 2747, 2807, 2818)
            MarathonSources.Charleston -> logger.printProgress(source, blankLines, 1185, 913, 960, 963)
            MarathonSources.Carlsbad -> logger.printProgress(source, blankLines, 1389, 1083, 1082, 927)
            MarathonSources.NewOrleans -> logger.printProgress(source, blankLines, 2749, 2353, 3625, 2824)
            MarathonSources.Woodlands -> logger.printProgress(source, blankLines, 1230, 894, 874, 935)
            MarathonSources.Phoenix -> logger.printProgress(source, blankLines, 1816, 1881, 2109, 2004)
            MarathonSources.NapaValley -> logger.printProgress(source, blankLines, 1741, 1885, 1719, 1269)
            MarathonSources.Illinois -> logger.printProgress(source, blankLines, 1684, 1051, 1452, 1215)
            MarathonSources.OklahomaCity -> logger.printProgress(source, blankLines, 2623, 2489, 2225, 2201)
            MarathonSources.GlassCity -> logger.printProgress(source, blankLines, 858, 963, 1146, 1070)
            MarathonSources.Rotorua -> logger.printProgress(source, blankLines, 3511, 1172, 1016, 999)
            MarathonSources.Pittsburgh -> logger.printProgress(source, blankLines, 4500, 4210, 3681, 3359)
            MarathonSources.OC -> logger.printProgress(source, blankLines, 2210, 1862, 1937, 1489)
            MarathonSources.Seattle -> logger.printProgress(source, blankLines, 1867, 1716, 1596, 1439)
            MarathonSources.Miami -> logger.printProgress(source, blankLines, 3529, 2749, 3061, 2972)
            MarathonSources.Portland -> logger.printProgress(source, blankLines, 6258, 5550, 4421, 2912)
            MarathonSources.Lincoln -> logger.printProgress(source, blankLines, 1259, 1145, 1062, 1050)
            MarathonSources.CoxSports -> logger.printProgress(source, blankLines, 1333, 1105, 1029, 1120)
            MarathonSources.MiltonKeynes -> logger.printProgress(source, blankLines, 2105, 1559, 1954, 2020)
            MarathonSources.Burlington -> logger.printProgress(source, blankLines, 2432, 2458, 1894, 1986)
            MarathonSources.MountainsToBeach -> logger.printProgress(source, blankLines, 1621, 1602, 2006, 1947)
            MarathonSources.ChiangMai -> logger.printProgress(source, blankLines, 805)
            MarathonSources.CorkCity -> logger.printProgress(source, blankLines, 524, 1115, 1110, 1151)
            MarathonSources.UtahValley -> logger.printProgress(source, blankLines, 1409, 1144, 954, 1175)
            MarathonSources.Missoula -> logger.printProgress(source, blankLines, 1168, 997, 982, 868)
            MarathonSources.Erie -> logger.printProgress(source, blankLines, 956, 1522, 1382, 1528)
            MarathonSources.Seamtown -> logger.printProgress(source, blankLines, 2183, 2227, 1712, 1421)
            MarathonSources.Mohawk -> logger.printProgress(source, blankLines, 893, 1145, 1113, 890)
            MarathonSources.StLouis -> logger.printProgress(source, blankLines, 1395, 1377, 1338, 1181)
            MarathonSources.LongBeach -> logger.printProgress(source, blankLines, 2782, 2324, 1951, 1709)
            MarathonSources.PoweradeMonterrery -> logger.printProgress(source, blankLines, 4160, 5344, 6242, 6696)
            MarathonSources.Istanbul -> logger.printProgress(source, blankLines, 3877, 2783, 2783, 1701)
            MarathonSources.Milwaukee -> logger.printProgress(source, blankLines, 2087, 2281, 2031, 1736)
            MarathonSources.PfChangsArizona -> logger.printProgress(source, blankLines, 2882, 2592, 2346, 2343)
            MarathonSources.Helsinki -> logger.printProgress(source, blankLines, 3865, 3541, 2718, 2330)
            MarathonSources.RockRollLasVegas -> logger.printProgress(source, blankLines, 3228, 3106, 2594, 2987)
            MarathonSources.NoredaRiga -> logger.printProgress(source, blankLines, 1236, 1484, 1495, 1641)
            MarathonSources.MyrtleBeach -> logger.printProgress(source, blankLines, 1624, 1477, 1421, 1239)
            MarathonSources.Belfast -> logger.printProgress(source, blankLines, 2329, 2283, 2156, 2147)
            MarathonSources.Cottonwood -> logger.printProgress(source, blankLines, 1528, 1265, 1330, 1372)
            MarathonSources.Philadelphia -> logger.printProgress(source, blankLines, 10359, 9161, 9000, 7773)
            MarathonSources.Berlin -> logger.printProgress(source, blankLines, 28984, 36838, 36084, 39146)
            MarathonSources.Maritzburg -> logger.printProgress(source, blankLines, 2296, 2072, 2237, 2442)
            else -> throw IllegalArgumentException("No status for this marathon: $source")
        }
    }

    @Async
    override fun reportStatusAsync(source: MarathonSources, blankLines: Boolean): CompletableFuture<String> {
        return try {
            while (!shutdown) {
                reportStatus(source, blankLines)
                Thread.sleep(10000)
            }
            successResult()
        } catch (e: Exception) {
            logger.error("Error in Status Reporter", e)
            failResult()
        }
    }

    private fun Logger.printProgress(source: MarathonSources, blankLines: Boolean = true, vararg yearTotals: Int) {
        if (blankLines) {
            printBlankLines()
        }


        val percentFormat = NumberFormat.getPercentInstance()

        val count = runnerDataRepository.countBySource(source)
        val total = sum(*yearTotals)
        val percent = count.toDouble() / total.toDouble()

        info("${source.name} at ${percentFormat.format(percent)}: $count / $total")

        if (blankLines) {
            printBlankLines()
        }
    }

    private fun sum(vararg amounts: Int): Int {
        var total = 0
        for (amount in amounts) {
            total += amount
        }
        return total
    }
}