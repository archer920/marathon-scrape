package com.stonesoupprogramming.marathonscrape.models

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.UNAVAILABLE
import com.stonesoupprogramming.marathonscrape.enums.Gender
import org.slf4j.Logger
import javax.persistence.*
import javax.persistence.CascadeType.ALL
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

@Inheritance
open class ResultsPage(
        @field: Id @field: GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
        @field: Enumerated(EnumType.STRING) var source : MarathonSources = MarathonSources.Unassigned,
        @field: Min(2014) @field: Max(2018) var marathonYear : Int = 2014,
        @field: OneToMany(cascade = [ALL], orphanRemoval = true) var runnerData: Set<RunnerData> = emptySet(),
        @field: NotBlank var url : String = "",
        var category: String? = null,
        @field: Enumerated(EnumType.STRING) var gender : Gender = Gender.UNASSIGNED)

@Entity
class NumberedResultsPage(@field: Min(0) var pageNum: Int = 0) : ResultsPage()

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

    companion object {
        fun createRunnerData(logger: Logger, age: String, finishTime: String, gender: String, year: Int, nationality: String, place: Int, source: MarathonSources, company: String = "", halfwayTime: String = ""): RunnerData {
            check(age.isNotBlank()) { "age cannot be blank. Mark as $UNAVAILABLE if not present" }
            check(finishTime.isNotBlank()) { "finishTime cannot be blank. Mark as $UNAVAILABLE if not present" }
            check(gender.isNotBlank()) { "gender cannot be blank. Mark as $UNAVAILABLE if not present" }
            check(year in 2014..2018) { "The year has to be between 2014-2018" }
            check(nationality.isNotBlank()) { "nationality cannot be blank. Mark as $UNAVAILABLE if not present" }
            check(place > 0) { "Place can't be negative. Use Int.MAX if it isn't present" }
            check(source != MarathonSources.Unassigned) { "source has to be assigned" }

            val runnerData = RunnerData(
                    source = source,
                    age = age,
                    gender = gender,
                    nationality = nationality,
                    finishTime = finishTime,
                    halfwayTime = halfwayTime,
                    company = company,
                    marathonYear = year,
                    place = place
            )
            logger.info("Produced: $runnerData")
            return runnerData
        }
    }
}