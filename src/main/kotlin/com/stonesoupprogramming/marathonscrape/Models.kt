package com.stonesoupprogramming.marathonscrape

import javax.persistence.*
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

enum class MarathonSources(val cityName : String){
    Unassigned("Unassigned"),
    Berlin("Berlin"),
    Vienna("Vienna"),
    Boston("Boston"),
    Chicago("Chicago"),
    Nyc("New York City"),
    LosAngeles("Los Angeles"),
    Marines("Marines"),
    TwinCities("Medtronic"),
    Disney("Disney"),
    Ottawa("Ottawa"),
    Budapest("Budapest"),
    SanFranscisco("San Franscisco"),
    Melbourne("Melbourne"),
    Taipei("Taipei"),
    Yuengling("Yuengling"),
    Honolulu("Honolulu"),
    Jeruselm("Jeruselm"),
    Eversource("Eversource Hartford"),
    LittleRock("Little Rock"),
    FlyingPig("Flying Pig"),
    KentuckyDerby("Kentucky Derby"),
    Queenstown("Queenstown"),
    BigSur("Big Sur"),
    Toronto("Toronto")
    NewJersey("New Jersey")
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
data class GenderPagedResults(
        @field: Id @field: GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
        @field: Enumerated(EnumType.STRING) var source : MarathonSources = MarathonSources.Unassigned,
        @field: Min(2014) @field: Max(2018) var marathonYear : Int = 2014,
        @field: NotBlank var url : String = "",
        @field: Min(0) var pageNum: Int = 0,
        @field: NotNull var gender : Gender = Gender.UNASSIGNED)

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
        val url : String,
        val source: MarathonSources,
        val marathonYear: Int,
        val columnPositions: ColumnPositions,
        val tbodySelector : String ? = null,
        val rangeOptions : String? = null,
        val gender: Gender? = null)