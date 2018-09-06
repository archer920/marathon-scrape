package com.stonesoupprogramming.marathonscrape.enums

enum class Gender(val code : String){
    MALE("M"),
    FEMALE("W"),
    UNASSIGNED("U")
}

enum class MarathonSources(val arg : String, val endYear: Int = 2017, val startYear : Int = 2014){
    RockRollLasVegas("--rock-n-roll-las-vegas"),
    Istanbul("--istanbul"),
    Philadelphia("--philadelphia"),
    Belfast("--belfast"),
    Berlin("--berlin"),
    MyrtleBeach("--myrtle-beach"),
    Maritzburg("--maritzburg"),
    Cottonwood("--cottonwood"),
    Helsinki("--helsinki"),
    NoredaRiga("--noreda-riga"),
    PfChangsArizona("--pf-changs-arizona"),
    Unassigned("")
}