package com.stonesoupprogramming.marathonscrape.producers.sites.races

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumnPositions
import com.stonesoupprogramming.marathonscrape.models.PagedScrapeInfo
import com.stonesoupprogramming.marathonscrape.producers.AbstractNumberedResultsPageProducer
import com.stonesoupprogramming.marathonscrape.repository.NumberedResultsPageRepository
import com.stonesoupprogramming.marathonscrape.scrapers.sites.RunToPixScraper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class KinmenProducer(@Autowired private val runToPixScraper: RunToPixScraper,
                              @Autowired numberedResultsPageRepository: NumberedResultsPageRepository) : AbstractNumberedResultsPageProducer(numberedResultsPageRepository, LoggerFactory.getLogger(KinmenProducer::class.java), MarathonSources.Kinmen){

    private val url2014 = "https://www.run2pix.com/report/report_w.php?EventCode=20140112&Race=MA&sn=64"
    private val end2014 = 2

    private val url2015 = "https://www.run2pix.com/report/report_w.php?EventCode=20150118&Race=MA&sn=93"
    private val end2015 = 4

    private val url2016 = "https://www.run2pix.com/report/report_w.php?EventCode=20160228&Race=MA&sn=119"
    private val end2016 = 4

    private val url2017 = "https://www.run2pix.com/report/report_w.php?EventCode=20170114&Race=MA&sn=140"
    private val end2017 = 4

    private val scrapeInfo = PagedScrapeInfo(
            url = "",
            marathonSources = marathonSources,
            marathonYear = -1,
            tableBodySelector = "body > table:nth-child(4) > tbody:nth-child(1)",
            skipRowCount = 8,
            clipRows = 5,
            columnPositions = MergedAgedGenderColumnPositions(
                    nationality = -1,
                    finishTime = 7,
                    place = 5,
                    ageGender = 3
            ),
            clickNextSelector = "body > table:nth-child(4) > tbody > tr:nth-child(6) > td > table > tbody > tr > td > table > tbody > tr > td:nth-child(2) > table > tbody > tr > td:nth-child(3) > a",
            secondaryClickNextSelector = "body > table:nth-child(4) > tbody:nth-child(1) > tr:nth-child(6) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(3) > a:nth-child(1)",
            clickPreviousSelector = "body > table:nth-child(4) > tbody:nth-child(1) > tr:nth-child(6) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2) > a:nth-child(1)",
            currentPage = 0,
            startPage = 0,
            endPage = -1
    )

    override fun buildYearlyThreads(year: Int, lastPage: Int) {
        val localScrapeInfo = scrapeInfo.copy(startPage = lastPage)
        when(year){
            2014 -> threads.add(runToPixScraper.scrape(localScrapeInfo.copy(url = url2014, endPage = end2014, marathonYear = 2014)))
            2015 -> threads.add(runToPixScraper.scrape(localScrapeInfo.copy(url = url2015, endPage = end2015, marathonYear = 2015)))
            2016 -> threads.add(runToPixScraper.scrape(localScrapeInfo.copy(url = url2016, endPage = end2016, marathonYear = 2016)))
            2017 -> threads.add(runToPixScraper.scrape(localScrapeInfo.copy(url = url2017, endPage = end2017, marathonYear = 2017)))
        }
    }
}