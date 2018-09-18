package com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.races

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.sites.SequenceAthLinks
import com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.AbstractNumberedAthSequenceProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.AthLinksMarathonScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

//@Component
//class Producer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
//                               @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
//    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
//        LoggerFactory.getLogger(::class.java),
//        MarathonSources.,
//        listOf(SequenceAthLinks(2014, "", ),
//                SequenceAthLinks(2015, "", ),
//                SequenceAthLinks(2016, "", ),
//                SequenceAthLinks(2017, "", )))

@Component
class SanSebastianProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                           @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(SanSebastianProducer::class.java),
        MarathonSources.SanSebastian,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/35432/results/Event/362430/Course/532679/Results", 59),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/35432/results/Event/499877/Course/743435/Results", 60),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/35432/results/Event/604448/Course/915740/Results", 51)))

@Component
class FreiburgProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                       @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(FreiburgProducer::class.java),
        MarathonSources.Freiburg,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/34642/results/Event/326563/Course/474929/Results", 21),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/34642/results/Event/418570/Course/629458/Results", 22),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34642/results/Event/530527/Course/791738/Results", 17),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34642/results/Event/631695/Course/971344/Results", 15)))

@Component
class MilanoProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                     @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(MilanoProducer::class.java),
        MarathonSources.Milano,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/35354/results/Event/413660/Course/622728/Results", 72),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/35354/results/Event/435857/Course/649757/Results", 81),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/35354/results/Event/532260/Course/791026/Results", 75),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/35354/results/Event/629050/Course/967148/Results", 107)))

@Component
class AucklandProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                               @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(AucklandProducer::class.java),
        MarathonSources.Auckland,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/34562/results/Event/402472/Course/604033/Results", 47),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/34562/results/Event/493687/Course/627094/Results", 31),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34562/results/Event/596963/Course/901052/Results", 33),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34562/results/Event/685227/Course/1109983/Results", 32)))

@Component
class WhiteKnightProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                          @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(WhiteKnightProducer::class.java),
        MarathonSources.WhiteKnightInternational,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/36532/results/Event/756645/Course/1283339/Results", 44),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/36532/results/Event/457594/Course/1280254/Results", 61),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/36532/results/Event/758912/Course/1290449/Results", 57),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/36532/results/Event/655342/Course/1024787/Results", 61)))

@Component
class EindhovenProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                        @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(EindhovenProducer::class.java),
        MarathonSources.Eindhoven,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/34601/results/Event/327252/Course/473385/Results", 27),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/34601/results/Event/474461/Course/706351/Results", 41),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34601/results/Event/527432/Course/892419/Results", 43),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34601/results/Event/606032/Course/1120597/Results", 43)))

@Component
class PortoEdpProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                       @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(PortoEdpProducer::class.java),
        MarathonSources.EdpPorto,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/35334/results/Event/334850/Course/487271/Results", 81),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/35334/results/Event/503020/Course/747862/Results", 89),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/35334/results/Event/528809/Course/786136/Results", 95),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/35334/results/Event/687503/Course/1256267/Results", 91)))

@Component
class BaxtersLochnessProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                              @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(BaxtersLochnessProducer::class.java),
        MarathonSources.BaxtersLochNess,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/35025/results/Event/322964/Course/522040/Results", 50),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/35025/results/Event/482780/Course/718162/Results", 49),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/35025/results/Event/590110/Course/887087/Results", 50),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/35025/results/Event/689384/Course/1115271/Results", 53)))

@Component
class SwissCityProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                        @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(SwissCityProducer::class.java),
        MarathonSources.SwissCity,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/87198/results/Event/401052/Course/531011/Results", 30),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/87198/results/Event/491038/Course/730288/Results", 27),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/87198/results/Event/587106/Course/882454/Results", 29),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/87198/results/Event/687833/Course/1112214/Results", 27)))

@Component
class FrankfurtProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                        @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(FrankfurtProducer::class.java),
        MarathonSources.Frankfurt,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/34538/results/Event/324806/Course/474657/Results", 223),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/34538/results/Event/441100/Course/660776/Results", 224),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34538/results/Event/595636/Course/898369/Results", 238),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34538/results/Event/668970/Course/1056262/Results", 223)))

@Component
class RotterdamProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                               @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(RotterdamProducer::class.java),
        MarathonSources.Rotterdam,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/34492/results/Event/323915/Course/468492/Results", 214),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/34492/results/Event/433837/Course/651542/Results", 238),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34492/results/Event/528048/Course/785131/Results", 257),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34492/results/Event/625190/Course/959528/Results", 262)))

@Component
class EdinburghProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                        @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(EdinburghProducer::class.java),
        MarathonSources.Edinburgh,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/34514/results/Event/343336/Course/568563/Results", 173),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/34514/results/Event/404856/Course/608010/Results", 144),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34514/results/Event/546690/Course/822657/Results", 132),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34514/results/Event/644023/Course/1016123/Results", 123)))

