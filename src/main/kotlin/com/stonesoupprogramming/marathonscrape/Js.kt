package com.stonesoupprogramming.marathonscrape

import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader

//NOTE: Do not inject a remote driver into this class because they are not thread safe
@Component
class JsDriver {

    private val logger = LoggerFactory.getLogger(JsDriver::class.java)

    private val selector = "{{selector}}"
    private val arg = "{{args}}"

    private val jquery = BufferedReader(InputStreamReader(JsDriver::class.java.getResourceAsStream("/js/jquery-3.3.1.js"))).readText()
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


    fun readTableRows(driver : RemoteWebDriver, tbodySelector : String, trimResults : Boolean = true, rawHtml : Boolean = false) : List<List<String>> {
        val js = if(rawHtml){
            readTableJsHtml.replace(selector, tbodySelector)
        } else {
            readTableJs.replace(selector, tbodySelector)
        }
        return try {
            driver.executeScript(jquery)
            Thread.sleep(1000)
            val results = driver.executeScript(js) as List<List<String>>
            if(trimResults) {
                return results.map { row ->
                    row.map { cell -> cell.trim() }
                }.toList()
            } else {
                results
            }
        } catch (e : Exception){
            logger.error("Failed to execute readTableJs", e)
            throw e
        }
    }

    fun elementIsPresent(driver: RemoteWebDriver, cssSelector: String) : Boolean {
        return try {
            driver.executeScript(jquery)
            driver.executeScript(presentJS.replace(selector, cssSelector)) as Boolean
        } catch (e : Exception){
            logger.error("Failed to execute presentJs", e)
            throw e
        }
    }

    fun clickElement(driver: RemoteWebDriver, cssSelector: String){
        try {
            driver.executeScript(jquery)
            driver.executeScript(clickElementJs.replace(selector, cssSelector))
        } catch (e: Exception){
            logger.error("Failed to execute clickElementJs", e)
            throw e
        }
    }

    fun scrollToPage(driver: RemoteWebDriver, clickButtonSelector : String, pageNum : Int, sleepAmount : Long = 1000){
        try {
            var currentPage = 0
            while(currentPage < pageNum){
                clickElement(driver, clickButtonSelector)
                currentPage++
                Thread.sleep(sleepAmount)
            }
        } catch (e : Exception){
            logger.error("Failed to advance to page: $pageNum", e)
            throw e
        }
    }
}