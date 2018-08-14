package com.stonesoupprogramming.marathonscrape

import org.openqa.selenium.chrome.ChromeDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.LinkedBlockingQueue

@Configuration
@EnableAsync(proxyTargetClass = true)
class Configuration {

    @Bean
    fun jquery () = BufferedReader(InputStreamReader(Configuration::class.java.getResourceAsStream("/js/jquery-3.3.1.js"))).readText()

    @Bean
    fun asyncExecute () : ThreadPoolTaskExecutor {
        with (ThreadPoolTaskExecutor()){
            corePoolSize = Runtime.getRuntime().availableProcessors()
            maxPoolSize = Runtime.getRuntime().availableProcessors()
            setQueueCapacity(500)
            setThreadNamePrefix("Automotive-CMS")
            initialize()
            return this
        }
    }

    @Bean
    fun nyScrapers(@Autowired first: NyWebScraper,
                   @Autowired second: NyWebScraper,
                   @Autowired third: NyWebScraper,
                   @Autowired fourth: NyWebScraper): List<WebScraper> {
        return listOf(first, second, third, fourth)
    }

    @Bean
    fun stateCodes () = listOf("AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "DC", "FL", "GA", "HI",
            "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH",
            "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA",
            "WV", "WI", "WY", "AS", "GU", "MH", "FM", "MP", "PW", "PR", "VI")

    @Bean
    fun consumers(@Autowired first : RunnerDataConsumer,
                    @Autowired second: RunnerDataConsumer,
                    @Autowired third: RunnerDataConsumer,
                    @Autowired fourth: RunnerDataConsumer) = listOf(first, second, third, first)

}

@SpringBootApplication
class MarathonScrapeApplication

fun main(args: Array<String>) {
    runApplication<MarathonScrapeApplication>(*args)
}

