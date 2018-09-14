package com.stonesoupprogramming.marathonscrape.producers.sites.races

import com.stonesoupprogramming.marathonscrape.enums.Gender
import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.extension.calcAge
import com.stonesoupprogramming.marathonscrape.models.AgeGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.StandardScrapeInfo
import com.stonesoupprogramming.marathonscrape.producers.AbstractResultsPageProducer
import com.stonesoupprogramming.marathonscrape.repository.ResultsRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.MikatimingDeScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.function.BiFunction

@Component
class HamburgProducer(@Autowired private val mikatimingDeScraper: MikatimingDeScraper,
                      @Autowired pagedResultsRepository: ResultsRepository<ResultsPage>) :
        AbstractResultsPageProducer<ResultsPage>(pagedResultsRepository, LoggerFactory.getLogger(HamburgProducer::class.java), MarathonSources.Hamburg){

    private val mens2014 = Array(22) {it -> "http://hamburg.r.mikatiming.de/2014/?page=${it + 1}&event=HML&num_results=500&pid=list&search%5Bage_class%5D=%25&search%5Bsex%5D=M" }
    private val womens2014 = Array(6) {it -> "http://hamburg.r.mikatiming.de/2014/?page=${it + 1}&event=HML&num_results=500&pid=list&search%5Bage_class%5D=%25&search%5Bsex%5D=W" }

    private val mens2015 = Array(23) { it -> "http://hamburg.r.mikatiming.de/2015/?page=${it + 1}&event=HML&num_results=500&pid=list&search%5Bsex%5D=M" }
    private val womens2015 = Array(7){ it -> "http://hamburg.r.mikatiming.de/2015/?page=${it + 1}&event=HML&num_results=500&pid=list&search%5Bsex%5D=W" }

    private val mens2016 = Array(19){ it -> "http://hamburg.r.mikatiming.de/2016/?page=${it + 1}&event=HML&num_results=500&pid=list&search%5Bage_class%5D=%25&search%5Bsex%5D=M" }
    private val womens2016 = Array(6) { it -> "http://hamburg.r.mikatiming.de/2016/?page=${it + 1}&event=HML&num_results=500&pid=list&search%5Bage_class%5D=%25&search%5Bsex%5D=W" }

    private val mens2017 = Array(19) { it -> "http://hamburg.r.mikatiming.de/2017/?page=${it + 1}&event=HML&num_results=500&pid=list&search%5Bage_class%5D=%25&search%5Bsex%5D=M" }
    private val womens2017 = Array(6) { it -> "http://hamburg.r.mikatiming.de/2017/?page=${it + 1}&event=HML&num_results=500&pid=list&search%5Bage_class%5D=%25&search%5Bsex%5D=W" }

    override fun buildThreads() {
        val scrapeInfo = StandardScrapeInfo<AgeGenderColumnPositions, ResultsPage> (
                url = "",
                marathonSources = marathonSources,
                marathonYear = 0,
                tableBodySelector = ".list-table > tbody:nth-child(2)",
                skipRowCount = 0,
                columnPositions = AgeGenderColumnPositions(
                        nationality = 3,
                        finishTime = 7,
                        place = 0,
                        age = 5,
                        ageFunction = BiFunction { text, _ ->
                            val ageParts = text.split(" ")
                            val ages = ageParts.find { part -> part.contains("-") }
                            ages?.let { age ->
                                val parts = age.split("-")
                                "${parts[1].calcAge(logger, false)} - ${parts[0].calcAge(logger, false)}"
                            }
                                    ?: text
                        },
                        gender = -1
                )
        )

        mens2014.filter { url -> completed.none { cp -> cp.url == url } }.forEach { url ->
            threads.add(mikatimingDeScraper.scrape(scrapeInfo.copy(url = url, marathonYear = 2014, gender = Gender.MALE)))
        }
        womens2014.filter { url -> completed.none { cp -> cp.url == url } }.forEach { url ->
            threads.add(mikatimingDeScraper.scrape(scrapeInfo.copy(url = url, marathonYear = 2014, gender = Gender.FEMALE)))
        }

        mens2015.filter { url -> completed.none { cp -> cp.url == url } }.forEach { url ->
            threads.add(mikatimingDeScraper.scrape(scrapeInfo.copy(url = url, marathonYear = 2015, gender = Gender.MALE)))
        }
        womens2015.filter { url -> completed.none { cp -> cp.url == url } }.forEach { url ->
            threads.add(mikatimingDeScraper.scrape(scrapeInfo.copy(url = url, marathonYear = 2015, gender = Gender.FEMALE)))
        }

        mens2016.filter { url -> completed.none { cp -> cp.url == url } }.forEach { url ->
            threads.add(mikatimingDeScraper.scrape(scrapeInfo.copy(url = url, marathonYear = 2016, gender = Gender.MALE)))
        }
        womens2016.filter { url -> completed.none { cp -> cp.url == url } }.forEach { url ->
            threads.add(mikatimingDeScraper.scrape(scrapeInfo.copy(url = url, marathonYear = 2016, gender = Gender.FEMALE)))
        }

        mens2017.filter { url -> completed.none { cp -> cp.url == url } }.forEach { url ->
            threads.add(mikatimingDeScraper.scrape(scrapeInfo.copy(url = url, marathonYear = 2017, gender = Gender.MALE)))
        }
        womens2017.filter { url -> completed.none { cp -> cp.url == url } }.forEach { url ->
            threads.add(mikatimingDeScraper.scrape(scrapeInfo.copy(url = url, marathonYear = 2017, gender = Gender.FEMALE)))
        }
    }
}