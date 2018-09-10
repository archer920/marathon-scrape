package com.stonesoupprogramming.marathonscrape.producers.sites.marathonguide

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.extension.isAllCaps
import com.stonesoupprogramming.marathonscrape.extension.toNationality
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumPositions
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.sites.MarathonGuideInfo
import com.stonesoupprogramming.marathonscrape.repository.ResultsRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.MarathonGuideScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.function.Function

private val standardColumnPositions = MergedAgedGenderColumPositions(nationality = 6, finishTime = 5, place = 2, ageGender = 0, backupAge = 4)

//@Component
//class Producer(@Autowired marathonGuideScraper: MarathonGuideScraper,
//                 @Autowired resultsRepository: ResultsRepository<ResultsPage>)
//    : AbstractMarathonGuideProducer(marathonGuideScraper,
//        resultsRepository,
//        LoggerFactory.getLogger(::class.java),
//        MarathonSources.,
//        listOf(
//                MarathonGuideInfo("", 2014, standardColumnPositions, ),
//                MarathonGuideInfo("", 2015, standardColumnPositions, ),
//                MarathonGuideInfo("", 2016, standardColumnPositions, ),
//                MarathonGuideInfo("", 2017, standardColumnPositions, )))

@Component
class CanberraProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                 @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(CanberraProducer::class.java),
        MarathonSources.Canberra,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1388140413", 2014, standardColumnPositions, 1217),
                MarathonGuideInfo("", 2015, standardColumnPositions, ),
                MarathonGuideInfo("", 2016, standardColumnPositions, ),
                MarathonGuideInfo("", 2017, standardColumnPositions, )))

@Component
class ChesterProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                      @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(ChesterProducer::class.java),
        MarathonSources.Chester,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=3674141005", 2014, standardColumnPositions.copy(nationality = -1, finishTime = 5), 2468),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=3674151004", 2015, standardColumnPositions.copy(nationality = -1, finishTime = 5), 2287),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=3674161002", 2016, standardColumnPositions.copy(nationality = -1, finishTime = 5), 2155),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=3674171008", 2017, standardColumnPositions.copy(nationality = -1, finishTime = 5), 2587)))

@Component
class SnowdoniaProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                        @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(SnowdoniaProducer::class.java),
        MarathonSources.Snowdonia,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1444141025", 2014, standardColumnPositions.copy(nationality = 5, finishTime = 1), 1719),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1444151024", 2015, standardColumnPositions.copy(nationality = -1, finishTime = 5), 1846),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1444161029", 2016, standardColumnPositions.copy(nationality = -1, finishTime = 5), 2066),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1444171028", 2017, standardColumnPositions.copy(nationality = -1, finishTime = 5), 2220)))

@Component
class CaliforniaProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                         @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(CaliforniaProducer::class.java),
        MarathonSources.California,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=687141207", 2014, standardColumnPositions.copy(nationality = 5, finishTime = 1), 5777),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=687151206", 2015, standardColumnPositions.copy(nationality = 5, finishTime = 1), 5629),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=687161204", 2016, standardColumnPositions.copy(nationality = 5, finishTime = 1), 6174),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=687171203", 2017, standardColumnPositions.copy(nationality = 5, finishTime = 4, place = 1, backupAge = 3), 6543)))

@Component
class RocketCityProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                         @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(RocketCityProducer::class.java),
        MarathonSources.RocketCity,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=558141213", 2014, standardColumnPositions, 1463),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=558151212", 2015, standardColumnPositions, 1272),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=558161210", 2016, standardColumnPositions, 1028),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=558171209", 2017, standardColumnPositions.copy(nationality = 5, finishTime = 4), 897)))

@Component
class DallasProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                     @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(DallasProducer::class.java),
        MarathonSources.Dallas,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=475141214", 2014, standardColumnPositions, 3950),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=475151213", 2015, standardColumnPositions, 2747),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=475161211", 2016, standardColumnPositions, 2807),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=475171210", 2017, standardColumnPositions, 2818)))