@Component
class TaipeiProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                     @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(TaipeiProducer::class.java),
        MarathonSources.Taipei,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/34450/results/Event/410756/Course/617603/Results", 107),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/34450/results/Event/512311/Course/669211/Results", 94),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34450/results/Event/704200/Course/1147895/Results", 112),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34450/results/Event/701640/Course/1142522/Results", 120)))

@Component
class GreaterManchesterProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                                @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(GreaterManchesterProducer::class.java),
        MarathonSources.GreaterManchester,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/35036/results/Event/409266/Course/615374/Results", 119),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/35036/results/Event/410243/Course/616875/Results", 157),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/35036/results/Event/496603/Course/738535/Results", 187),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/35036/results/Event/614024/Course/934861/Results", 174)))

@Component
class LuxemburgProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                        @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(LuxemburgProducer::class.java),
        MarathonSources.Luxemburg,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/170017/results/Event/328629/Course/498841/Results", 24),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/170017/results/Event/451794/Course/674608/Results", 23),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/170017/results/Event/546463/Course/812277/Results", 24),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/170017/results/Event/678583/Course/1084220/Results", 23)))

@Component
class TallinnProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                      @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(TallinnProducer::class.java),
        MarathonSources.Tallinn,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/35378/results/Event/355330/Course/520379/Results", 37),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/35378/results/Event/398029/Course/596704/Results", 37),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/35378/results/Event/580745/Course/872147/Results", 36),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/35378/results/Event/690167/Course/1116781/Results", 36)))

@Component
class PisaProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                   @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(PisaProducer::class.java),
        MarathonSources.Pisa,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/36137/results/Event/409381/Course/535186/Results", 21),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/36137/results/Event/527548/Course/784510/Results", 26),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/36137/results/Event/608325/Course/922891/Results", 28),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/36137/results/Event/700181/Course/1139089/Results", 26)))

@Component
class HannoverProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                       @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(HannoverProducer::class.java),
        MarathonSources.Hannover,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/34611/results/Event/371269/Course/498083/Results", 35),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/34611/results/Event/437824/Course/656682/Results", 37),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34611/results/Event/538909/Course/800819/Results", 38),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34611/results/Event/540665/Course/803451/Results", 40)))

@Component
class BrusselsProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                       @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(BrusselsProducer::class.java),
        MarathonSources.Brussels,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/34700/results/Event/345111/Course/503496/Results", 38),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/34700/results/Event/485004/Course/718799/Results", 41),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34700/results/Event/591042/Course/888853/Results", 29),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34700/results/Event/676077/Course/1103699/Results", 29)))

@Component
class TorontoProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                      @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(TorontoProducer::class.java),
        MarathonSources.Toronto,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/34502/results/Event/359507/Course/462020/Results", 80),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/34502/results/Event/434296/Course/724826/Results", 75),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34502/results/Event/491087/Course/730358/Results", 75),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34502/results/Event/598900/Course/1007313/Results", 80)))

@Component
class SydneyProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                     @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(SydneyProducer::class.java),
        MarathonSources.Sydney,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/34455/results/Event/398183/Course/596361/Results", 65),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/34455/results/Event/481510/Course/713155/Results", 66),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34455/results/Event/553777/Course/825408/Results", 70),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34455/results/Event/672820/Course/983302/Results", 72)))

@Component
class VolkswagePragueProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                              @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(VolkswagePragueProducer::class.java),
        MarathonSources.VolkswagenPrague,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/35394/results/Event/361776/Course/566063/Results", 121),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/35394/results/Event/439488/Course/658689/Results", 118),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/35394/results/Event/541046/Course/804079/Results", 116),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/35394/results/Event/638193/Course/987203/Results", 131)))

@Component
class HelsinkiProducerNumbered(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                               @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(HelsinkiProducerNumbered::class.java), MarathonSources.Helsinki,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/34484/results/Event/356618/Course/522702/Results", 78),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/34484/results/Event/459769/Course/685690/Results", 71),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34484/results/Event/573818/Course/859013/Results", 55),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34484/results/Event/655614/Course/1025305/Results", 45)))

@Component
class CottonwoodProducerNumbered(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                                 @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(CottonwoodProducerNumbered::class.java), MarathonSources.Cottonwood,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/12714/results/Event/357341/Course/593145/Results", 31),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/12714/results/Event/414576/Course/624098/Results", 26),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/12714/results/Event/510240/Course/758611/Results", 27),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/12714/results/Event/603507/Course/1025926/Results", 28)))

