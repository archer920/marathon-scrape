package com.stonesoupprogramming.marathonscrape.producers.sites.races

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.AgeGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.StandardScrapeInfo
import com.stonesoupprogramming.marathonscrape.producers.AbstractResultsPageProducer
import com.stonesoupprogramming.marathonscrape.repository.ResultsRepository
import com.stonesoupprogramming.marathonscrape.scrapers.StandardAgeGenderRowProcessor
import com.stonesoupprogramming.marathonscrape.scrapers.StandardWebScraperAgeGender
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

//Ergebnis
@Component
class ErgebnisProducer(@Autowired private val standardWebScraperAgeGender: StandardWebScraperAgeGender,
                       @Autowired private val pagedResultsRepository: ResultsRepository<ResultsPage>) : AbstractResultsPageProducer<ResultsPage>(pagedResultsRepository, LoggerFactory.getLogger(ErgebnisProducer::class.java), MarathonSources.Ergebnis){

    val urls2014 = Array(1107) {it -> "http://www.marathon-ergebnis.de/cgi-bin/edbDetailSucheNeu.php?Seite=${it + 1}&Total=110690&MW=&AK=1&Ergebnis=0:00:00&Jahr=2014&Nachname=&Vorname=&Verein=&VerTag=&VerOrt=&AproSeite=100"}
    val urls2015 = Array(1173) {it -> "http://www.marathon-ergebnis.de/cgi-bin/edbDetailSucheNeu.php?Seite=${it + 1}&Total=117227&MW=&AK=1&Ergebnis=0:00:00&Jahr=2015&Nachname=&Vorname=&Verein=&VerTag=&VerOrt=&AproSeite=100" }
    val urls2016 = Array(1156) {it -> "http://www.marathon-ergebnis.de/cgi-bin/edbDetailSucheNeu.php?Seite=${it + 1}&Total=115505&MW=&AK=1&Ergebnis=0:00:00&Jahr=2016&Nachname=&Vorname=&Verein=&VerTag=&VerOrt=&AproSeite=100" }
    val urls2017 = Array(1145) {it -> "http://www.marathon-ergebnis.de/cgi-bin/edbDetailSucheNeu.php?Seite=${it + 1}&Total=114469&MW=&AK=1&Ergebnis=0:00:00&Jahr=2017&Nachname=&Vorname=&Verein=&VerTag=&VerOrt=&AproSeite=100"}

    private val scrapeInfo = StandardScrapeInfo<AgeGenderColumnPositions, ResultsPage>(
            url = "",
            marathonYear = -1,
            marathonSources = marathonSources,
            tableBodySelector = "table:nth-child(10) > tbody",
            skipRowCount = 2,
            columnPositions = AgeGenderColumnPositions(
                    nationality = 4,
                    place = 0,
                    finishTime = 7,
                    age = 6,
                    gender = 5,
                    category = 9
            )
    )

    override fun buildThreads() {
        urls2014.filter { url -> completed.none { cp -> cp.url == url } }.forEach { url ->
            threads.add(standardWebScraperAgeGender.scrape(scrapeInfo.copy(url = url, marathonYear = 2014), rowProcessor = StandardAgeGenderRowProcessor()))
        }
        urls2015.filter { url -> completed.none { cp -> cp.url == url } }.forEach { url ->
            threads.add(standardWebScraperAgeGender.scrape(scrapeInfo.copy(url = url, marathonYear = 2015), rowProcessor = StandardAgeGenderRowProcessor()))
        }
        urls2016.filter { url -> completed.none { cp -> cp.url == url } }.forEach { url ->
            threads.add(standardWebScraperAgeGender.scrape(scrapeInfo.copy(url = url, marathonYear = 2016), rowProcessor = StandardAgeGenderRowProcessor()))
        }
        urls2017.filter { url -> completed.none { cp -> cp.url == url } }.forEach { url ->
            threads.add(standardWebScraperAgeGender.scrape(scrapeInfo.copy(url = url, marathonYear = 2017), rowProcessor = StandardAgeGenderRowProcessor()))
        }
    }
}