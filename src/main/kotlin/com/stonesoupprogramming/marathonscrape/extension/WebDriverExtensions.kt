package com.stonesoupprogramming.marathonscrape.extension

import org.openqa.selenium.By
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun RemoteWebDriver.selectComboBoxOption(selector: By, value: String, logger: Logger = LoggerFactory.getLogger(RemoteWebDriver::class.java)) {
    try {
        Select(this.findElement(selector)).selectByVisibleText(value)
    } catch (e : Exception) {
        logger.error("Unable to select value $value", e)
        throw e
    }
}

fun RemoteWebDriver.waitUntilClickable(selector: By, timeout: Long = 60, attemptNum: Int = 0, giveUp: Int = 60) {
    try {
        WebDriverWait(this, timeout).until(ExpectedConditions.elementToBeClickable(selector))
    } catch (e : Exception){
        if(attemptNum < giveUp){
            this.waitUntilClickable(selector, timeout, attemptNum + 1, giveUp)
        } else {
            throw e
        }
    }
}

fun RemoteWebDriver.waitUntilVisible(selector: By, attemptNum: Int = 0, giveUp: Int = 60) {
    try {
        WebDriverWait(this, 10).until(ExpectedConditions.visibilityOf(this.findElement(selector)))
    } catch (e : Exception){
        if(attemptNum < giveUp){
            Thread.sleep(1000)
            waitUntilVisible(selector, attemptNum = attemptNum + 1)
        } else {
            throw e
        }
    }
}

fun RemoteWebDriver.click(element: By, logger: Logger) {
    try {
        this.waitUntilClickable(element)
        this.findElement(element).click()
    } catch (e: Exception) {
        logger.error("Failed to click element", e)
        throw e
    }
}