package com.stonesoupprogramming.marathonscrape

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.extension.toMarathonSources
import com.stonesoupprogramming.marathonscrape.extension.writeToCsv
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.RunnerData
import com.stonesoupprogramming.marathonscrape.producers.AbstractBaseProducer
import com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.races.*
import com.stonesoupprogramming.marathonscrape.producers.sites.endunet.TurinProducer
import com.stonesoupprogramming.marathonscrape.producers.sites.marathonguide.*
import com.stonesoupprogramming.marathonscrape.producers.sites.races.*
import com.stonesoupprogramming.marathonscrape.producers.sites.tdslive.FlorenceProducer
import com.stonesoupprogramming.marathonscrape.producers.sites.tdslive.RomeProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.repository.ResultsRepository
import com.stonesoupprogramming.marathonscrape.repository.RunnerDataRepository
import com.stonesoupprogramming.marathonscrape.service.StatusReporterService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue

@Configuration
@EnableAsync(proxyTargetClass = true)
class Configuration {

    @Bean
    fun runnerDataQueue() = LinkedBlockingQueue<RunnerData>()

    @Bean
    fun asyncExecute(): ThreadPoolTaskExecutor {
        with(ThreadPoolTaskExecutor()) {

            corePoolSize = if (Runtime.getRuntime().availableProcessors() > 4) {
                Runtime.getRuntime().availableProcessors()
            } else {
                4
            }
            setThreadNamePrefix("Marathon-Scraper-")
            initialize()
            return this
        }
    }

    @Bean
    fun canadaProvinceCodes() = listOf("AB", "BC", "MB", "NB", "NL", "NS", "NT", "NU", "ON", "PE", "QC", "SK", "YT")

    @Bean
    fun usStateCodes() = listOf("AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "DC", "FL", "GA", "HI",
            "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH",
            "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA",
            "WV", "WI", "WY", "AS", "GU", "MH", "FM", "MP", "PW", "PR", "VI")

