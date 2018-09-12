package com.stonesoupprogramming.marathonscrape.producers.sites.races

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.producers.AbstractResultsPageProducer
import com.stonesoupprogramming.marathonscrape.repository.ResultsRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class AntwerpProducer(@Autowired pagedResultsRepository: ResultsRepository<ResultsPage>) : AbstractResultsPageProducer<ResultsPage>(pagedResultsRepository, LoggerFactory.getLogger(AntwerpProducer::class.java), MarathonSources.Antwerp) {

    private val urls2014 = Array(4) { it -> "http://prod.chronorace.be/Classements/classement.aspx?eventId=243129508691975&redirect=0&mode=large&IdClassement=10335&srch=&scope=All&page=$it" }
    private val urls2015 = Array(5) { it -> "http://www.chronorace.be/Classements/classement.aspx?eventId=1138162038541123&lng=EN&mode=large&IdClassement=11108&srch=&scope=All&page=$it" }
    private val urls2016 = Array(5) { it -> "http://www.chronorace.be/Classements/classement.aspx?eventId=1186385931281826&lng=EN&mode=large&IdClassement=13045&srch=&scope=All&page=$it" }
    private val urls2017 = Array(5) { it -> "http://www.chronorace.be/Classements/classement.aspx?eventId=1186579204860344&lng=EN&mode=large&IdClassement=14971&srch=&scope=All&page=$it" }

    override fun buildThreads() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}