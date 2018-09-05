package com.stonesoupprogramming.marathonscrape.extension

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