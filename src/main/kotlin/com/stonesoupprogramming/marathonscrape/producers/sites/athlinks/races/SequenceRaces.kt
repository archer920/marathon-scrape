package com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.races

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.sites.SequenceAthLinks
import com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.AbstractAthSequenceProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.AthLinksMarathonScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class HelsinkiProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                         @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(HelsinkiProducer::class.java), MarathonSources.Helsinki,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/34484/results/Event/356618/Course/522702/Results", 78),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/34484/results/Event/459769/Course/685690/Results", 71),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34484/results/Event/573818/Course/859013/Results", 55),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34484/results/Event/655614/Course/1025305/Results", 45)))

@Component
class CottonwoodProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                         @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(CottonwoodProducer::class.java), MarathonSources.Cottonwood,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/12714/results/Event/357341/Course/593145/Results", 31),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/12714/results/Event/414576/Course/624098/Results", 26),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/12714/results/Event/510240/Course/758611/Results", 27),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/12714/results/Event/603507/Course/1025926/Results", 28)))

@Component
class MaritzburgProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                         @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(MaritzburgProducer::class.java), MarathonSources.Maritzburg,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/35263/results/Event/332716/Course/1174743/Results", 46),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/35263/results/Event/412705/Course/620994/Results", 42),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/35263/results/Event/716009/Course/1172543/Results", 45),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/35263/results/Event/716017/Course/1172571/Results", 49)))

@Component
class MyrtleBeachProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                          @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(MyrtleBeachProducer::class.java), MarathonSources.MyrtleBeach,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/34968/results/Event/368045/Course/543311/Results", 33),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/34968/results/Event/418384/Course/632208/Results", 30),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34968/results/Event/525247/Course/733804/Results", 29),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34968/results/Event/622176/Course/956955/Results", 25)))

@Component
class BelfastProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                         @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(BelfastProducer::class.java), MarathonSources.Belfast,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/6631/results/Event/388932/Course/582165/Results", 47),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/6631/results/Event/450323/Course/672631/Results", 46),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/6631/results/Event/609040/Course/924274/Results", 44),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/6631/results/Event/655780/Course/1000105/Results", 43)))

@Component
class NordeaRigaProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                         @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(NordeaRigaProducer::class.java), MarathonSources.NoredaRiga,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/34500/results/Event/382345/Course/502115/Results", 25),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/34500/results/Event/472253/Course/703216/Results", 30),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34500/results/Event/636074/Course/982135/Results", 30),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34500/results/Event/641817/Course/997742/Results", 33)))

@Component
class RockRollLasVegasProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                         @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(RockRollLasVegasProducer::class.java), MarathonSources.RockRollLasVegas,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/19454/results/Event/405110/Course/608406/Results", 65),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/19454/results/Event/494340/Course/734391/Results", 63),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/19454/results/Event/514055/Course/910974/Results", 52),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/19454/results/Event/614539/Course/1120038/Results", 52)))

@Component
class PfChangsArizonaProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                               @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(PfChangsArizonaProducer::class.java), MarathonSources.PfChangsArizona,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/19453/results/Event/351162/Course/456973/Results", 58),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/19453/results/Event/413550/Course/623479/Results", 52),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/19453/results/Event/519385/Course/758201/Results", 47),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/19453/results/Event/524669/Course/780238/Results", 47)))

@Component
class IstanbulProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                              @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(IstanbulProducer::class.java), MarathonSources.Istanbul,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/34771/results/Event/328687/Course/477328/Results", 78),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/34771/results/Event/495768/Course/737457/Results", 56),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34771/results/Event/599550/Course/906321/Results", 56),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34771/results/Event/696213/Course/1129944/Results", 35)))