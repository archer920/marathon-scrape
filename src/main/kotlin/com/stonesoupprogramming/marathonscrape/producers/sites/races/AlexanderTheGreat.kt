package com.stonesoupprogramming.marathonscrape.producers.sites.races

import com.stonesoupprogramming.marathonscrape.enums.Gender
import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.extension.calcAge
import com.stonesoupprogramming.marathonscrape.models.AgeGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.StandardScrapeInfo
import com.stonesoupprogramming.marathonscrape.producers.AbstractResultsPageProducer
import com.stonesoupprogramming.marathonscrape.repository.ResultsRepository
import com.stonesoupprogramming.marathonscrape.scrapers.StandardWebScraperAgeGender
import com.stonesoupprogramming.marathonscrape.scrapers.sites.AlexanderTheGreatScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.function.BiFunction

@Component
class AlexanderTheGreatProducer(@Autowired pageResultsRepository: ResultsRepository<ResultsPage>,
                                @Autowired private val standardWebScraperAgeGender: StandardWebScraperAgeGender,
                                @Autowired private val alexanderTheGreatScraper: AlexanderTheGreatScraper) : AbstractResultsPageProducer<ResultsPage>(pageResultsRepository, LoggerFactory.getLogger(AlexanderTheGreatProducer::class.java), MarathonSources.Axexander) {

    private val mens2014 = Array(112) { it -> "http://www.alexanderthegreatmarathon.org/index.php?option=com_joodb&view=catalog&format=html&reset=false&orderby=rankgeneral&Itemid=124&gender[0]=male&club[0]=&nationality[0]=&category[0]=&search=search...&searchfield=ALL&lang=en&limitstart=${it * 10}" }
    private val womens2014 = Array(12) { it -> "http://www.alexanderthegreatmarathon.org/index.php?option=com_joodb&view=catalog&format=html&reset=false&orderby=rankgeneral&Itemid=124&gender[0]=female&club[0]=&nationality[0]=&category[0]=&searchfield=ALL&limitstart=${it * 10}&lang=en" }
    private val mens2016 = Array(29) { it -> "http://www.alexanderthegreatmarathon.org/entries/index.php/results-2016/marathon?format=html&reset=false&gs[Gender][0]=male&gs[Club][0]=&gs[Category][0]=&start=${it * 50}" }
    private val womens2016 = Array(3) { it -> "http://www.alexanderthegreatmarathon.org/entries/index.php/results-2016/marathon?format=html&reset=false&gs[Gender][0]=female&gs[Club][0]=&gs[Category][0]=&start=${it * 50}" }
    private val mens2017 = Array(29) { it -> "http://www.alexanderthegreatmarathon.org/entries/index.php/results-2017/marathon?format=html&reset=false&gs[Gender][0]=Male&gs[Club][0]=&gs[Category][0]=&start=${it * 50}" }
    private val womens2017 = Array(3) { it -> "http://www.alexanderthegreatmarathon.org/entries/index.php/results-2017/marathon?format=html&reset=false&gs[Gender][0]=Female&gs[Club][0]=&gs[Category][0]=&start=${it * 50}" }

    private val url2015 = "http://www.racetimer.se/en/race/resultlist/2459?commit=Show+results+%3E%3E&layout=clean%22&page=1&per_page=1500&race_id=2459&rc_id=10269#top"

    override fun buildThreads() {
        val scrapeInfo = StandardScrapeInfo<AgeGenderColumnPositions, ResultsPage>(
                url = "",
                marathonSources = marathonSources,
                marathonYear = 0,
                tableBodySelector = "",
                skipRowCount = 0,
                columnPositions = AgeGenderColumnPositions(nationality = -1, finishTime = -1, place = -1, age = -1, gender = -1)
        )
        mens2014.filter { link -> completed.none { cp -> cp.url == link } }.forEach { link ->
            threads.add(alexanderTheGreatScraper.scrape(scrapeInfo.copy(url = link, marathonYear = 2014, tableBodySelector = ".contentpaneopen > tbody:nth-child(1) > tr:nth-child(5) > td:nth-child(1)")))
        }
        womens2014.filter { link -> completed.none { cp -> cp.url == link } }.forEach { link ->
            threads.add(alexanderTheGreatScraper.scrape(scrapeInfo.copy(url = link, marathonYear = 2014, tableBodySelector = ".contentpaneopen > tbody:nth-child(1) > tr:nth-child(5) > td:nth-child(1)")))
        }

        if (completed.none { cp -> cp.url == url2015 }) {
            threads.add(standardWebScraperAgeGender.scrape(StandardScrapeInfo(
                    url = url2015,
                    marathonSources = marathonSources,
                    marathonYear = 2015,
                    tableBodySelector = "#top3-list > tbody:nth-child(1)",
                    skipRowCount = 1,
                    columnPositions = AgeGenderColumnPositions(
                            nationality = 3,
                            finishTime = 8,
                            place = 0,
                            age = 2,
                            gender = 4,
                            ageFunction = BiFunction { text, _ -> text.calcAge(logger, false) }
                    ))))
        }

        mens2016.filter { link -> completed.none { cp -> cp.url == link } }.forEach { link ->
            threads.add(alexanderTheGreatScraper.scrape(scrapeInfo.copy(url = link, marathonYear = 2016, tableBodySelector = ".contentpaneopen > tbody:nth-child(1) > tr:nth-child(4)", gender = Gender.MALE)))
        }
        womens2016.filter { link -> completed.none { cp -> cp.url == link } }.forEach { link ->
            threads.add(alexanderTheGreatScraper.scrape(scrapeInfo.copy(url = link, marathonYear = 2016, tableBodySelector = ".contentpaneopen > tbody:nth-child(1) > tr:nth-child(4)", gender = Gender.FEMALE)))
        }
        mens2017.filter { link -> completed.none { cp -> cp.url == link } }.forEach { link ->
            threads.add(alexanderTheGreatScraper.scrape(scrapeInfo.copy(url = link, marathonYear = 2017, tableBodySelector = ".contentpaneopen > tbody:nth-child(1) > tr:nth-child(4)", gender = Gender.MALE)))
        }
        womens2017.filter { link -> completed.none { cp -> cp.url == link } }.forEach { link ->
            threads.add(alexanderTheGreatScraper.scrape(scrapeInfo.copy(url = link, marathonYear = 2017, tableBodySelector = ".contentpaneopen > tbody:nth-child(1) > tr:nth-child(4)", gender = Gender.FEMALE)))
        }
    }
}