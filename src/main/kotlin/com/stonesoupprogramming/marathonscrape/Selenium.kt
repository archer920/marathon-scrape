package com.stonesoupprogramming.marathonscrape

import org.openqa.selenium.chrome.ChromeDriver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class NyWebScraper(@Autowired private val chromeDriver: ChromeDriver) {

    private val urls = (2014..2018).map { "https://results.nyrr.org/event/M$it/finishers" }.toList()

    fun scrape() : List<RunnerData> {
        return urls.map {

            chromeDriver.get(it)
            val elements = chromeDriver.findElementsByClassName("cmd-finisher")
            val runners = elements.map {
                val parts = it.text.split("\n")
                RunnerData(age(parts[1]),
                        gender(parts[1]),
                        nationality(parts[1]),
                        parts[4])
            }
            runners.sortedBy { it.age }.toList()
        }.flatten()
    }

    private fun gender(input : String) = input.substring(0,1)
    private fun age(input : String) = input.substring(1, 3)
    private fun nationality(input : String) =
            when{
                input.contains("USA") -> "USA"
                else -> input.substring(4, 7)
            }
}