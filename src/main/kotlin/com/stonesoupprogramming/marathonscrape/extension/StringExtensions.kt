package com.stonesoupprogramming.marathonscrape.extension

import org.openqa.selenium.By
import org.slf4j.Logger
import java.time.LocalDate

const val UNAVAILABLE = "Unavailable"

fun String.toCss(): By {
    return By.cssSelector(this)
}

fun String.toXpath(): By {
    return By.xpath(this)
}

fun String.safeInt(logger: Logger): Int {
    return try {
        this.toInt()
    } catch (e: Exception) {
        logger.error("Unable to parse int from value $this", e)
        Int.MAX_VALUE
    }
}

fun String.calcAge(logger: Logger, twoDigit: Boolean = true): String {
    return try {
        if (this.isBlank()) {
            return UNAVAILABLE
        }
        val yob = if (twoDigit) {
            LocalDate.now().year - (1900 + this.safeInt(logger))
        } else {
            LocalDate.now().year - this.safeInt(logger)
        }
        yob.toString()
    } catch (e: Exception) {
        logger.error("Failed to calculate age for $this", e)
        throw e
    }
}

fun String.unavailableIfBlank(): String {
    return if (this.isBlank()) {
        UNAVAILABLE
    } else {
        this
    }
}

fun String.toNationality(usCodes: List<String>, canadaCodes: List<String>, separator: String = ","): String {
    return if (this.contains(separator)) {
        var code = this.split(separator).last().trim()
        code = usCodes.stateToUSA(code)
        code = canadaCodes.provinceToCanada(code)
        code
    } else {
        var code = this.unavailableIfBlank()
        code = usCodes.stateToUSA(code)
        code = canadaCodes.provinceToCanada(code)
        code
    }
}


private val states = listOf("Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut",
        "Delaware", "Florida", "Georgia", "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana",
        "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska",
        "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota", "North Dako", "Ohio", "Oklahoma",
        "Oregon", "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota", "South Dako", "NORTH CARO", "MISSISSIPP", "Tennessee", "Texas", "Utah", "Vermont",
        "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming", "MASSACHUSE").map { it -> it.toLowerCase() }


fun String.toNationality(): String {
    return if (states.contains(this.toLowerCase())) {
        "USA"
    } else {
        this
    }
}

fun String.isAllCaps() : Boolean = this.all { it-> it.isUpperCase() }