package com.stonesoupprogramming.marathonscrape.producers.sites.races

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.AgeGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.StandardScrapeInfo
import com.stonesoupprogramming.marathonscrape.producers.AbstractResultsPageProducer
import com.stonesoupprogramming.marathonscrape.repository.ResultsRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.ChronoRaceScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class AntwerpProducer(
        @Autowired private val chronoRaceScraper: ChronoRaceScraper,
        @Autowired pagedResultsRepository: ResultsRepository<ResultsPage>) : AbstractResultsPageProducer<ResultsPage>(pagedResultsRepository, LoggerFactory.getLogger(AntwerpProducer::class.java), MarathonSources.Antwerp) {

    private val urls2014 = Array(4) { it -> "http://prod.chronorace.be/Classements/classement.aspx?eventId=243129508691975&redirect=0&mode=large&IdClassement=10335&srch=&scope=All&page=$it" }
    private val urls2015 = Array(5) { it -> "http://www.chronorace.be/Classements/classement.aspx?eventId=1138162038541123&lng=EN&mode=large&IdClassement=11108&srch=&scope=All&page=$it" }
    private val urls2016 = Array(5) { it -> "http://www.chronorace.be/Classements/classement.aspx?eventId=1186385931281826&lng=EN&mode=large&IdClassement=13045&srch=&scope=All&page=$it" }
    private val urls2017 = Array(5) { it -> "http://www.chronorace.be/Classements/classement.aspx?eventId=1186579204860344&lng=EN&mode=large&IdClassement=14971&srch=&scope=All&page=$it" }

    override fun buildThreads() {
        val scrapeInfo = StandardScrapeInfo<AgeGenderColumnPositions, ResultsPage>(
                url = "",
                marathonSources = marathonSources,
                marathonYear = 0,
                tableBodySelector = "#classements > tbody:nth-child(1) > tr:nth-child(3) > td:nth-child(1) > table:nth-child(4) > tbody:nth-child(1)",
                skipRowCount = 3,
                columnPositions =  AgeGenderColumnPositions(
                        nationality = 6,
                        finishTime = 8,
                        place = 1,
                        gender = 3,
                        age = 5
                )
        )

        val columnPositions = AgeGenderColumnPositions(
                nationality = 5,
                finishTime = 7,
                place = 0,
                gender = 2,
                age = 4
        )

        urls2014.filter { url -> completed.none { cp -> cp.url == url } }.forEach {
            chronoRaceScraper.scrape(scrapeInfo.copy(url = it, marathonYear = 2014))
        }

        urls2015.filter { url -> completed.none { cp -> cp.url == url } }.forEach {
            chronoRaceScraper.scrape(scrapeInfo.copy(url = it, marathonYear = 2015, columnPositions = columnPositions))
        }

        urls2016.filter { url -> completed.none { cp -> cp.url == url } }.forEach {
            chronoRaceScraper.scrape(scrapeInfo.copy(url = it, marathonYear = 2016, columnPositions = columnPositions))
        }

        urls2017.filter { url -> completed.none { cp -> cp.url == url } }.forEach {
            chronoRaceScraper.scrape(scrapeInfo.copy(url = it, marathonYear = 2017, columnPositions = columnPositions))
        }
    }
}