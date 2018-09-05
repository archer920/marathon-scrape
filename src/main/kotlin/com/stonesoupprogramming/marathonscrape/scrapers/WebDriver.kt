package com.stonesoupprogramming.marathonscrape.scrapers

import com.stonesoupprogramming.marathonscrape.extension.sleepRandom
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.Semaphore

@Component
class DriverFactory {

    private val logger = LoggerFactory.getLogger(DriverFactory::class.java)
    private val semaphore = Semaphore(Runtime.getRuntime().availableProcessors())

    fun createDriver(): RemoteWebDriver {
        return try {
            logger.info("Waiting on Permit")
            semaphore.acquire()
            logger.info("Permit Acquired")

            sleepRandom()
            ChromeDriver()
        } catch (e: Exception) {
            when (e) {
                is InterruptedException -> {
                    logger.error("Timeout while waiting for driver", e)
                    throw e
                }
                else -> FirefoxDriver()
            }
        }
    }

    fun destroy(driver: RemoteWebDriver) {
        try {
            semaphore.release()
            logger.info("Permit has been released")
            driver.close()
            System.gc()
        } catch (e: Exception) {
            logger.error("Failed to destroy driver", e)
        }
    }
}