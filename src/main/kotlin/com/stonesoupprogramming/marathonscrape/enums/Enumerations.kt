package com.stonesoupprogramming.marathonscrape.enums

enum class Gender(val code : String){
    MALE("M"),
    FEMALE("W"),
    UNASSIGNED("U")
}

enum class MarathonSources(val arg : String, val endYear: Int = 2017, val startYear : Int = 2014){
    Unassigned("Unassigned"),
    Stockholm("--stockholm", 2018),
    Amsterdam("--amsterdam", 2017),
    Santiago("--santiago", 2017),
    Berlin("--berlin", 2017),
    Buffalo("--buffalo", 2017),
    Taipei("--taipei", 2017),
    Copenhagen("--copenhagen", 2017),
    Geneva("--geneva", 2017),
    RheinEnergie("--rhein-energie", 2017),
    Bournemouth("--bournemouth", 2017),
    Memphis("--memphis", 2017),
    Indianapolis("--indianapolis", 2017),
    Munchen("--munchen", 2017),
    Fargo("--fargo", 2017),
    Bayshore("--bayshore", 2017),
    Brighton("--brighton", 2017),
    Vancouver("--vancouver", 2017),
    Philadelphia("--philadelphia", 2017),
    Venice("--venice", 2017),
    Dusseldorf("--dusseldorf", 2017),
    SurfCity("--surf-city", 2017),
    Liverpool("--liverpool", 2017),
    SanDiego("--san-diego", 2017),
    Akron("--akron", 2017),
    RiverRock("--river-rock", 2017),
    Route66("--route-66", 2017),
    ChaingMai("--chaing-mai", startYear = 2015, endYear = 2017)
}