@Component
class MaritzburgProducerNumbered(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                                 @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(MaritzburgProducerNumbered::class.java), MarathonSources.Maritzburg,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/35263/results/Event/332716/Course/1174743/Results", 46),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/35263/results/Event/412705/Course/620994/Results", 42),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/35263/results/Event/716009/Course/1172543/Results", 45),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/35263/results/Event/716017/Course/1172571/Results", 49)))

@Component
class MyrtleBeachProducerNumbered(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                                  @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(MyrtleBeachProducerNumbered::class.java), MarathonSources.MyrtleBeach,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/34968/results/Event/368045/Course/543311/Results", 33),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/34968/results/Event/418384/Course/632208/Results", 30),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34968/results/Event/525247/Course/733804/Results", 29),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34968/results/Event/622176/Course/956955/Results", 25)))

@Component
class MilwaukeeProducerNumbered(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                                @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(MilwaukeeProducerNumbered::class.java), MarathonSources.Milwaukee,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/181603/results/Event/350590/Course/512405/Results", 42),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/181603/results/Event/424400/Course/638397/Results", 46),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/181603/results/Event/591199/Course/754364/Results", 41),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/181603/results/Event/603741/Course/914282/Results", 35)))

@Component
class BelfastProducerNumbered(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                              @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(BelfastProducerNumbered::class.java), MarathonSources.Belfast,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/6631/results/Event/388932/Course/582165/Results", 47),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/6631/results/Event/450323/Course/672631/Results", 46),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/6631/results/Event/609040/Course/924274/Results", 44),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/6631/results/Event/655780/Course/1000105/Results", 43)))

@Component
class NordeaRigaProducerNumbered(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                                 @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(NordeaRigaProducerNumbered::class.java), MarathonSources.NoredaRiga,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/34500/results/Event/382345/Course/502115/Results", 25),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/34500/results/Event/472253/Course/703216/Results", 30),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34500/results/Event/636074/Course/982135/Results", 30),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34500/results/Event/641817/Course/997742/Results", 33)))

@Component
class RockRollLasVegasProducerNumbered(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                                       @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(RockRollLasVegasProducerNumbered::class.java), MarathonSources.RockRollLasVegas,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/19454/results/Event/405110/Course/608406/Results", 65),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/19454/results/Event/494340/Course/734391/Results", 63),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/19454/results/Event/514055/Course/910974/Results", 52),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/19454/results/Event/614539/Course/1120038/Results", 52)))

@Component
class PfChangsArizonaProducerNumbered(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                                      @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(PfChangsArizonaProducerNumbered::class.java), MarathonSources.PfChangsArizona,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/19453/results/Event/351162/Course/456973/Results", 58),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/19453/results/Event/413550/Course/623479/Results", 52),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/19453/results/Event/519385/Course/758201/Results", 47),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/19453/results/Event/524669/Course/780238/Results", 47)))

@Component
class IstanbulProducerNumbered(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                               @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(IstanbulProducerNumbered::class.java), MarathonSources.Istanbul,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/34771/results/Event/328687/Course/477328/Results", 78),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/34771/results/Event/495768/Course/737457/Results", 56),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/34771/results/Event/599550/Course/906321/Results", 56),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/34771/results/Event/696213/Course/1129944/Results", 35)))

//NOTE: This may need to be done on a category basis
@Component
class PoweradeMonterreyProducerNumbered(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                                        @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(PoweradeMonterreyProducerNumbered::class.java), MarathonSources.PoweradeMonterrery,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/35592/results/Event/365335/Course/537650/Results", 84),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/35592/results/Event/501209/Course/745432/Results", 107),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/35592/results/Event/622256/Course/953492/Results", 125),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/35592/results/Event/688063/Course/1112757/Results", 134)))

@Component
class LongBeachProducerNumbered(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                                @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(LongBeachProducerNumbered::class.java), MarathonSources.LongBeach,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/20146/results/Event/393344/Course/513404/Results", 56),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/20146/results/Event/467463/Course/724745/Results", 47),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/20146/results/Event/591445/Course/878229/Results", 40),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/20146/results/Event/691269/Course/1119057/Results", 35)))

@Component
class StLouisProducerNumbered(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                              @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(StLouisProducerNumbered::class.java), MarathonSources.StLouis,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/4852/results/Event/312739/Course/552839/Results", 28),
                SequenceAthLinks(2015, "https://www.athlinks.com/event/4852/results/Event/435476/Course/600133/Results", 28),
                SequenceAthLinks(2016, "https://www.athlinks.com/event/4852/results/Event/506778/Course/753617/Results", 27),
                SequenceAthLinks(2017, "https://www.athlinks.com/event/4852/results/Event/632942/Course/912154/Results", 24)))

@Component
class ChaingMaiProducer(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                              @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository, LoggerFactory.getLogger(ChaingMaiProducer::class.java), MarathonSources.ChiangMai,
        listOf(SequenceAthLinks(2014, "https://www.athlinks.com/event/51019/results/Event/364480/Course/536039/Results", 17)))