package com.stonesoupprogramming.marathonscrape.producers.sites.marathonguide

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumPositions
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.sites.MarathonGuideInfo
import com.stonesoupprogramming.marathonscrape.repository.ResultsRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.MarathonGuideScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

private val mohawkHudsonColumnPositions = MergedAgedGenderColumPositions(nationality = 6, finishTime = 5, place = 2, ageGender = 0)

@Component
class MohawkHudsonRiverProducer(@Autowired marathonGuideScraper: MarathonGuideScraper,
                                @Autowired resultsRepository: ResultsRepository<ResultsPage>)
    : AbstractMarathonGuideProducer(marathonGuideScraper,
        resultsRepository,
        LoggerFactory.getLogger(MohawkHudsonRiverProducer::class.java),
        MarathonSources.Mohawk,
        listOf(
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1191141012", 2014, mohawkHudsonColumnPositions, 893),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1191151011", 2015, mohawkHudsonColumnPositions, 1145),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1191161009", 2016, mohawkHudsonColumnPositions, 1113),
                MarathonGuideInfo("http://www.marathonguide.com/results/browse.cfm?MIDD=1191171008", 2017, mohawkHudsonColumnPositions, 890)))