@Component
class CharlestonProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                         @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(CharlestonProducer::class.java),
        MarathonSources.Charleston,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=3858140118", 2014, standardColumnPositions, 1185),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=3858150117", 2015, standardColumnPositions, 913),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=3858160116", 2016, standardColumnPositions, 960),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=3858170114", 2017, standardColumnPositions.copy(nationality = 5, finishTime = 4, backupAge = null), 963)))

@Component
class CarlsbadProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                       @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(CarlsbadProducer::class.java),
        MarathonSources.Carlsbad,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1013140119", 2014, standardColumnPositions, 1389),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1013150118", 2015, standardColumnPositions, 1083),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1013160117", 2016, standardColumnPositions, 1082),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1013170115", 2017, standardColumnPositions, 927)))

@Component
class NewOrleansProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                         @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(NewOrleansProducer::class.java),
        MarathonSources.NewOrleans,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=483140202", 2014, standardColumnPositions.copy(nationality = 5, finishTime = 1), 2749),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=483150125", 2015, standardColumnPositions.copy(nationality = 5, finishTime = 1), 2353),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=483160228", 2016, standardColumnPositions.copy(nationality = -1, finishTime = 1), 3625),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=483170205", 2017, standardColumnPositions.copy(nationality = -1, finishTime = 1), 2824)))

@Component
class WoodlandsProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                        @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(WoodlandsProducer::class.java),
        MarathonSources.Woodlands,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=4022140301", 2014, standardColumnPositions.copy(nationality = -1, finishTime = 4, backupAge = null), 1230),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=4022150228", 2015, standardColumnPositions.copy(nationality = 5, finishTime = 1), 894),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=4022160305", 2016, standardColumnPositions, 874),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=4022170304", 2017, standardColumnPositions, 935)))

@Component
class PhoenixProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                      @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(PhoenixProducer::class.java),
        MarathonSources.Phoenix,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=4338140301", 2014, standardColumnPositions, 1816),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=4338150228", 2015, standardColumnPositions.copy(nationality = -1), 1881),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=4338160227", 2016, standardColumnPositions.copy(nationality = -1, finishTime = 1), 2109),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=4338170225", 2017, standardColumnPositions.copy(nationality = 5, finishTime = 1), 2004)))

@Component
class NapaValleyProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                         @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(NapaValleyProducer::class.java),
        MarathonSources.NapaValley,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=492140302", 2014, standardColumnPositions.copy(nationality = 5, finishTime = 1), 1741),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=492150301", 2015, standardColumnPositions.copy(nationality = 5, finishTime = 1), 1885),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=492160306", 2016, standardColumnPositions.copy(nationality = 5, finishTime = 1), 1719),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=492170305", 2017, standardColumnPositions.copy(nationality = 4, finishTime = 1, backupAge = null), 1269)))

@Component
class IllinoisProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                       @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(IllinoisProducer::class.java),
        MarathonSources.Illinois,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=3118140426", 2014, standardColumnPositions, 1684),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=3118150425", 2015, standardColumnPositions, 1051),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=3118160430", 2016, standardColumnPositions, 1452),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=3118170422", 2017, standardColumnPositions.copy(nationality = 5, finishTime = 1), 1215)))

@Component
class OklahomaCityProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                           @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(OklahomaCityProducer::class.java),
        MarathonSources.OklahomaCity,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1538140427", 2014, standardColumnPositions.copy(nationality = 5, finishTime = 1), 2623),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1538150426", 2015, standardColumnPositions, 2489),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1538160424", 2016, standardColumnPositions.copy(nationality = 5, finishTime = 1), 2225),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1538170430", 2017, standardColumnPositions.copy(nationality = 5, finishTime = 1), 2201)))

@Component
class GlassCityProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                        @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(GlassCityProducer::class.java),
        MarathonSources.GlassCity,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=500140427", 2014, standardColumnPositions, 858),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=500150426", 2015, standardColumnPositions, 963),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=500160424", 2016, standardColumnPositions, 1146),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=500170423", 2017, standardColumnPositions.copy(nationality = 5, finishTime = 1), 1070)))

