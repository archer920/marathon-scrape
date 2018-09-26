package com.stonesoupprogramming.marathonscrape.producers.sites.races

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.extension.UNAVAILABLE
import com.stonesoupprogramming.marathonscrape.extension.calcAge
import com.stonesoupprogramming.marathonscrape.models.AgeGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.PagedScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.SequenceLinks
import com.stonesoupprogramming.marathonscrape.producers.AbstractBaseProducer
import com.stonesoupprogramming.marathonscrape.producers.AbstractNumberedResultsPageProducer
import com.stonesoupprogramming.marathonscrape.producers.sites.athlinks.AbstractNumberedAthSequenceProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.scrapers.StandardAgeGenderRowProcessor
import com.stonesoupprogramming.marathonscrape.scrapers.StandardMergedAgeGenderRowProcessor
import com.stonesoupprogramming.marathonscrape.scrapers.sites.AthLinksMarathonScraper
import com.stonesoupprogramming.marathonscrape.scrapers.sites.KalendarzBiegowyScraper
import com.stonesoupprogramming.marathonscrape.scrapers.sites.PkoScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.function.BiFunction

@Component
class PkoAthComponent(@Autowired athLinksMarathonScraper: AthLinksMarathonScraper,
               @Autowired numberedResultsPageRepository: NumberedResultsPageRepository)
    : AbstractNumberedAthSequenceProducer(athLinksMarathonScraper, numberedResultsPageRepository,
        LoggerFactory.getLogger(PkoAthComponent::class.java),
        MarathonSources.PKO,
        listOf(SequenceLinks(2014, "https://www.athlinks.com/event/36171/results/Event/795244/Course/1381840/Results", 77),
                SequenceLinks(2017, "https://www.athlinks.com/event/36171/results/Event/688855/Course/1114288/Results", 93)))

@Component
class PkoDataSportComponent(@Autowired private val pkoScraper: PkoScraper,
                  @Autowired numberedResultsPageRepository: NumberedResultsPageRepository) : AbstractNumberedResultsPageProducer(numberedResultsPageRepository, LoggerFactory.getLogger(PkoDataSportComponent::class.java), MarathonSources.PKO){

    private val scrapeInfo = PagedScrapeInfo(
            url = "https://wyniki.datasport.pl/results1581/index.php",
            marathonSources = marathonSources,
            marathonYear = 2015,
            tableBodySelector = "#table2 > tbody > tr > td:nth-child(1) > table > tbody",
            clipRows = 2,
            skipRowCount = 1,
            clickNextSelector = "#table2 > tbody > tr > td:nth-child(1) > table > tbody > tr:nth-child(12) > td:nth-child(3) > button:nth-child(3)",
            clickPreviousSelector = "#table2 > tbody > tr > td:nth-child(1) > table > tbody > tr:nth-child(12) > td:nth-child(1) > button:nth-child(3)",
            columnPositions = MergedAgedGenderColumnPositions(
                    nationality = 2,
                    finishTime = 6,
                    ageGender = 5,
                    place = 0,
                    ageFunction = BiFunction { txt, _ ->
                        try {
                            val first = txt.split("-").first()
                            val age = first.replace("M", "").replace("K", "")
                            if(age.isBlank()){
                                UNAVAILABLE
                            } else {
                                age
                            }
                        } catch (e : Exception){
                            logger.error("Unable to find age", e)
                            throw e
                        }
                    },
                    genderFunction = BiFunction { txt, _ ->
                        try {
                            val letter = txt[0].toString()
                            if(letter == "K"){
                                "F"
                            } else {
                                letter
                            }
                        } catch (e : Exception){
                            logger.error("Unable to determine gender", e)
                            throw e
                        }
                    }
            ),
            currentPage = 0,
            startPage = 0,
            endPage = 476
    )

    override fun buildYearlyThreads(year: Int, lastPage: Int) {
        when(year){
            2015 -> {
                threads.add(pkoScraper.scrape(scrapeInfo.copy(startPage = lastPage), rowProcessor = StandardMergedAgeGenderRowProcessor()))
            }
        }
    }
}

@Component
class PkoKalendarzBiegowyComponent(@Autowired private val kalendarzBiegowyScraper: KalendarzBiegowyScraper,
                               @Autowired numberedResultsPageRepository: NumberedResultsPageRepository) : AbstractNumberedResultsPageProducer(numberedResultsPageRepository, LoggerFactory.getLogger(PkoKalendarzBiegowyComponent::class.java), MarathonSources.PKO) {

    private val scrapeInfo = PagedScrapeInfo(
            url = "https://kalendarzbiegowy.pl/34-pko-wroclaw-maraton/results#runId-14053",
            marathonSources = marathonSources,
            marathonYear = 2016,
            tableBodySelector = "",
            skipRowCount = 1,
            clickNextSelector = "",
            clickPreviousSelector = "",
            columnPositions = AgeGenderColumnPositions(
                    nationality = 4,
                    finishTime = 8,
                    age = 5,
                    gender = 3,
                    place = 0,
                    ageFunction = BiFunction { txt, _ ->
                        if(txt == "-" || txt.isBlank()){
                            UNAVAILABLE
                        } else {
                            txt.calcAge(logger, false)
                        }
                    }
            ),
            currentPage = 0,
            startPage = 0,
            endPage = 476
    )

    override fun buildYearlyThreads(year: Int, lastPage: Int) {
        if(year == 2015){
            threads.add(kalendarzBiegowyScraper.scrape(scrapeInfo, rowProcessor = StandardAgeGenderRowProcessor()))
        }
    }
}

@Component
class PkoProducer(@Autowired private val pkoAthComponent: PkoAthComponent,
                  @Autowired private val pkoDataSportComponent: PkoDataSportComponent,
                  @Autowired private val pkoKalendarzBiegowyComponent: PkoKalendarzBiegowyComponent) : AbstractBaseProducer(LoggerFactory.getLogger(PkoProducer::class.java), MarathonSources.PKO){

    override fun buildThreads() {
        threads.addAll(pkoAthComponent.process())
        threads.addAll(pkoDataSportComponent.process())
        threads.addAll(pkoKalendarzBiegowyComponent.process())
    }
}