@Component
class Application(
        @Autowired private val runnerDataRepository: RunnerDataRepository,
        @Autowired @Qualifier("nyScrapers") private val nyScrapers: List<WebScraper>,
        @Autowired @Qualifier("berlinMarathonScraper") private val berlinMarathonScraper: WebScraper,
        @Autowired @Qualifier("viennaMarathonScrape") private val viennaMarathonScrape: WebScraper,
        @Autowired @Qualifier("bostonMarathonScrape") private val bostonMarathonScrape: WebScraper,
        @Autowired @Qualifier("chicagoMarathonScrape") private val chicagoMarathonScrape: ChicagoMarathonScrape,
        @Autowired private val nyMarathonGuide: NyMarathonGuide,
        @Autowired private val laMarathonScrape: LaMarathonScrape,
        @Autowired private val runnerDataConsumer: RunnerDataConsumer) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(Application::class.java)

    override fun run(vararg args: String) {
        val queue = LinkedBlockingQueue<RunnerData>()

        if(args.contains("--NYRR")){
            nyScrapers[0].scrape(ChromeDriver(), queue,2014, "https://results.nyrr.org/event/M2014/finishers")
            runnerDataConsumer.insertValues(queue)
            Thread.sleep(10000)

            nyScrapers[1].scrape(ChromeDriver(), queue, 2015, "https://results.nyrr.org/event/M2015/finishers")
            runnerDataConsumer.insertValues(queue)
            Thread.sleep(10000)

            nyScrapers[2].scrape(ChromeDriver(), queue, 2016, "https://results.nyrr.org/event/M2016/finishers")
            runnerDataConsumer.insertValues(queue)
            Thread.sleep(10000)

            nyScrapers[3].scrape(ChromeDriver(), queue, 2017, "https://results.nyrr.org/event/M2017/finishers")
            runnerDataConsumer.insertValues(queue)
        }
        if(args.contains("--Write-NYRR-CSV")){
            writeFile(Sources.NY, 2014, 2017)
        }
        if(args.contains("--Berlin-Marathon-Scrape")){
            val url = "https://www.bmw-berlin-marathon.com/en/facts-and-figures/results-archive.html"
            berlinMarathonScraper.scrape(ChromeDriver(), queue, 2014, url)
            runnerDataConsumer.insertValues(queue)
        }
        if(args.contains("--Write-Berlin-Marathon-Scrape")) {
            writeFile(Sources.BERLIN, 2014, 2017)
        }
        if(args.contains("--Vienna-City-Marathon-Scrape")){
            viennaMarathonScrape.scrape(ChromeDriver(), queue, 2014, "https://www.vienna-marathon.com/?surl=cd162e16e318d263fd56d6261673fe72#goto-result")
            runnerDataConsumer.insertValues(queue)
        }
        if(args.contains("--Write-Vienna-City-Marathon")){
            writeFile(Sources.VIENNA, 2014, 2018)
        }
        if(args.contains("--Boston-Marathon-Scrape")){
            bostonMarathonScrape.scrape(ChromeDriver(), queue, 2014, "http://registration.baa.org/cfm_Archive/iframe_ArchiveSearch.cfm")
            runnerDataConsumer.insertValues(queue)
        }
        if(args.contains("--Write-Boston-Marathon")){
            writeFile(Sources.BOSTON, 2014, 2018)
        }
        if(args.contains("--Chicago-Marathon-Scrape")){
            chicagoMarathonScrape.scrape(ChromeDriver(), queue, 2014, "http://chicago-history.r.mikatiming.de/2015/")
            Thread.sleep(1000)
            chicagoMarathonScrape.scrape2017(ChromeDriver(), queue)
            runnerDataConsumer.insertValues(queue)
        }
        if(args.contains("--Write-Chicago-Marathon")){
            writeFile(Sources.CHICAGO, 2014, 2017)
        }
        if(args.contains("--Scrape-Ny-Marathon")){
            val urls = mapOf(2014 to "http://www.marathonguide.com/results/browse.cfm?MIDD=472141102",
                    2015 to "http://www.marathonguide.com/results/browse.cfm?MIDD=472151101",
                    2016 to "http://www.marathonguide.com/results/browse.cfm?MIDD=472161106",
                    2017 to "http://www.marathonguide.com/results/browse.cfm?MIDD=472171105")
            urls.forEach{ it ->
                nyMarathonGuide.scrape(ChromeDriver(), queue, it.key, it.value)
                Thread.sleep(5000)
            }
            runnerDataConsumer.insertValues(queue)
        }
        if(args.contains("--Write-Ny-Marathon")){
            writeFile(Sources.NY_MARATHON_GUIDE, 2014, 2017)
        }
        if(args.contains("--Scrape-LA-Marathon")){
            val mens2015 = listOf("https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=D&Ind=2",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=DA&Ind=3",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=E&Ind=4",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=F&Ind=5",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=G&Ind=6",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=H&Ind=7",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=I&Ind=8",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=J&Ind=9",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=K&Ind=10",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=L&Ind=11",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=M&Ind=12",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=MA&Ind=13",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=N&Ind=14",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=NA&Ind=15",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=NB&Ind=16")
            val womens2015 = listOf("https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=O&Ind=17",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=R&Ind=18",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=S&Ind=19",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=SA&Ind=20",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=U&Ind=21",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=V&Ind=22",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=W&Ind=23",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=X&Ind=24",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=Y&Ind=25",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=Z&Ind=26",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=ZA&Ind=27",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=ZB&Ind=28",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=ZC&Ind=29",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=ZD&Ind=30",
                    "https://www.trackshackresults.com/lamarathon/results/2015_Marathon/mar_results.php?Link=2&Type=2&Div=ZE&Ind=31")
            val mens2016 = listOf("https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=D&Ind=0",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=DA&Ind=1",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=E&Ind=2",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=F&Ind=3",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=G&Ind=4",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=H&Ind=5",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=I&Ind=6",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=J&Ind=7",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=K&Ind=8",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=L&Ind=9",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=M&Ind=10",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=MA&Ind=11",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=N&Ind=12",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=NA&Ind=13",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=NB&Ind=14")
            val womens2016 = listOf("https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=OA&Ind=15",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=R&Ind=16",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=S&Ind=17",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=SA&Ind=18",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=U&Ind=19",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=V&Ind=20",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=W&Ind=21",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=X&Ind=22",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=Y&Ind=23",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=Z&Ind=24",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=ZA&Ind=25",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=ZB&Ind=26",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=ZC&Ind=27",
                    "https://www.trackshackresults.com/lamarathon/results/2016/mar_results.php?Link=4&Type=2&Div=ZD&Ind=28")
            val mens2017 = listOf("https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=D&Ind=0",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=DA&Ind=1",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=E&Ind=2",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=F&Ind=3",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=G&Ind=4",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=H&Ind=5",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=I&Ind=6",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=J&Ind=7",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=K&Ind=8",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=L&Ind=9",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=M&Ind=10",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=MA&Ind=11",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=N&Ind=12",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=NA&Ind=13",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=NB&Ind=14")
            val womens2017 = listOf("https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=OA&Ind=15",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=R&Ind=16",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=S&Ind=17",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=SA&Ind=18",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=U&Ind=19",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=V&Ind=20",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=W&Ind=21",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=X&Ind=22",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=Y&Ind=23",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=Z&Ind=24",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=ZA&Ind=25",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=ZB&Ind=26",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=ZC&Ind=27",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=ZD&Ind=28",
                    "https://www.trackshackresults.com/lamarathon/results/2017/mar_results.php?Link=9&Type=2&Div=ZE&Ind=29")
            mens2015.forEach { it ->
                laMarathonScrape.scrape(ChromeDriver(), queue, it, 2015, "M")
                Thread.sleep(10000)
            }
            mens2016.forEach { it ->
                laMarathonScrape.scrape(ChromeDriver(), queue, it, 2016, "M")
                Thread.sleep(10000)
            }
            mens2017.forEach { it ->
                laMarathonScrape.scrape(ChromeDriver(), queue, it, 2017, "M")
                Thread.sleep(10000)
            }
            womens2015.forEach { it ->
                laMarathonScrape.scrape(ChromeDriver(), queue, it, 2015, "W")
                Thread.sleep(10000)
            }
            womens2016.forEach { it ->
                laMarathonScrape.scrape(ChromeDriver(), queue, it, 2016, "W")
                Thread.sleep(10000)
            }
            womens2017.forEach { it ->
                laMarathonScrape.scrape(ChromeDriver(), queue, it, 2017, "W")
                Thread.sleep(10000)
            }
            Thread.sleep(10000)
            laMarathonScrape.scrape2014(ChromeDriver(), queue)
            runnerDataConsumer.insertValues(queue)
        }
        if(args.contains("--Write-LA-Marathon")){
            writeFile(Sources.LA, 2014, 2017)
        }
    }

    private fun writeFile(source : String, startYear : Int, endYear : Int){
        logger.info("Starting file export...")
        for(i in startYear..endYear){
            runnerDataRepository.findByMarathonYearAndSourceOrderByAge(i, source).writeToCsv("$source-$i.csv")
        }
        logger.info("Finished file export...")
    }
}