@Component
class RotoruaProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                      @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(RotoruaProducer::class.java),
        MarathonSources.Rotorua,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=977140503", 2014, standardColumnPositions.copy(nationality = -1, finishTime = 1), 3511),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=977150502", 2015, standardColumnPositions.copy(nationality = -1, finishTime = 1), 1172),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=977160430", 2016, standardColumnPositions.copy(nationality = -1, finishTime = 1), 1016),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=977170506", 2017, standardColumnPositions.copy(nationality = -1, finishTime = 1), 999)))

@Component
class PittsburgProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                        @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(PittsburgProducer::class.java),
        MarathonSources.Pittsburgh,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=3077140504", 2014, standardColumnPositions, 4500),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=3077150503", 2015, standardColumnPositions, 4210),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=3077160501", 2016, standardColumnPositions, 3681),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=3077170507", 2017, standardColumnPositions.copy(nationality = 5, finishTime = 1), 3359)))

@Component
class OCProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                 @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(OCProducer::class.java),
        MarathonSources.OC,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1908140504", 2014, standardColumnPositions, 2210),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1908150503", 2015, standardColumnPositions, 1862),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1908160501", 2016, standardColumnPositions, 1937),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1908170507", 2017, standardColumnPositions, 1489)))

@Component
class SeattleProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                      @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(SeattleProducer::class.java),
        MarathonSources.Seattle,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=474141130", 2014, standardColumnPositions.copy(nationality = 5, finishTime = 1), 1867),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=474151129", 2015, standardColumnPositions.copy(nationality = 5, finishTime = 1), 1716),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=474161127", 2016, standardColumnPositions.copy(nationality = 5, finishTime = 1), 1596),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=474171126", 2017, standardColumnPositions.copy(nationality = 5, finishTime = 1), 1439)))

@Component
class MiamiProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                    @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(MiamiProducer::class.java),
        MarathonSources.Miami,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1850140202", 2014, standardColumnPositions, 3529),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1850150125", 2015, standardColumnPositions.copy(nationality = 5, finishTime = 1), 2749),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1850160124", 2016, standardColumnPositions.copy(nationality = 5, finishTime = 1), 3061),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1850170129", 2017, standardColumnPositions.copy(nationality = 5, finishTime = 1), 2972)))

@Component
class PortlandProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                       @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(PortlandProducer::class.java),
        MarathonSources.Portland,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=38141005", 2014, standardColumnPositions, 6258),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=38151004", 2015, standardColumnPositions, 5550),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=38161009", 2016, standardColumnPositions, 4421),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=38171008", 2017, standardColumnPositions, 2912)))

@Component
class LincolnProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                      @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(LincolnProducer::class.java),
        MarathonSources.Lincoln,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=34140504", 2014, standardColumnPositions.copy(nationality = 5, finishTime = 1), 1259),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=34150503", 2015, standardColumnPositions.copy(nationality = 5, finishTime = 1), 1145),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=34160501", 2016, standardColumnPositions.copy(nationality = 5, finishTime = 1), 1062),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=34170507", 2017, standardColumnPositions.copy(nationality = 5, finishTime = 1), 1050)))

@Component
class CoxSportsProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                        @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(CoxSportsProducer::class.java),
        MarathonSources.CoxSports,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=2873140504", 2014, standardColumnPositions, 1333),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=2873150503", 2015, standardColumnPositions.copy(nationality = -1), 1105),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=2873160501", 2016, standardColumnPositions.copy(nationality = -1, finishTime = 1), 1029),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=2873170507", 2017, standardColumnPositions.copy(nationality = -1, finishTime = 1, backupAge = null), 1120)))

@Component
class MiltonKeynesProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                           @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(MiltonKeynesProducer::class.java),
        MarathonSources.MiltonKeynes,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=4193140505", 2014, standardColumnPositions.copy(nationality = -1), 2105),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=4193150504", 2015, standardColumnPositions.copy(nationality = -1), 1559),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=4193160502", 2016, standardColumnPositions.copy(nationality = -1), 1954),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=4193170501", 2017, standardColumnPositions.copy(nationality = -1), 2020)))

