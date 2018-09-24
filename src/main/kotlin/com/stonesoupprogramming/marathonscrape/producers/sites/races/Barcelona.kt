package com.stonesoupprogramming.marathonscrape.producers.sites.races

import com.stonesoupprogramming.marathonscrape.enums.Gender
import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.extension.UNAVAILABLE
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.SequenceLinks
import com.stonesoupprogramming.marathonscrape.models.StandardScrapeInfo
import com.stonesoupprogramming.marathonscrape.producers.AbstractResultsPageProducer
import com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.AbstractNumberedAthSequenceProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.repository.ResultsRepository
import com.stonesoupprogramming.marathonscrape.scrapers.StandardWebScraperMergedAgeGender
import com.stonesoupprogramming.marathonscrape.scrapers.sites.AthLinksMarathonScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.function.BiFunction

@Component
class BarcelonaAthComponent(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
                               @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(BarcelonaAthComponent::class.java),
        MarathonSources.Barcelona,
        listOf(SequenceLinks(2015, "https://www.athlinks.com/event/34535/results/Event/430781/Course/641229/Results", 308, false),
                SequenceLinks(2016, "https://www.athlinks.com/event/34535/results/Event/527324/Course/784158/Results", 331, false),
                SequenceLinks(2017, "https://www.athlinks.com/event/34535/results/Event/623704/Course/901798/Results", 324, false)))

@Component
class BarcelonaProducer(@Autowired pagedResultsRepository: ResultsRepository<ResultsPage>,
                        @Autowired private val barcelonaAthComponent: BarcelonaAthComponent,
                        @Autowired private val standardWebScraperAgeGender: StandardWebScraperMergedAgeGender) : AbstractResultsPageProducer<ResultsPage>(pagedResultsRepository, LoggerFactory.getLogger(BarcelonaProducer::class.java), MarathonSources.Barcelona) {

    private val scrapeInfo = StandardScrapeInfo<MergedAgedGenderColumnPositions, ResultsPage>(
            url = "https://www.planete-marathon.fr/Resultats_course.php?epreuve=BARC&edition=BARC2014",
            marathonSources = marathonSources,
            marathonYear = 2014,
            tableBodySelector = "#corps > table:nth-child(4) > tbody:nth-child(2)",
            skipRowCount = 0,
            columnPositions = MergedAgedGenderColumnPositions(
                    nationality = 2,
                    nationalityFunction = BiFunction { txt, _ ->
                        try {
                            val last = txt.split(" ").last()
                            last.replace("(", "").replace(")", "")
                        } catch (e: Exception) {
                            logger.error("Unable to parse nationality", e)
                            throw e
                        }
                    },
                    place = 0,
                    finishTime = 1,
                    ageGender = 4,
                    ageFunction = BiFunction { txt, _ ->
                        try {
                            val age = txt.filter { c: Char -> c.isDigit() }.toList().joinToString(separator = "")
                            if (age.isNotBlank()) {
                                age
                            } else {
                                UNAVAILABLE
                            }
                        } catch (e: Exception) {
                            logger.error("Unable to find age", e)
                            throw e
                        }
                    },
                    genderFunction = BiFunction { txt, _ ->
                        try {
                            when {
                                txt.contains("M") -> Gender.MALE.code
                                txt.contains("F") -> Gender.FEMALE.code
                                else -> UNAVAILABLE
                            }
                        } catch (e: Exception) {
                            logger.error("Unable to determine gender", e)
                            throw e
                        }
                    }
            )
    )

    override fun buildThreads() {
        threads.add(standardWebScraperAgeGender.scrape(scrapeInfo))
    }
}