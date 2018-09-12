package com.stonesoupprogramming.marathonscrape.models

import com.stonesoupprogramming.marathonscrape.enums.Gender
import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import java.util.function.Function

interface EntityTransformer<T : ResultsPage> {
    fun toEntity(clazz: Class<T>): T
}

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
        val ageGender : Int,
        val splitFunc : Function<String, String>? = null,
        val backupAge : Int? = null,
        val backupGender: Int? = null) : AbstractColumnPositions(nationality, finishTime, place, halfwayTime)

abstract class AbstractScrapeInfo<T : AbstractColumnPositions, V : ResultsPage>(
        open val url : String,
        open val marathonSources: MarathonSources,
        open val marathonYear : Int,
        open val tableBodySelector : String,
        open val skipRowCount: Int,
        open val columnPositions: T,
        open val category : String?,
        open val gender: Gender?) : EntityTransformer<V> {

    override fun toEntity(clazz: Class<V>): V {
        val v = clazz.getDeclaredConstructor().newInstance()
        v.url = url
        v.source = marathonSources
        v.marathonYear = marathonYear
        v.category = category
        v.gender = gender ?: Gender.UNASSIGNED
        return v
    }
}

data class StandardScrapeInfo<T : AbstractColumnPositions, V : ResultsPage>(
        override val url : String,
        override val marathonSources: MarathonSources,
        override val marathonYear : Int,
        override val tableBodySelector : String,
        override val skipRowCount: Int,
        override val columnPositions: T,
        override val category : String?,
        override val gender: Gender?) : AbstractScrapeInfo<T, V>(url, marathonSources, marathonYear, tableBodySelector, skipRowCount, columnPositions, category, gender)

data class PagedScrapeInfo<T: AbstractColumnPositions>(
        override val url : String,
        override val marathonSources: MarathonSources,
        override val marathonYear : Int,
        override val tableBodySelector : String,
        override val skipRowCount: Int,
        override val columnPositions: T,
        val startPage : Int,
        val currentPage : Int,
        val endPage : Int,
        val clickNextSelector : String,
        val clickPreviousSelector : String,
        override val category : String?,
        override val gender : Gender?,
        val secondaryClickNextSelector: String?,
        val thirdClickNextSelector: String? = null) : AbstractScrapeInfo<T, NumberedResultsPage>(url, marathonSources, marathonYear, tableBodySelector, skipRowCount, columnPositions, category, gender) {

    override fun toEntity(clazz: Class<NumberedResultsPage>): NumberedResultsPage {
        val v = super.toEntity(clazz)
        v.pageNum = currentPage
        return v
    }
}