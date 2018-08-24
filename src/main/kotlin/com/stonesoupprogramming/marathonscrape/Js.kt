package com.stonesoupprogramming.marathonscrape

import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader

interface JsDriver {
    fun readTableRows(driver: RemoteWebDriver, tbodySelector: String, trimResults: Boolean = true, rawHtml: Boolean = false): List<List<String>>
    fun readHtml(driver: RemoteWebDriver, elem: String): String
    fun elementIsPresent(driver: RemoteWebDriver, cssSelector: String): Boolean
    fun clickElement(driver: RemoteWebDriver, cssSelector: String)
    fun scrollToPage(driver: RemoteWebDriver, clickButtonSelector: String, pageNum: Int, sleepAmount: Long = 1000, secondClickButtonSelector : String = "" )
    fun injectJq(driver: RemoteWebDriver)
}

//NOTE: Do not inject a remote driver into this class because they are not thread safe
@Primary
@Component
class JsDriverImpl : JsDriver {


    private val logger = LoggerFactory.getLogger(JsDriverImpl::class.java)

    private val selector = "{{selector}}"

    private val jquery = BufferedReader(InputStreamReader(JsDriverImpl::class.java.getResourceAsStream("/js/jquery-3.3.1.js"))).readText()
    private val readTableJs = """
                let table = [];
                let rows = $('$selector').children('tr')
                rows.each((i, elem) => {
	                let cells = ${'$'}(elem).find('td')
                    let tableRow = [];
                    cells.each((j, cell) => {
  	                    tableRow.push(${'$'}(cell).text());
                    });
                    table.push(tableRow);
                });
                return table
            """.trimIndent()

    private val readTableJsHtml = """
                let table = [];
                let rows = $('$selector').children('tr')
                rows.each((i, elem) => {
	                let cells = ${'$'}(elem).find('td')
                    let tableRow = [];
                    cells.each((j, cell) => {
  	                    tableRow.push(${'$'}(cell).html());
                    });
                    table.push(tableRow);
                });
                return table
            """.trimIndent()

    private val presentJS = """
        return $('$selector').length !== 0;
    """.trimIndent()

    private val clickElementJs = """
        $('$selector').click();
    """.trimIndent()

    private val readHtmlJs = """
        return $('$selector').html()
    """.trimIndent()

    override fun injectJq(driver: RemoteWebDriver) {
        driver.executeScript(jquery)
    }

    override fun readTableRows(driver: RemoteWebDriver, tbodySelector: String, trimResults: Boolean, rawHtml: Boolean): List<List<String>> {
        val js = if (rawHtml) {
            readTableJsHtml.replace(selector, tbodySelector)
        } else {
            readTableJs.replace(selector, tbodySelector)
        }
        return try {
            driver.executeScript(jquery)
            Thread.sleep(1000)
            val results = driver.executeScript(js) as List<List<String>>
            if (trimResults) {
                return results.map { row ->
                    row.map { cell -> cell.trim() }
                }.toList()
            } else {
                results
            }
        } catch (e: Exception) {
            logger.error("Failed to execute readTableJs", e)
            throw e
        }
    }

    override fun readHtml(driver: RemoteWebDriver, elem: String): String {
        return try {
            driver.executeScript(readHtmlJs.replace(selector, elem)) as String
        } catch (e: Exception) {
            logger.error("Unable to return html", e)
            throw e
        }
    }

    override fun elementIsPresent(driver: RemoteWebDriver, cssSelector: String): Boolean {
        return try {
            driver.executeScript(jquery)
            driver.executeScript(presentJS.replace(selector, cssSelector)) as Boolean
        } catch (e: Exception) {
            logger.error("Failed to execute presentJs", e)
            throw e
        }
    }

    override fun clickElement(driver: RemoteWebDriver, cssSelector: String) {
        try {
            driver.executeScript(jquery)
            driver.executeScript(clickElementJs.replace(selector, cssSelector))
            sleepRandom(2, 5)
        } catch (e: Exception) {
            logger.error("Failed to execute clickElementJs", e)
            throw e
        }
    }

    override fun scrollToPage(driver: RemoteWebDriver, clickButtonSelector: String, pageNum: Int, sleepAmount: Long, secondClickButtonSelector : String) {
        try {
            var currentPage = 0
            while (currentPage < pageNum) {
                val selector = if(currentPage > 0 && secondClickButtonSelector.isNotBlank()) {
                    secondClickButtonSelector
                } else {
                    clickButtonSelector
                }
                clickElement(driver, selector)
                currentPage++
                Thread.sleep(sleepAmount)
            }
        } catch (e: Exception) {
            logger.error("Failed to advance to page: $pageNum", e)
            throw e
        }
    }
}

@Component
@Qualifier("athDriver")
class AthJsDriver(private val jsDriver: JsDriver) : JsDriver by jsDriver {

    private val logger = LoggerFactory.getLogger(AthJsDriver::class.java)

    private val extractInformationJs = """
        const results = []

        ${'$'}('.link-to-irp').each((i, elem) => {
            const parent = ${'$'}(elem).parent()
	        const row = ${'$'}(elem).find('.row')
            const spans = ${'$'}(row).find('.col-12').find('span')
            const px = ${'$'}(parent).find('.px-0')

            const place = ${'$'}(px[0]).text()
 	        const finishTime = ${'$'}(parent).find('.col-2').text()
            const gender = ${'$'}(spans[0]).text().split(' ')[0]
            const age = ${'$'}(spans[0]).text().split(' ')[1]
            const nationality = ${'$'}(spans[2]).text()

            results.push({place : place, finishTime : finishTime, gender : gender, age: age, nationality: nationality})
        });

        return results
    """.trimIndent()

    private val currentPageJs = """
        let buttons = []
        ${'$'}('#pager').find('button').each((i, elem) => {
	        let style = ${'$'}(elem).attr('style')
            let txt = ${'$'}(elem).text()

            buttons.push([style, txt])
        })
        return buttons
    """.trimIndent()

    fun readPage(driver: RemoteWebDriver) : List<Map<String, String>> {
        return try {
            injectJq(driver)
            driver.executeScript(extractInformationJs) as List<Map<String, String>>
        } catch (e : Exception){
            logger.error("Failed to extract page information", e)
            throw e
        }
    }

    fun findCurrentPage(driver: RemoteWebDriver) : Int {
        return try {
            injectJq(driver)
            val buttons = driver.executeScript(currentPageJs) as List<List<String>>
            var index = -1
            for(b in buttons){
                val parts = b[0].split(";")
                val result = parts.any { it == " color: rgb(255, 255, 255)" }
                if(result){
                    index = b[1].toInt()
                }
            }
            index
        } catch (e : Exception){
            logger.error("Can't determine the current page")
            throw e
        }
    }
}