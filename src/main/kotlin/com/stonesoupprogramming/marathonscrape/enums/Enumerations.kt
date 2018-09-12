package com.stonesoupprogramming.marathonscrape.enums

enum class Gender(val code : String){
    MALE("M"),
    FEMALE("W"),
    UNASSIGNED("U")
}

enum class MarathonSources(val arg : String, val endYear: Int = 2017, val startYear : Int = 2014){
    SwissCity("--swiss-city"),
    Ogden("--ogden"),
    Dubai("--dubai"),
    Frankfurt("--frankfurt"),
    Athens("--athens"),
    Rotterdam("--rotterdam"),
    Capetown("--capetown", startYear = 2015),
    Edinburgh("--edinburgh"),
    Taipei("--taipei"),
    //NOTE: Add above this line
    GreaterManchester("--greater-manchester"),
    Luxemburg("--luxemburg"),
    Tallinn("--tallinn"),
    Pisa("--pisa"),
    Hannover("--hannover"),
    Brussels("--brussels"),
    Toronto("--toronto"),
    Sydney("--sydney"),
    VolkswagenPrague("--volkswagen-prague"),
    Yorkshire("--yorkshire"),
    Ikano("--ikano"),
    Steamtown("--steamtown"),
    DesMoines("--des-moines"),
    AirForce("--air-force"),
    Ralaeigh("--raleigh"),
    CountryMusicFestival("--country-music-festival"),
    StGeorge("--st-george"),
    KansasCity("--kansas-city"),
    Baltimore("--baltimore"),
    GrandRapids("--grand-rapids"),
    RockNRollSavannah("--rock-n-roll-savannah"),
    AnthemRichmond("--anthem-richmond"),
    TobaccoRoad("--tobacco-road"),
    RockNRollUSA("--rock-n-roll-usa"),
    Madison("--madison"),
    Victoria("--victoria"),
    QuebecCity("--quebec-city"),
    KiawahIsland("--kiawah-island"),
    SantaRose("--santa-rose"),
    Baystate("--baystate"),
    Canberra("--canberra"),
    Chester("--chester"),
    Snowdonia("--snowdonia"),
    California("--california"),
    RocketCity("--rocket-city"),
    Dallas("--dallas"),
    Charleston("--charleston"),
    Carlsbad("--carlsbad"),
    NewOrleans("--new-orleans"),
    Woodlands("--woodlands"),
    Phoenix("--phoenix"),
    NapaValley("--napa-valley"),
    Illinois("--illinois"),
    OklahomaCity("--oklahoma-city"),
    GlassCity("--glass-city"),
    Rotorua("--rotorua"),
    Pittsburgh("--pittsburg"),
    OC("--oc"),
    Seattle("--seattle"),
    Miami("--miami"),
    Portland("--portland"),
    Lincoln("--lincoln"),
    CoxSports("--cox-sports"),
    MiltonKeynes("--milton-keynes"),
    Burlington("--burlington"),
    MountainsToBeach("--mountains-to-beach"),
    ChiangMai("--chaing-mai"),
    CorkCity("--cork-city"),
    UtahValley("--utah-valley"),
    Missoula("--missoula"),
    Erie("--erie-marathon"),
    Seamtown("--seamtown"),
    Mohawk("--mohawk"),
    StLouis("--st-louis"),
    RockRollLasVegas("--rock-n-roll-las-vegas"),
    LongBeach("--long-beach"),
    PoweradeMonterrery("--powerade-monterrey"),
    Milwaukee("--milwaukee"),
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