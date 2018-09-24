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
    fun reportBulkStatusAsync(sources: List<MarathonSources>): CompletableFuture<String>
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

    override fun reportBulkStatusAsync(sources: List<MarathonSources>): CompletableFuture<String> {
        return try {
            while (!shutdown) {
                reportBulkStatus(sources)
                Thread.sleep(10000)
            }
            successResult()
        } catch (e: Exception) {
            logger.error("Error in Status Reporter", e)
            failResult()
        }
    }

    override fun reportStatus(source: MarathonSources, blankLines: Boolean) {
        when (source) {
            MarathonSources.Treviso -> logger.printProgress(source, blankLines, 1835, 1551, 1180, 801)
            MarathonSources.Rome -> logger.printProgress(source, blankLines, 14875, 11487, 13881, 13318)
            MarathonSources.Florence -> logger.printProgress(source, blankLines, 8686, 8275, 8215, 8438)
            MarathonSources.Oslo -> logger.printProgress(source, blankLines, 2272, 2391, 2404, 2237)
            MarathonSources.Ljubljanski -> logger.printProgress(source, blankLines, 1357, 1884, 1949, 1894)
            MarathonSources.Dublin -> logger.printProgress(source, blankLines, 16764, 15887)
            MarathonSources.Padova -> logger.printProgress(source, blankLines, 1432, 1374, 1740, 1541)
            MarathonSources.London -> logger.printProgress(source, blankLines, 35817, 37581, 39091, 39406)
            MarathonSources.Turin -> logger.printProgress(source, blankLines, 1367, 1579, 1574, 3545)
            MarathonSources.Barcelona -> logger.printProgress(source, blankLines, 14223, //2014 is an estimate
                    15380, 16504, 16189)
            MarathonSources.Ottawa -> logger.printProgress(source, blankLines, 4664)
            MarathonSources.Kaiser -> logger.printProgress(source, blankLines, 1332, 1422, 1263, 1335)
            MarathonSources.Cracovia -> logger.printProgress(source, blankLines, 5358, 4577, 5552, 5615)
            MarathonSources.Columbus -> logger.printProgress(source, blankLines, 5459, 4440, 3849, 3203)
            MarathonSources.SanSebastian -> logger.printProgress(source, blankLines, 2914, 2969, 2532)
            MarathonSources.Freiburg -> logger.printProgress(source, blankLines, 1034, 1088, 814, 743)
            MarathonSources.Milano -> logger.printProgress(source, blankLines, 3556, 4002, 3719, 5304)
            MarathonSources.Hca -> logger.printProgress(source, blankLines, (50 * 33 + 12), (50 * 30 + 21), (26 * 50 + 43), (26 * 50 + 8))
            MarathonSources.Dresden -> logger.printProgress(source, blankLines, 1420, (998 + 241), 1198, (883 + 179))
            MarathonSources.Axexander -> logger.printProgress(source, blankLines, 1225, 1389, 1544, 1561)
            MarathonSources.Jungfrau -> logger.printProgress(source, blankLines, 3959, 4099, 3649, (3760 + 11 + 1257 + 7))
            MarathonSources.Hamburg -> logger.printProgress(source, blankLines, 10051, 2818, 11405, 3365, 9378, 2701, 9146, 2787)
            MarathonSources.Antwerp -> logger.printProgress(source, blankLines, 1989, 2232, 2027, 2060)
            MarathonSources.Singapore -> logger.printProgress(source, blankLines, (514 * 25 + 6), (458 * 25 + 5), (415 * 25 + 16), (388 * 25 + 19))
            MarathonSources.Auckland -> logger.printProgress(source, blankLines, 2306, 1505, 1632, 1565)
            MarathonSources.WhiteKnightInternational -> logger.printProgress(source, blankLines, 2172, 3050, 2840, 3001)
            MarathonSources.Eindhoven -> logger.printProgress(source, blankLines, 1315, 2022, 2149, 2139)
            MarathonSources.EdpPorto -> logger.printProgress(source, blankLines, 4041, 4405, 4747, 4529)
            MarathonSources.BaxtersLochNess -> logger.printProgress(source, blankLines, 2480, 2408, 2478, 2617)
            MarathonSources.SwissCity -> logger.printProgress(source, blankLines, 1465, 1339, 1413, 1328)
            MarathonSources.Ogden -> logger.printProgress(source, blankLines, 2530, 2144, 1559, 1069)
            MarathonSources.Dubai -> logger.printProgress(source, blankLines, 2158, 1970, 2030, (1476 + 438))
            MarathonSources.Frankfurt -> logger.printProgress(source, blankLines, 11124, 11176, 11883, 11140)
            MarathonSources.Athens -> logger.printProgress(source, blankLines, 10480, 11881, 13779, 14740)
            MarathonSources.Rotterdam -> logger.printProgress(source, blankLines, 10677, 11882, 12814, 13061)
            MarathonSources.Capetown -> logger.printProgress(source, blankLines, 3387, 4300, 6197, (25 * 439 + 7))
            MarathonSources.Edinburgh -> logger.printProgress(source, blankLines, 8612, 7180, 6596, 6125)
            MarathonSources.Taipei -> logger.printProgress(source, blankLines, 5317, 4668, 5560, 5988)
            MarathonSources.GreaterManchester -> logger.printProgress(source, blankLines, 5908, 7850, 9342, 8692)
            MarathonSources.Luxemburg -> logger.printProgress(source, blankLines, 1196, 1136, 1173, 1139)
            MarathonSources.Tallinn -> logger.printProgress(source, blankLines, 1807, 1817, 1778, 1779)
            MarathonSources.Pisa -> logger.printProgress(source, blankLines, 1041, 1296, 1392, 1279)
            MarathonSources.Hannover -> logger.printProgress(source, blankLines, 1728, 1808, 1860, 1961)
            MarathonSources.Brussels -> logger.printProgress(source, blankLines, 1883, 2007, 1429, 1438)
            MarathonSources.Toronto -> logger.printProgress(source, blankLines, 3966, 3748, 3703, 3959)
            MarathonSources.Sydney -> logger.printProgress(source, blankLines, 3236, 3273, 3476, 3568)
            MarathonSources.VolkswagenPrague -> logger.printProgress(source, blankLines, 6036, 5874, 5776, 6510)
            MarathonSources.Yorkshire -> logger.printProgress(source, blankLines, 3585, 3846, 3826, 4139)
            MarathonSources.Ikano -> logger.printProgress(source, blankLines, 1495, 1238, 1192, 1101)
            MarathonSources.Steamtown -> logger.printProgress(source, blankLines, 2183, 2227, 1712, 1421)
            MarathonSources.DesMoines -> logger.printProgress(source, blankLines, 1580, 1392, 1372, 1130)
            MarathonSources.AirForce -> logger.printProgress(source, blankLines, 2912, 2156, 2042, 1265)
            MarathonSources.Ralaeigh -> logger.printProgress(source, blankLines, 1615, 952, 965, 799)
            MarathonSources.CountryMusicFestival -> logger.printProgress(source, blankLines, 3092, 2629, 2936, 2383)
            MarathonSources.StGeorge -> logger.printProgress(source, blankLines, 5799, 5462, 5417, 4720)
            MarathonSources.KansasCity -> logger.printProgress(source, blankLines,1340, 1295, 1250, 1257)
            MarathonSources.Baltimore -> logger.printProgress(source, blankLines, 2764, 2504, 2354, 2112)
            MarathonSources.GrandRapids -> logger.printProgress(source, blankLines, 1395, 1328, 1196, 997)
            MarathonSources.RockNRollSavannah -> logger.printProgress(source, blankLines, 2798, 1249, 2225, 1777)
            MarathonSources.AnthemRichmond -> logger.printProgress(source, blankLines, 5093, 4509, 4056, 4238)
            MarathonSources.TobaccoRoad -> logger.printProgress(source, blankLines, 1096, 1144, 977, 865)
            MarathonSources.RockNRollUSA -> logger.printProgress(source, blankLines, 2722, 2499, 2306, 2371)
            MarathonSources.Madison -> logger.printProgress(source, blankLines, 1272, 978, 996, 1063)
            MarathonSources.Victoria -> logger.printProgress(source, blankLines, 1570, 1200, 1095, 1067)
            MarathonSources.QuebecCity -> logger.printProgress(source, blankLines, 1276, 1098, 1059, 1028)
            MarathonSources.KiawahIsland -> logger.printProgress(source, blankLines, 1017, 1105, 1025, 1034)
            MarathonSources.SantaRose -> logger.printProgress(source, blankLines, 1234, 1440, 1276, 1062)
            MarathonSources.Baystate -> logger.printProgress(source, blankLines, 1328, 1419, 1329, 1328)
            MarathonSources.Canberra -> logger.printProgress(source, blankLines, 1217, 1077, 1105, 1006)
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