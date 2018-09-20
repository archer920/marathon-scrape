package com.stonesoupprogramming.marathonscrape.producers.sites.races

import com.stonesoupprogramming.marathonscrape.enums.Gender
import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.extension.UNAVAILABLE
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.StandardScrapeInfo
import com.stonesoupprogramming.marathonscrape.producers.AbstractResultsPageProducer
import com.stonesoupprogramming.marathonscrape.repository.ResultsRepository
import com.stonesoupprogramming.marathonscrape.scrapers.StandardWebScraperMergedAgeGender
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.function.BiFunction
import java.util.function.Predicate

@Component
class HcaProducer(@Autowired private val standardWebScraperMergedAgeGender: StandardWebScraperMergedAgeGender,
                  @Autowired pagedResultsRepository: ResultsRepository<ResultsPage>) : AbstractResultsPageProducer<ResultsPage>(pagedResultsRepository, LoggerFactory.getLogger(HcaProducer::class.java), MarathonSources.Hca) {

    private val urls2014 = Array(34) { it -> "https://www.sportstiming.dk/event/728/results?page=${it + 1}" }
    private val urls2015 = Array(31) { it -> "https://www.sportstiming.dk/event/2399/results?page=${it + 1}" }
    private val urls2016 = Array(27) { it -> "https://www.sportstiming.dk/event/4008/results?page=${it + 1}" }
    private val urls2017 = Array(27) { it -> "https://www.sportstiming.dk/event/4505/results?page=${it + 1}" }

    private val scrapeInfo = StandardScrapeInfo<MergedAgedGenderColumnPositions, ResultsPage>(
            url = "",
            marathonSources = marathonSources,
            marathonYear = -1,
            tableBodySelector = ".table-striped > tbody:nth-child(1)",
            skipRowCount = 1,
            tableRowFilter = java.util.function.Function { it -> it.filter { sub -> sub.size == 9 } },
            columnPositions = MergedAgedGenderColumnPositions(
                    nationality = 7,
                    nationalityFunction = BiFunction { _, html ->
                        try {
                            val parts = html.split(" ")
                            parts[1].replace("title=", "").replace("\"", "")
                        } catch (e: Exception) {
                            logger.error("Unable to find nationality", e)
                            throw e
                        }
                    },
                    place = 0,
                    finishTime = 2,
                    ageGender = 5,
                    ageFunction = BiFunction { text, _ ->
                        if (text == "-") {
                            UNAVAILABLE
                        } else {
                            text.replace("M", "").replace("K", "")
                        }
                    },
                    genderFunction = BiFunction { text, _ ->
                        if (text == "-") {
                            UNAVAILABLE
                        } else {
                            val g = text[0].toString()
                            if (g == "K") {
                                Gender.FEMALE.code
                            } else {
                                Gender.MALE.code
                            }
                        }
                    }
            )
    )

    override fun buildThreads() {
        val predicate = Predicate<String> { it -> completed.none { cp -> cp.url == it } }
        urls2014.filter(predicate::test).forEach { link ->
            threads.add(standardWebScraperMergedAgeGender.scrape(scrapeInfo.copy(url = link, marathonYear = 2014)))
        }
        urls2015.filter(predicate::test).forEach { link ->
            threads.add(standardWebScraperMergedAgeGender.scrape(scrapeInfo.copy(url = link, marathonYear = 2015)))
        }
        urls2016.filter(predicate::test).forEach { link ->
            threads.add(standardWebScraperMergedAgeGender.scrape(scrapeInfo.copy(url = link, marathonYear = 2016)))
        }
        urls2017.filter(predicate::test).forEach { link ->
            threads.add(standardWebScraperMergedAgeGender.scrape(scrapeInfo.copy(url = link, marathonYear = 2017)))
        }
    }
}