package com.stonesoupprogramming.marathonscrape

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import javax.persistence.*
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

//-1 will trigger IndexOutBoundsException so these are ok defaults
data class ColumnPositions(
        val nationality : Int = -1,
        val finishTime : Int = -1,
        val halfwayTime: Int = -1,
        val place : Int = -1,
        val age : Int = -1,
        val ageGender : Int = -1,
        val gender : Int = -1)

interface PageInfo{
    val url: String
    val columnPositions : ColumnPositions
    val marathonSources : MarathonSources
    val marathonYear : Int
    val tableBodySelector : String
    val headerRow : Boolean
}

data class UrlScrapeInfo(
        override val url : String,
        override val columnPositions: ColumnPositions,
        override val marathonSources: MarathonSources,
        override val marathonYear: Int,
        override val tableBodySelector: String,
        override val headerRow: Boolean)
    : PageInfo {

    fun toUrlResults(): UrlPage =
            UrlPage(source = marathonSources, marathonYear = marathonYear, url = url)
}

data class CategoryScrapeInfo(
        override val url : String,
        override val columnPositions: ColumnPositions,
        override val marathonSources: MarathonSources,
        override val marathonYear: Int,
        override val tableBodySelector: String,
        override val headerRow: Boolean,
        val category: String,
        val gender: Gender? = null,
        val raceSelection : String? = null)
    : PageInfo {

    fun toCategoryResults() =
            CategoryResults(source = marathonSources, marathonYear = marathonYear, url = url, category = category)
}

data class PagedResultsScrapeInfo(
        override val url : String,
        override val columnPositions: ColumnPositions,
        override val marathonSources: MarathonSources,
        override val marathonYear: Int,
        override val tableBodySelector: String,
        override val headerRow: Boolean,
        val category : String? = null,
        val startPage: Int,
        val endPage: Int,
        var currentPage: Int = startPage,
        val nextPageSelector: String,
        val backwardsSelector: String,
        val secondNextPageSelector: String? = null,
        val gender: Gender? = null)
    : PageInfo {

    fun toPagedResults() = PagedResults(source = marathonSources, marathonYear = marathonYear, url = url, pageNum = currentPage, category = category)
}