    @Bean
    fun producers(
            @Autowired ralaighProducer: RalaighProducer,
            @Autowired canberraProducer: CanberraProducer,
            @Autowired chesterProducer: ChesterProducer,
            @Autowired snowdoniaProducer: SnowdoniaProducer,
            @Autowired californiaProducer: CaliforniaProducer,
            @Autowired rocketCityProducer: RocketCityProducer,
            @Autowired dallasProducer: DallasProducer,
            @Autowired charlestonProducer: CharlestonProducer,
            @Autowired carlsbadProducer: CarlsbadProducer,
            @Autowired newOrleansProducer: NewOrleansProducer,
            @Autowired woodlandsProducer: WoodlandsProducer,
            @Autowired phoenixProducer: PhoenixProducer,
            @Autowired napaValleyProducer: NapaValleyProducer,
            @Autowired illinoisProducer: IllinoisProducer,
            @Autowired oklahomaCityProducer: OklahomaCityProducer,
            @Autowired glassCityProducer: GlassCityProducer,
            @Autowired rotoruaProducer: RotoruaProducer,
            @Autowired pittsburgProducer: PittsburgProducer,
            @Autowired ocProducer: OCProducer,
            @Autowired seattleProducer: SeattleProducer,
            @Autowired miamiProducer: MiamiProducer,
            @Autowired portlandProducer: PortlandProducer,
            @Autowired lincolnProducer: LincolnProducer,
            @Autowired coxSportsProducer: CoxSportsProducer,
            @Autowired miltonKeynesProducer: MiltonKeynesProducer,
            @Autowired burlingtonProducer: BurlingtonProducer,
            @Autowired mountainsToBeachProducer: MountainsToBeachProducer,
            @Autowired chaingMaiProducer: ChaingMaiProducer,
            @Autowired corkCityProducer: CorkCityProducer,
            @Autowired utahValleyProducer: UtahValleyProducer,
            @Autowired missoulaProducer: MissoulaProducer,
            @Autowired erieProducer: ErieProducer,
            @Autowired seamTownProducer: SeamTownProducer,
            @Autowired mohawkHudsonRiverProducer: MohawkHudsonRiverProducer,
            @Autowired stLouisProducer: StLouisProducerNumbered,
            @Autowired longBeachProducer: LongBeachProducerNumbered,
            @Autowired poweradeMonterreyProducer: PoweradeMonterreyProducerNumbered,
            @Autowired philadelphiaProducer: PhiladelphiaProducer,
            @Autowired istanbulProducer: IstanbulProducerNumbered,
            @Autowired pfChangsArizonaProducer: PfChangsArizonaProducerNumbered,
            @Autowired helsinkiProducer: HelsinkiProducerNumbered,
            @Autowired berlinProducer: BerlinProducer,
            @Autowired maritzburgProducer: MaritzburgProducerNumbered,
            @Autowired milwaukeeProducer: MilwaukeeProducerNumbered,
            @Autowired myrtleBeachProducer: MyrtleBeachProducerNumbered,
            @Autowired belfastProducer: BelfastProducerNumbered,
            @Autowired nordeaRigaProducer: NordeaRigaProducerNumbered,
            @Autowired rockRollLasVegasProducer: RockRollLasVegasProducerNumbered,
            @Autowired cottonwoodProducer: CottonwoodProducerNumbered,
            @Autowired baystateProducer: BaystateProducer,
            @Autowired santaRoseProducer: SantaRoseProducer,
            @Autowired kiawahIslandProducer: KiawahIslandProducer,
            @Autowired quebecCityProducer: QuebecCityProducer,
            @Autowired victoriaProducer: VictoriaProducer,
            @Autowired madisonProducer: MadisonProducer,
            @Autowired rockNRollUsaProducer: RockNRollUsaProducer,
            @Autowired tobaccoRoadProducer: TobaccoRoadProducer,
            @Autowired anthemRichmondProducer: AnthemRichmondProducer,
            @Autowired rockNRollSavannahProducer: RockNRollSavannahProducer,
            @Autowired grandRapidsProducer: GrandRapidsProducer,
            @Autowired baltimoreProducer: BaltimoreProducer,
            @Autowired kansasCityProducer: KansasCityProducer,
            @Autowired stGeorgeProducer: StGeorgeProducer,
            @Autowired countryMusicProducer: CountryMusicProducer,
            @Autowired airForceProducer: AirForceProducer,
            @Autowired desMoinesProducer: DesMoinesProducer,
            @Autowired steamtownProducer: SteamtownProducer,
            @Autowired ikanoProducer: IkanoProducer,
            @Autowired yorkshireProducer: YorkshireProducer,
            @Autowired volkswagePragueProducer: VolkswagePragueProducer,
            @Autowired sydneyProducer: SydneyProducer,
            @Autowired torontoProducer: TorontoProducer,
            @Autowired brusselsProducer: BrusselsProducer,
            @Autowired hannoverProducer: HannoverProducer,
            @Autowired pisaProducer: PisaProducer,
            @Autowired tallinnProducer: TallinnProducer,
            @Autowired luxemburgProducer: LuxemburgProducer,
            @Autowired greaterManchesterProducer: GreaterManchesterProducer,
            @Autowired taipeiProducer: TaipeiProducer,
            @Autowired edinburghProducer: EdinburghProducer,
            @Autowired capetownProducer: CapetownProducer,
            @Autowired rotterdamProducer: RotterdamProducer,
            @Autowired athensProducer: AthensProducer,
            @Autowired frankfurtProducer: FrankfurtProducer,
            @Autowired dubaiProducer: DubaiProducer,
            @Autowired odgenProducer: OdgenProducer,
            @Autowired swissCityProducer: SwissCityProducer,
            @Autowired baxtersLochnessProducer: BaxtersLochnessProducer,
            @Autowired portoEdpProducer: PortoEdpProducer,
            @Autowired eindhovenProducer: EindhovenProducer,
            @Autowired whiteKnightProducer: WhiteKnightProducer,
            @Autowired aucklandProducer: AucklandProducer,
            @Autowired singaporeProducer: SignaporeProducer,
            @Autowired antwerpProducer: AntwerpProducer,
            @Autowired hamburgProducer: HamburgProducer,
            @Autowired jungfrauProducer: JungfrauProducer,
            @Autowired alexanderTheGreatProducer: AlexanderTheGreatProducer,
            @Autowired dresdenProducer: DresdenProducer,
            @Autowired hcaProducer: HcaProducer,
            @Autowired milanoProducer: MilanoProducer,
            @Autowired freiburgProducer: FreiburgProducer,
            @Autowired sanSebastianProducer: SanSebastianProducer,
            @Autowired columbusProducer: ColumbusProducer,
            @Autowired cracoviaProducer: CracoviaProducer,
            @Autowired kaiserProducer: KaiserProducer,
            @Autowired ottawaProducer: OttawaProducer,
            @Autowired barcelonaProducer: BarcelonaProducer,
            @Autowired turinProducer: TurinProducer,
            @Autowired londonProducer: LondonProducer,
            @Autowired padovaProducer: PadovaProducer,
            @Autowired dublinProducer: DublinProducer,
            @Autowired ljubljanskiProducer: LjubljanskiProducer,
            @Autowired osloProducer: OsloProducer,
            @Autowired romeProducer: RomeProducer,
            @Autowired trevisoProducer: TrevisoProducer,
            @Autowired pkoProducer: PkoProducer,
            @Autowired florenceProducer: FlorenceProducer,
            @Autowired ergebnisProducer: ErgebnisProducer,
            @Autowired taipeiStandardCharteredProducer: TaipeiStandardCharteredProducer,
            @Autowired kinmenProducer: KinmenProducer,
            @Autowired telAvivProducer: TelAvivProducer,
            @Autowired maineProducer: MaineProducer,
            @Autowired georgiaProducer: GeorgiaProducer,
            @Autowired saltLakeCityProducer: SaltLakeCityProducer,
            @Autowired riteAidClevelandProducer: RiteAidClevelandProducer,
            @Autowired niagraFallsProducer: NiagraFallsProducer,
            @Autowired venturaProducer: VenturaProducer,
            @Autowired charlottesThunderRoadProducer: CharlottesThunderRoadProducer,
            @Autowired revelCanyonCityProducer: RevelCanyonCityProducer,
            @Autowired roadToHopeProducer: RoadToHopeProducer,
            @Autowired outerBanksProducer: OuterBanksProducer,
            @Autowired rehobothProducer: RehobothProducer,
            @Autowired breastCancerProducer: BreastCancerProducer,
            @Autowired mercedesProducer: MercedesProducer,
            @Autowired mayorsProducer: MayorsProducer,
            @Autowired poznanProducer: PoznanProducer

    ): Map<MarathonSources, AbstractBaseProducer> =

