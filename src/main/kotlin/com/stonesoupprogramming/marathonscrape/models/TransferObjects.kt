package com.stonesoupprogramming.marathonscrape.models

import com.stonesoupprogramming.marathonscrape.enums.Gender
import com.stonesoupprogramming.marathonscrape.enums.MarathonSources

abstract class AbstractColumnPositions(
        open val nationality : Int,
        open val finishTime : Int,
        open val place : Int,
        open val halfwayTime : Int? = null)

data class AgeGenderColumnPositions(
        override val nationality: Int,
        override val finishTime: Int,
        override val place: Int,
        override val halfwayTime: Int? = null,
        val age : Int,
        val gender : Int) : AbstractColumnPositions(nationality, finishTime, place, halfwayTime)

data class MergedAgedGenderColumPositions(
        override val nationality: Int,
        override val finishTime: Int,
        override val place: Int,
        override val halfwayTime: Int? = null,
        val ageGender : Int) : AbstractColumnPositions(nationality, finishTime, place, halfwayTime)

abstract class AbstractScrapeInfo<T : AbstractColumnPositions>(
        open val url : String,
        open val marathonSources: MarathonSources,
        open val marathonYear : Int,
        open val tableBodySelector : String,
        open val columnPositions: T,
        open val category : String?,
        open val gender : Gender?)

data class StandardScrapeInfo<T: AbstractColumnPositions>(
        override val url : String,
        override val marathonSources: MarathonSources,
        override val marathonYear : Int,
        override val tableBodySelector : String,
        override val columnPositions: T,
        override val category : String?,
        override val gender : Gender?) : AbstractScrapeInfo<T>(url, marathonSources, marathonYear, tableBodySelector, columnPositions, category, gender)

data class PagedScrapeInfo<T: AbstractColumnPositions>(
        override val url : String,
        override val marathonSources: MarathonSources,
        override val marathonYear : Int,
        override val tableBodySelector : String,
        override val columnPositions: T,
        val startPage : Int,
        val currentPage : Int,
        val endPage : Int,
        val clickNextSelector : String,
        val clickPreviousSelector : String,
        override val category : String?,
        override val gender : Gender?,
        val secondaryClickNextSelector : String?) : AbstractScrapeInfo<T>(url, marathonSources, marathonYear, tableBodySelector, columnPositions, category, gender)