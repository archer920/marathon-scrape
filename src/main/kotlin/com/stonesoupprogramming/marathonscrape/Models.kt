package com.stonesoupprogramming.marathonscrape

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter

data class RunnerData(var age : String = "",
                      var gender : String = "",
                      var nationality : String = "",
                      var finishTime : String = "",
                      var halfwayTime : String = "",
                       var company : String = "")

fun List<RunnerData>.writeToCsv(fileName : String){
    val printer = CSVPrinter(FileWriter(fileName), CSVFormat.DEFAULT.withHeader("Age",
            "Gender", "Nationality", "Finish Time", "Halfway Time", "Company, Team, or Sponsor"))
    this.forEach { printer.printRecord(it.age, it.gender, it.nationality, it.finishTime, it.halfwayTime, it.company) }
    printer.close()
}