            mapOf(
                    MarathonSources.Ponzan to poznanProducer,
                    MarathonSources.Kinmen to kinmenProducer,
                    MarathonSources.Mayors to mayorsProducer,
                    MarathonSources.Mercedes to mercedesProducer,
                    MarathonSources.BreastCancer to breastCancerProducer,
                    MarathonSources.Rehobath to rehobothProducer,
                    MarathonSources.OuterBanks to outerBanksProducer,
                    MarathonSources.RoadToHopeHamilton to roadToHopeProducer,
                    MarathonSources.RevelCanyonCity to revelCanyonCityProducer,
                    MarathonSources.CharlottesThunderRoad to charlottesThunderRoadProducer,
                    MarathonSources.Ventura to venturaProducer,
                    MarathonSources.NiagraFalls to niagraFallsProducer,
                    MarathonSources.RiteAidCleveland to riteAidClevelandProducer,
                    MarathonSources.SaltLakeCity to saltLakeCityProducer,
                    MarathonSources.Georgia to georgiaProducer,
                    MarathonSources.Maine to maineProducer,
                    MarathonSources.TelAviv to telAvivProducer,
                    MarathonSources.TaipeiStandardChartered to taipeiStandardCharteredProducer,
                    MarathonSources.Florence to florenceProducer,
                    MarathonSources.Ergebnis to ergebnisProducer,
                    MarathonSources.PKO to pkoProducer,
                    MarathonSources.Treviso to trevisoProducer,
                    MarathonSources.Rome to romeProducer,
                    MarathonSources.Oslo to osloProducer,
                    MarathonSources.Ljubljanski to ljubljanskiProducer,
                    MarathonSources.Dublin to dublinProducer,
                    MarathonSources.Padova to padovaProducer,
                    MarathonSources.Turin to turinProducer,
                    MarathonSources.London to londonProducer,
                    MarathonSources.Barcelona to barcelonaProducer,
                    MarathonSources.Ottawa to ottawaProducer,
                    MarathonSources.Kaiser to kaiserProducer,
                    MarathonSources.Cracovia to cracoviaProducer,
                    MarathonSources.Columbus to columbusProducer,
                    MarathonSources.SanSebastian to sanSebastianProducer,
                    MarathonSources.Freiburg to freiburgProducer,
                    MarathonSources.Milano to milanoProducer,
                    MarathonSources.Hca to hcaProducer,
                    MarathonSources.Dresden to dresdenProducer,
                    MarathonSources.Axexander to alexanderTheGreatProducer,
                    MarathonSources.Jungfrau to jungfrauProducer,
                    MarathonSources.Hamburg to hamburgProducer,
                    MarathonSources.Antwerp to antwerpProducer,
                    MarathonSources.Singapore to singaporeProducer,
                    MarathonSources.Auckland to aucklandProducer,
                    MarathonSources.WhiteKnightInternational to whiteKnightProducer,
                    MarathonSources.Eindhoven to eindhovenProducer,
                    MarathonSources.EdpPorto to portoEdpProducer,
                    MarathonSources.BaxtersLochNess to baxtersLochnessProducer,
                    MarathonSources.SwissCity to swissCityProducer,
                    MarathonSources.Ogden to odgenProducer,
                    MarathonSources.Dubai to dubaiProducer,
                    MarathonSources.Frankfurt to frankfurtProducer,
                    MarathonSources.Athens to athensProducer,
                    MarathonSources.Rotterdam to rotterdamProducer,
                    MarathonSources.Capetown to capetownProducer,
                    MarathonSources.Edinburgh to edinburghProducer,
                    MarathonSources.Taipei to taipeiProducer,
                    MarathonSources.GreaterManchester to greaterManchesterProducer,
                    MarathonSources.Luxemburg to luxemburgProducer,
                    MarathonSources.Tallinn to tallinnProducer,
                    MarathonSources.Pisa to pisaProducer,
                    MarathonSources.Hannover to hannoverProducer,
                    MarathonSources.Brussels to brusselsProducer,
                    MarathonSources.Toronto to torontoProducer,
                    MarathonSources.Sydney to sydneyProducer,
                    MarathonSources.VolkswagenPrague to volkswagePragueProducer,
                    MarathonSources.Yorkshire to yorkshireProducer,
                    MarathonSources.Ikano to ikanoProducer,
                    MarathonSources.Steamtown to steamtownProducer,
                    MarathonSources.DesMoines to desMoinesProducer,
                    MarathonSources.AirForce to airForceProducer,
                    MarathonSources.Ralaeigh to ralaighProducer,
                    MarathonSources.CountryMusicFestival to countryMusicProducer,
                    MarathonSources.StGeorge to stGeorgeProducer,
                    MarathonSources.KansasCity to kansasCityProducer,
                    MarathonSources.Baltimore to baltimoreProducer,
                    MarathonSources.GrandRapids to grandRapidsProducer,
                    MarathonSources.RockNRollSavannah to rockNRollSavannahProducer,
                    MarathonSources.AnthemRichmond to anthemRichmondProducer,
                    MarathonSources.TobaccoRoad to tobaccoRoadProducer,
                    MarathonSources.RockNRollUSA to rockNRollUsaProducer,
                    MarathonSources.Madison to madisonProducer,
                    MarathonSources.Victoria to victoriaProducer,
                    MarathonSources.QuebecCity to quebecCityProducer,
                    MarathonSources.KiawahIsland to kiawahIslandProducer,
                    MarathonSources.SantaRose to santaRoseProducer,
                    MarathonSources.Baystate to baystateProducer,
                    MarathonSources.Canberra to canberraProducer,
                    MarathonSources.Chester to chesterProducer,
                    MarathonSources.Snowdonia to snowdoniaProducer,
                    MarathonSources.California to californiaProducer,
                    MarathonSources.RocketCity to rocketCityProducer,
                    MarathonSources.Dallas to dallasProducer,
                    MarathonSources.Charleston to charlestonProducer,
                    MarathonSources.Carlsbad to carlsbadProducer,
                    MarathonSources.NewOrleans to newOrleansProducer,
                    MarathonSources.Woodlands to woodlandsProducer,
                    MarathonSources.Phoenix to phoenixProducer,
                    MarathonSources.NapaValley to napaValleyProducer,
                    MarathonSources.Illinois to illinoisProducer,
                    MarathonSources.OklahomaCity to oklahomaCityProducer,
                    MarathonSources.GlassCity to glassCityProducer,
                    MarathonSources.Rotorua to rotoruaProducer,
                    MarathonSources.Pittsburgh to pittsburgProducer,
                    MarathonSources.OC to ocProducer,
                    MarathonSources.Seattle to seattleProducer,
                    MarathonSources.Miami to miamiProducer,
                    MarathonSources.Portland to portlandProducer,
                    MarathonSources.Lincoln to lincolnProducer,
                    MarathonSources.CoxSports to coxSportsProducer,
                    MarathonSources.MiltonKeynes to miltonKeynesProducer,
                    MarathonSources.Burlington to burlingtonProducer,
                    MarathonSources.MountainsToBeach to mountainsToBeachProducer,
                    MarathonSources.ChiangMai to chaingMaiProducer,
                    MarathonSources.CorkCity to corkCityProducer,
                    MarathonSources.UtahValley to utahValleyProducer,
                    MarathonSources.Missoula to missoulaProducer,
                    MarathonSources.Erie to erieProducer,
                    MarathonSources.Seamtown to seamTownProducer,
                    MarathonSources.Mohawk to mohawkHudsonRiverProducer,
                    MarathonSources.StLouis to stLouisProducer,
                    MarathonSources.LongBeach to longBeachProducer,
                    MarathonSources.PoweradeMonterrery to poweradeMonterreyProducer,
                    MarathonSources.Philadelphia to philadelphiaProducer,
                    MarathonSources.Istanbul to istanbulProducer,
                    MarathonSources.Milwaukee to milwaukeeProducer,
                    MarathonSources.PfChangsArizona to pfChangsArizonaProducer,
                    MarathonSources.RockRollLasVegas to rockRollLasVegasProducer,
                    MarathonSources.NoredaRiga to nordeaRigaProducer,
                    MarathonSources.Cottonwood to cottonwoodProducer,
                    MarathonSources.Berlin to berlinProducer,
                    MarathonSources.Maritzburg to maritzburgProducer,
                    MarathonSources.Helsinki to helsinkiProducer,
                    MarathonSources.MyrtleBeach to myrtleBeachProducer,
                    MarathonSources.Belfast to belfastProducer)

}

