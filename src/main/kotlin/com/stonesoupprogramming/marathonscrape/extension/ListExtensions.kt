package com.stonesoupprogramming.marathonscrape.extension

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.AbstractColumnPositions
import com.stonesoupprogramming.marathonscrape.models.AbstractScrapeInfo
import com.stonesoupprogramming.marathonscrape.models.ResultsPage

fun List<String>.stateToUSA(code: String): String {
    return if (this.contains(code.trim())) {
        "USA"
    } else {
        code.trim()
    }
}

fun List<String>.provinceToCanada(code : String): String {
    return if(this.contains(code.trim())) {
        "CAN"
    } else {
        code.trim()
    }
}

fun Array<out String>.toMarathonSources(): List<MarathonSources?> {
    return this.map { arg -> MarathonSources.values().find { arg == it.arg } }.toList()
}

fun <T : AbstractColumnPositions, U : ResultsPage> List<ResultsPage>.filterCompleteByUrlAndCategory(scrapeInfo : List<AbstractScrapeInfo<T, U>>) : List<AbstractScrapeInfo<T, U>> {
    return scrapeInfo.filter { input -> this.none { it -> it.url == input.url && it.category == input.category }}
}