@Component
class BurlingtonProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                         @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(BurlingtonProducer::class.java),
        MarathonSources.Burlington,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=28140525", 2014, standardColumnPositions, 2432),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=28150524", 2015, standardColumnPositions, 2458),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=28160529", 2016, standardColumnPositions, 1894),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=28170528", 2017, standardColumnPositions, 1986)))

@Component
class MountainsToBeachProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                               @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(MountainsToBeachProducer::class.java),
        MarathonSources.MountainsToBeach,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=4718140525", 2014, standardColumnPositions.copy(nationality = -1, finishTime = 1, backupAge = 4), 1621),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=4718150524", 2015, standardColumnPositions, 1602),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=4718160529", 2016, standardColumnPositions.copy(nationality = 5, finishTime = 4, backupAge = null), 2006),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=4718170528", 2017, standardColumnPositions.copy(nationality = 5, finishTime = 4, backupAge = null), 1947)))

@Component
class MohawkHudsonRiverProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                                @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(MohawkHudsonRiverProducer::class.java),
        MarathonSources.Mohawk,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1191141012", 2014, standardColumnPositions, 893),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1191151011", 2015, standardColumnPositions, 1145),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1191161009", 2016, standardColumnPositions, 1113),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1191171008", 2017, standardColumnPositions, 890)))

@Component
class SeamTownProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                                @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(SeamTownProducer::class.java),
        MarathonSources.Seamtown,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=540141012", 2014, standardColumnPositions, 2183),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=540151011", 2015, standardColumnPositions, 2227),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=540161009", 2016, standardColumnPositions, 1712),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=540171008", 2017, standardColumnPositions.copy(nationality = 5, finishTime = 1), 1421)))

@Component
class ErieProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                       @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(ErieProducer::class.java),
        MarathonSources.Erie,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=533140914", 2014, standardColumnPositions, 956),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=533150913", 2015, standardColumnPositions, 1522),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=533160911", 2016, standardColumnPositions.copy(nationality = 5, finishTime = 1), 1382),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=533170910", 2017, standardColumnPositions.copy(nationality = 5, finishTime = 1), 1528)))

@Component
class MissoulaProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                   @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(MissoulaProducer::class.java),
        MarathonSources.Missoula,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=2627140713", 2014, standardColumnPositions, 1168),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=2627150712", 2015, standardColumnPositions, 997),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=2627160710", 2016, standardColumnPositions, 982),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=2627170709", 2017, standardColumnPositions, 868)))

val utahNationalityFunc = Function<String, String> { it ->
    var parts = it.split(" ")
    if(parts.size > 2){
        parts = parts.subList(parts.size - 2, parts.size)
    }
    if(parts.first().isAllCaps()){
        parts.joinToString(" ").toNationality()
    } else {
        parts.last().toNationality()
    }
}

@Component
class UtahValleyProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                       @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(UtahValleyProducer::class.java),
        MarathonSources.UtahValley,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=2964140614", 2014, standardColumnPositions.copy(backupAge = 4), 1409),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=2964150613", 2015, standardColumnPositions.copy(backupAge = 4), 1144),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=2964160611", 2016, standardColumnPositions.copy(backupAge = 4, nationality = -1), 954),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=2964170610", 2017, standardColumnPositions.copy(backupAge = 4, splitFunc = utahNationalityFunc), 1175)))

private val corkCityColumnPositions = MergedAgedGenderColumPositions(ageGender = 0, finishTime = 5, place = 2, nationality = -1, backupAge = 4)

@Component
class CorkCityProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                         @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(CorkCityProducer::class.java),
        MarathonSources.CorkCity,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=3278140602", 2014, corkCityColumnPositions, 524),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=3278150601", 2015, corkCityColumnPositions, 1115),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=3278160606", 2016, corkCityColumnPositions, 1110),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=3278170605", 2017, corkCityColumnPositions, 1151)))