package com.stonesoupprogramming.marathonscrape

import javax.persistence.*
import javax.validation.constraints.Max
import javax.validation.constraints.Min
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
    const val DISNEY = "Disney World Marathon"
    const val HONOLULU = "Honolulu Marathon"
    const val OTTAWA = "Ottawa Marathon"
    const val BUDAPEST = "Budapest"
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
        raceYearPlace = "$source,$age,$gender,$nationality,$finishTime,$halfwayTime,$company,$marathonYear,$place"
    }
}

//-1 will trigger IndexOutBoundsException so these are ok defaults
data class ColumnPositions(
        val nationality : Int = -1,
        val finishTime : Int = -1,
        val place : Int = -1,
        val age : Int = -1,
        val ageGender : Int = -1,
        val gender : Int = -1)