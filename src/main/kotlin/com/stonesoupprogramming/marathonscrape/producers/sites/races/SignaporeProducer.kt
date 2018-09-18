package com.stonesoupprogramming.marathonscrape.producers.sites.races

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.extension.UNAVAILABLE
import com.stonesoupprogramming.marathonscrape.models.AgeGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.StandardScrapeInfo
import com.stonesoupprogramming.marathonscrape.producers.AbstractResultsPageProducer
import com.stonesoupprogramming.marathonscrape.repository.ResultsRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.MultisportAustraliaScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.function.BiFunction

@Component
class SignaporeProducer(
        @Autowired private val multisportAustraliaScraper: MultisportAustraliaScraper,
        @Autowired pagedResultsRepository: ResultsRepository<ResultsPage>)
    : AbstractResultsPageProducer<ResultsPage>(pagedResultsRepository, LoggerFactory.getLogger(CapetownUrlComponent::class.java), MarathonSources.Singapore) {

    private val urls2014 = Array(515) { it -> "https://www.multisportaustralia.com.au/races/6184/events/1?page=$it" }
    private val urls2015 = Array(459) { it -> "https://www.multisportaustralia.com.au/races/10714/events/1?page=$it" }
    private val urls2016 = Array(416) { it -> "https://www.multisportaustralia.com.au/races/13255/events/1?page=$it" }
    private val urls2017 = Array(389) { it -> "https://www.multisportaustralia.com.au/races/14829/events/3?page=$it" }

    override fun buildThreads() {
        val nationalityFunction = BiFunction<String, String, String> { text, html ->
            try {
                when {
                    html.contains("title") -> {
                        val alt = html.split("=").find { it -> it.contains("title") }
                        alt?.replace(" title", "")?.replace("\"", "") ?: UNAVAILABLE
                    }
                    else -> {
                        val nationality = text.split("\n")[0].trim()
                        if(nationality == "|"){
                            UNAVAILABLE
                        } else{
                            nationality
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Unable to parse nationality", e)
                throw e
            }
        }

        val scrapeInfo = StandardScrapeInfo<AgeGenderColumnPositions, ResultsPage>(
                url = "",
                marathonSources = marathonSources,
                marathonYear = 0,
                tableBodySelector = ".table > tbody:nth-child(2)",
                skipRowCount = 0,
                columnPositions = AgeGenderColumnPositions(nationality = 5, finishTime = 4, place = 0, gender = 7, age = 1, nationalityFunction = nationalityFunction),
                category = null,
                gender = null)

        urls2014.filter { completed.none { cp -> cp.url == it } }.forEach { url ->
            threads.add(multisportAustraliaScraper.scrape(scrapeInfo.copy(url = url, marathonYear = 2014)))
        }

        urls2015.filter { completed.none { cp -> cp.url == it } }.forEach { url ->
            threads.add(multisportAustraliaScraper.scrape(scrapeInfo.copy(url = url, marathonYear = 2015)))
        }

        urls2016.filter { completed.none { cp -> cp.url == it } }.forEach { url ->
            threads.add(multisportAustraliaScraper.scrape(scrapeInfo.copy(url = url, marathonYear = 2016)))
        }

        urls2017.filter { completed.none { cp -> cp.url == it } }.forEach { url ->
            threads.add(multisportAustraliaScraper.scrape(scrapeInfo.copy(url = url, marathonYear = 2017)))
        }
    }
}