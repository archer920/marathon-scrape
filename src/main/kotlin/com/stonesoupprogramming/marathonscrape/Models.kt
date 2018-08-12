package com.stonesoupprogramming.marathonscrape

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.io.FileWriter

@Document
data class NyRunnerData(
            @field: Id var id: String? = null,
            var source : String = "",
            var age : String = "",
            var gender : String = "",
            var nationality : String = "",
            var finishTime : String = "",
            var halfwayTime : String = "",
            var company : String = "",
            var year : Int = 2014,
            var place : Int = 1,
            @field: Indexed(unique = true) var yearPlace : String = "") {
    override fun toString(): String {
        return "NyRunnerData(place=$place,  year=$year, id=$id, source='$source', age='$age', gender='$gender', nationality='$nationality', finishTime='$finishTime', halfwayTime='$halfwayTime', company='$company')"
    }
}

fun List<NyRunnerData>.writeToCsv(fileName : String){
    val printer = CSVPrinter(FileWriter(fileName), CSVFormat.DEFAULT.withHeader("Age",
            "Gender", "Nationality", "Finish Time", "Halfway Time", "Company, Team, or Sponsor", "Year"))
    this.forEach { printer.printRecord(it.age, it.gender, it.nationality, it.finishTime, it.halfwayTime, it.company, it.year) }
    printer.close()
}