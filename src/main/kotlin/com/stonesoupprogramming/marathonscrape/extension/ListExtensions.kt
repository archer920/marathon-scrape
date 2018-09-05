package com.stonesoupprogramming.marathonscrape.extension

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources

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