package com.stonesoupprogramming.marathonscrape.enums

enum class Gender(val code : String){
    MALE("M"),
    FEMALE("W"),
    UNASSIGNED("U")
}

enum class MarathonSources(val arg : String, val endYear: Int = 2017, val startYear : Int = 2014){
    Philadelphia("--philadelphia"),
    Berlin("--berlin"),
    MyrtleBeach("--myrtle-beach"),
    Maritzburg("--maritzburg"),
    Unassigned("")
}