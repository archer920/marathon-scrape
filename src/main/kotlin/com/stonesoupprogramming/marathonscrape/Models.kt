package com.stonesoupprogramming.marathonscrape

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter
import javax.persistence.*
import javax.validation.constraints.Min
import javax.validation.constraints.Max
import javax.validation.constraints.NotBlank

object Sources {
    const val BERLIN="Berlin Marathon"
    const val VIENNA = "Vienna City Marathon"
    const val BOSTON = "Boston Marathon"
    const val CHICAGO = "Chicago"
    const val NY_MARATHON_GUIDE = "Ny Marathon Guide"
    const val LA = "La Marathon"
    const val MARINES = "Marine Corp"
    const val SAN_FRANSCISO = "San Fransisco"
    const val MEDTRONIC = "Medtronic Twin Cities Marathon"
}

enum class Gender(val code : String){
    MALE("M"),
    FEMALE("W")
}

@Entity
data class RunnerData(
        @field: Id @field: GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
        @field: NotBlank @field: Column(nullable=false) var source : String = "",
        @field: NotBlank @field: Column(nullable=false) var age : String = "",
        @field: NotBlank @field: Column(nullable=false) var gender : String = "",
        @field: NotBlank @field: Column(nullable=false) var nationality : String = "",
        @field: NotBlank @field: Column(nullable=false) var finishTime : String = "",
        var halfwayTime : String = "",
        var company : String = "",
        @field: Column(nullable=false) @field: Min(2014) @field: Max(2018) var marathonYear : Int = 2014,
        @field: Column(nullable=false) @field: Min(1) var place : Int = 1,
        @field: Column(unique = true, nullable = false) @field: NotBlank var raceYearPlace : String = "") {
    override fun toString(): String {
        return "RunnerData(place=$place, Year=$marathonYear, id=$id, source='$source', age='$age', gender='$gender', nationality='$nationality', finishTime='$finishTime', halfwayTime='$halfwayTime', company='$company', raceYearPlace=$raceYearPlace)"
    }

    fun updateRaceYearPlace(){
        raceYearPlace = hashCode().toString()
    }
}

fun List<RunnerData>.writeToCsv(fileName : String){
    val printer = CSVPrinter(FileWriter(fileName), CSVFormat.DEFAULT.withHeader("Age",
            "Gender", "Nationality", "Finish Time", "Halfway Time", "Company, Team, or Sponsor", "Year"))
    this.forEach { printer.printRecord(it.age, it.gender, it.nationality, it.finishTime, it.halfwayTime, it.company, it.marathonYear) }
    printer.close()
}