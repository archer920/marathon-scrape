package com.stonesoupprogramming.marathonscrape.models

import com.stonesoupprogramming.marathonscrape.enums.Gender
import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import java.util.function.BiFunction
import java.util.function.Function

interface EntityTransformer<T : ResultsPage> {
    fun toEntity(clazz: Class<T>): T
}

abstract class AbstractColumnPositions(
        open val nationality : Int,
        open val finishTime : Int,
        open val place : Int,
        open val halfwayTime : Int? = null,
        open val nationalityFunction: BiFunction<String, String, String>? = null,
        open val finishTimeFunction: BiFunction<String, String, String>? = null,
        open val placeFunction: BiFunction<String, String, String>? = null,
        open val halfwayTimeFunction: BiFunction<String, String, String>? = null,
        open val ageFunction: BiFunction<String, String, String>? = null,
        open val genderFunction: BiFunction<String, String, String>? = null,
        open val category: Int? = null)

data class AgeGenderColumnPositions(
        override val nationality: Int,
        override val finishTime: Int,
        override val place: Int,
        override val halfwayTime: Int? = null,
        override val nationalityFunction: BiFunction<String, String, String>? = null,
        override val finishTimeFunction: BiFunction<String, String, String>? = null,
        override val placeFunction: BiFunction<String, String, String>? = null,
        override val halfwayTimeFunction: BiFunction<String, String, String>? = null,
        val age : Int,
        val gender : Int,
        override val ageFunction: BiFunction<String, String, String>? = null,
        override val genderFunction: BiFunction<String, String, String>? = null,
        override val category: Int? = null) : AbstractColumnPositions(nationality, finishTime, place, halfwayTime, nationalityFunction, finishTimeFunction, placeFunction, halfwayTimeFunction, ageFunction, category = category)

data class MergedAgedGenderColumnPositions(
        override val nationality: Int,
        override val finishTime: Int,
        override val place: Int,
        override val halfwayTime: Int? = null,
        val ageGender : Int,
        @Deprecated("Use the ageFunction and genderFunction")
        val splitFunc : Function<String, String>? = null,
        val backupAge : Int? = null,
        val backupGender: Int? = null,
        override val nationalityFunction: BiFunction<String, String, String>? = null,
        override val finishTimeFunction: BiFunction<String, String, String>? = null,
        override val placeFunction: BiFunction<String, String, String>? = null,
        override val halfwayTimeFunction: BiFunction<String, String, String>? = null,
        override val ageFunction: BiFunction<String, String, String>? = null,
        override val genderFunction: BiFunction<String, String, String>? = null) : AbstractColumnPositions(nationality, finishTime, place, halfwayTime, nationalityFunction, finishTimeFunction, placeFunction, halfwayTimeFunction, ageFunction, genderFunction)

abstract class AbstractScrapeInfo<T : AbstractColumnPositions, V : ResultsPage>(
        open val url : String,
        open val marathonSources: MarathonSources,
        open val marathonYear : Int,
        open val tableBodySelector : String,
        open val skipRowCount: Int,
        open val columnPositions: T,
        open val category : String? = null,
        open val gender: Gender? = null,
        open val clipRows: Int = 0,
        open val tableRowFilter : Function<List<List<String>>, List<List<String>>>? = null) : EntityTransformer<V> {

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
        override val category : String? = null,
        override val gender: Gender? = null,
        override val clipRows: Int = 0,
        override val tableRowFilter : Function<List<List<String>>, List<List<String>>>? = null) : AbstractScrapeInfo<T, V>(url, marathonSources, marathonYear, tableBodySelector, skipRowCount, columnPositions, category, gender, clipRows, tableRowFilter)

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
        override val category: String? = null,
        override val gender: Gender? = null,
        val secondaryClickNextSelector: String? = null,
        val thirdClickNextSelector: String? = null,
        override val tableRowFilter : Function<List<List<String>>, List<List<String>>>? = null,
        override val clipRows: Int = 0) : AbstractScrapeInfo<T, NumberedResultsPage>(url, marathonSources, marathonYear, tableBodySelector, skipRowCount, columnPositions, category, gender, tableRowFilter = tableRowFilter, clipRows = clipRows) {

    override fun toEntity(clazz: Class<NumberedResultsPage>): NumberedResultsPage {
        val v = super.toEntity(clazz)
        v.pageNum = currentPage
        return v
    }
}

data class SequenceLinks(
        val year: Int,
        val url: String,
        val endPage: Int,
        val reloadHack: Boolean = false)