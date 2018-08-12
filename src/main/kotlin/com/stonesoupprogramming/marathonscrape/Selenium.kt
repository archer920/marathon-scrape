package com.stonesoupprogramming.marathonscrape

import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.BlockingQueue
import javax.validation.ConstraintViolationException

const val END_OF_SCRAPE = "End of Scrape"

@Component
@Scope("prototype")
class NyWebScraper {

    private val logger = LoggerFactory.getLogger(NyWebScraper::class.java)

    @Async
    fun scrape(driver: RemoteWebDriver, queue: BlockingQueue<RunnerData>, year: Int, url: String) {
        try {
            driver.get(url)
            var showMore = true
            var page = 0

            try {
                driver.findElementByClassName("footer-rms-ads-close").click()
            } catch (e : Exception){
                logger.error("Unable to dismiss ad", e)
            }

            while (showMore) {
                try {
                    val elements = driver.findElementsByClassName("cmd-finisher")
                    elements.forEach {
                        val parts = it.text.split("\n")
                        val place = parts[8].replace(",", "").toInt()
                        val runnerData = RunnerData(
                                age = age(parts[1]),
                                gender = gender(parts[1]),
                                nationality = nationality(parts[1]),
                                finishTime = parts[4],
                                marathonYear = year,
                                source = "NYRR",
                                raceYearPlace = "$year, $place",
                                place = place)
                        queue.put(runnerData)
                        logger.info("Produced: $runnerData")
                    }

                    val button = driver.findElementsByClassName("button-load-more")
                    showMore = button.size == 2
                    if (showMore) {
                        driver.executeScript("arguments[0].scrollIntoView(true);", button[1])
                        WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(button[1]))
                        button[1].click()

                        logger.info("Advanced to page $page")
                        page++
                    }
                } catch (e: Exception) {
                    logger.error("Exception while advancing", e)
                    showMore = false
                }
            }
            queue.put(RunnerData(source = END_OF_SCRAPE))
            logger.info("Scraping $year completed")
        } catch (e : Exception){
            logger.error("Exception during scrape")
        } finally {
            driver.close()
        }
    }

    private fun gender(input: String) = input.substring(0, 1)
    private fun age(input: String) = input.substring(1, 3)
    private fun nationality(input: String) =
            when {
                input.contains("USA") -> "USA"
                else -> input.substring(4, 7)
            }
}

@Component
@Scope("prototype")
class NyConsumer(@Autowired private val repository: RunnerDataRepository){

    private val logger = LoggerFactory.getLogger(NyConsumer::class.java)

    @Async
    fun insertValues(queue: BlockingQueue<RunnerData>){

        var record = queue.take()
        while(record.source != END_OF_SCRAPE){
            try {
                repository.save(record)
                logger.info("Inserted $record")
            } catch (e: Exception) {
                when (e) {
                    is ConstraintViolationException -> logger.debug("Validation Failure: $record")
                    is DataIntegrityViolationException -> logger.debug("Duplicate Entry: $record")
                    else -> logger.error("Exception: $record", e)
                }
            }
            record = queue.take()
        }
        logger.info("All items have been inserted")
    }
}