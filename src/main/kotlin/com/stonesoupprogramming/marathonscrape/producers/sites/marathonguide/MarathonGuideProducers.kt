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

private val standardColumnPositions = MergedAgedGenderColumPositions(nationality = 6, finishTime = 5, place = 2, ageGender = 0)

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