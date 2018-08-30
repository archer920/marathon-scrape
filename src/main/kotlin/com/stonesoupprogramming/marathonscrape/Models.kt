package com.stonesoupprogramming.marathonscrape

import javax.persistence.*
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

enum class MarathonSources(val arg : String, val endYear: Int = 2017, val startYear : Int = 2014){
    Unassigned("Unassigned"),
    Stockholm("--stockholm", 2018),
    Amsterdam("--amsterdam", 2017),
    Santiago("--santiago", 2017),
    RheinEnergie("--rhein-energie", 2017),
    Copenhagen("--copenhagen", 2017),
    Bournemouth("--bournemouth", 2017),
    Memphis("--memphis", 2017),
    Indianapolis("--indianapolis", 2017)
}

enum class Gender(val code : String){
    MALE("M"),
    FEMALE("W"),
    UNASSIGNED("U")
}

@Entity
data class RunnerData(
        @field: Id @field: GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
        @field: Enumerated(EnumType.STRING) var source : MarathonSources = MarathonSources.Unassigned,
        @field: NotBlank @field: Column(nullable=false) var age : String = "",
        @field: NotBlank @field: Column(nullable=false) var gender : String = "",
        @field: NotBlank @field: Column(nullable=false) var nationality : String = "",
        @field: NotBlank @field: Column(nullable=false) var finishTime : String = "",
        var halfwayTime : String = "",
        var company : String = "",
        @field: Column(nullable=false) @field: Min(2014) @field: Max(2018) var marathonYear : Int = 2014,
        @field: Column(nullable=false) @field: Min(1) var place : Int = 1) {

    override fun toString(): String {
        return "RunnerData(place=$place, Year=$marathonYear, id=$id, source='$source', age='$age', gender='$gender', nationality='$nationality', finishTime='$finishTime', halfwayTime='$halfwayTime', company='$company')"
    }
}

@Entity
data class UrlPage(
        @field: Id @field: GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
        @field: Enumerated(EnumType.STRING) var source : MarathonSources = MarathonSources.Unassigned,
        @field: Min(2014) @field: Max(2018) var marathonYear : Int = 2014,
        @field: NotBlank var url : String = ""
)

@Entity
data class PagedResults(
        @field: Id @field: GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
        @field: Enumerated(EnumType.STRING) var source : MarathonSources = MarathonSources.Unassigned,
        @field: Min(2014) @field: Max(2018) var marathonYear : Int = 2014,
        @field: NotBlank var url : String = "",
        @field: Min(0) var pageNum: Int = 0)

@Entity
data class CategoryResults(
        @field: Id @field: GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
        @field: Enumerated(EnumType.STRING) var source : MarathonSources = MarathonSources.Unassigned,
        @field: Min(2014) @field: Max(2018) var marathonYear : Int = 2014,
        @field: NotBlank var url : String = "",
        @field: NotBlank var category: String = "") {

    fun matches(categoryScrapeInfo: CategoryScrapeInfo) =
            this.url == categoryScrapeInfo.url &&
                    this.marathonYear == categoryScrapeInfo.marathonYear &&
                    this.source == categoryScrapeInfo.marathonSources &&
                    this.category == categoryScrapeInfo.category
}

@Entity
data class GenderPagedResults(
        @field: Id @field: GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
        @field: Enumerated(EnumType.STRING) var source : MarathonSources = MarathonSources.Unassigned,
        @field: Min(2014) @field: Max(2018) var marathonYear : Int = 2014,
        @field: NotBlank var url : String = "",
        @field: Min(0) var pageNum: Int = 0,
        @field: NotNull var gender : Gender = Gender.UNASSIGNED)

interface PageInfo{
    val url: String
    val columnPositions : ColumnPositions
    val marathonSources : MarathonSources
    val marathonYear : Int
}

//-1 will trigger IndexOutBoundsException so these are ok defaults
data class ColumnPositions(
        val nationality : Int = -1,
        val finishTime : Int = -1,
        val halfwayTime: Int = -1,
        val place : Int = -1,
        val age : Int = -1,
        val ageGender : Int = -1,
        val gender : Int = -1)

data class UrlScrapeInfo(
        override val url : String,
        override val marathonSources: MarathonSources,
        override val marathonYear: Int,
        override val columnPositions: ColumnPositions,
        val tbodySelector : String ? = null,
        val rangeOptions : String? = null,
        val gender: Gender? = null) : PageInfo

data class CategoryScrapeInfo(
        override val url : String,
        override val marathonSources: MarathonSources,
        override val marathonYear: Int,
        override val columnPositions: ColumnPositions,
        val category: String,
        val gender: Gender? = null,
        val raceSelection : String? = null) : PageInfo {

    fun toCategoryResults() =
            CategoryResults(source = marathonSources, marathonYear = marathonYear, url = url, category = category)
}

data class PagedResultsScrapeInfo(
        override val url : String,
        override val marathonSources: MarathonSources,
        override val marathonYear: Int,
        override val columnPositions: ColumnPositions,
        val startPage : Int,
        val endPage: Int,
        val nextPageSelector: String,
        val backwardsSelector : String,
        val tableBodySelector : String,
        val headerRow: Boolean = true,
        val tableFrame : String? = null,
        val secondNextPageSelector : String? = null,
        val rangeOptions : String? = null,
        val comboBoxValue : String? = null,
        val comboBoxSelector : String? = null,
        val comboBoxFrame : String? = null,
        val gender: Gender? = null) : PageInfo {

    fun toPagedResults(currentPage : Int) = PagedResults(source = marathonSources, marathonYear = marathonYear, url = url, pageNum = currentPage)
}