@SpringBootApplication
class MarathonScrapeApplication

fun main(args: Array<String>) {
    runApplication<MarathonScrapeApplication>(*args)
}

@Component
class Application(
        @Autowired private val applicationContext: ApplicationContext,
        @Autowired private val runnerDataRepository: RunnerDataRepository,
        @Autowired private val statusReporterService: StatusReporterService,
        @Autowired private val resultsRepository: ResultsRepository<ResultsPage>,
        @Autowired private val numberedResultsPageRepository: NumberedResultsPageRepository,
        @Autowired private val producers: Map<MarathonSources, AbstractBaseProducer>) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(Application::class.java)

    override fun run(vararg args: String) {
        if(args.contains("--purge")){
            doPurge(args.toMarathonSources().filterNotNull())
        }
        if(args.contains("--list")){
            val races = runnerDataRepository.queryDistinctCategories(MarathonSources.Ergebnis)
            races.forEach { race ->
                val results = mutableListOf<RunnerData>()
                val results2014 = runnerDataRepository.findByMarathonYearAndSourceAndCompanyOrderByAge(2014, MarathonSources.Ergebnis, race)
                val results2015 = runnerDataRepository.findByMarathonYearAndSourceAndCompanyOrderByAge(2015, MarathonSources.Ergebnis, race)
                val results2016 = runnerDataRepository.findByMarathonYearAndSourceAndCompanyOrderByAge(2016, MarathonSources.Ergebnis, race)
                val results2017 = runnerDataRepository.findByMarathonYearAndSourceAndCompanyOrderByAge(2017, MarathonSources.Ergebnis, race)

                with(results) {
                    addAll(results2014)
                    addAll(results2015)
                    addAll(results2016)
                    addAll(results2017)
                }
                if(results2014.isNotEmpty() && results2015.isNotEmpty() && results2016.isNotEmpty() && results2017.isNotEmpty()){
                    println("$race, ${results.size}")
                }
            }
            System.exit(0)
        }

        statusReporterService.reportBulkStatusAsync(args.toMarathonSources().filterNotNull())

        process(*args)

        this.statusReporterService.shutdown = true

        writeCompleted(*args)

        logger.info("Exiting...")

        statusReporterService.reportBulkStatus(args.toMarathonSources().filterNotNull())

        SpringApplication.exit(applicationContext, ExitCodeGenerator { 0 })
    }

    private fun doPurge(sources: List<MarathonSources>) {
        println()

        sources.forEach {source ->
            println("Delete all $source? (y/n)?")
            val answer = readLine()
            answer?.let { a->
                if(a.toLowerCase() == "y"){
                    val pages = resultsRepository.findBySource(source)
                    resultsRepository.deleteAll(pages)

                    val numberedPages = numberedResultsPageRepository.findBySource(source)
                    numberedResultsPageRepository.deleteAll(numberedPages)
                    println("deleted $source")

                    //We have to exit since all of the producer beans configure themselves on startup and their data
                    //is now invalid
                    System.exit(0) //FIXME: Exit normally
                } else {
                    println("Source has not been deleted")
                }
            }
        }
    }

    private fun process(vararg args: String) {
        val threads = mutableListOf<CompletableFuture<String>>()
        args.toMarathonSources().forEach { it ->
            it?.let {
                producers[it]?.let { producer ->
                    threads.addAll(producer.process())
                }
            }
        }

        CompletableFuture.allOf(*threads.toTypedArray()).join()
        logger.info("Finished Web Scraping")
    }

    private fun writeCompleted(vararg args: String) {
        args.toMarathonSources().forEach { it ->
            it?.let {
                writeFile(it, it.startYear, it.endYear)
            }
        }
    }

    private fun writeFile(source: MarathonSources, startYear: Int, endYear: Int) {
        logger.info("Starting file export...")

        when(source){
            MarathonSources.Ergebnis ->{
                val races = runnerDataRepository.queryDistinctCategories(source)
                races.forEach { race ->
                    try {
                        Files.createDirectory(Paths.get("csv", race))
                        logger.info("created folder for $race")
                    } catch (e : Exception){
                        //ignore
                    }
                    for (i in startYear..endYear) {
                        runnerDataRepository.findByMarathonYearAndSourceAndCompanyOrderByAge(i, source, race).writeToCsv("csv/$race/$race-$i.csv")
                        logger.info("Wrote $race")
                    }
                }
            }
            else -> {
                try{
                    Files.createDirectory(Paths.get("csv", source.name))
                    logger.info("Created folder csv/${source.name}")
                } catch (e : Exception) {}

                for (i in startYear..endYear) {
                    runnerDataRepository.findByMarathonYearAndSourceOrderByAge(i, source).writeToCsv("csv/${source.name}/$source-$i.csv")
                }
            }
        }
        logger.info("Finished file export...")
    }
}