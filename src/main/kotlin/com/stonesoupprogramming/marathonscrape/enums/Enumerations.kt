package com.stonesoupprogramming.marathonscrape.enums

import com.stonesoupprogramming.marathonscrape.extension.UNAVAILABLE

enum class Gender(val code : String){
    MALE("M"),
    FEMALE("W"),
    UNASSIGNED(UNAVAILABLE);

    object Lookup {
        fun fromCode(code : String) : Gender{
            return when(code) {
                "女" -> FEMALE
                "男" -> MALE
                else -> throw IllegalArgumentException("No gender associated with $code")
            }
        }
    }
}

enum class MarathonSources(val arg : String, val endYear: Int = 2017, val startYear : Int = 2014){
    RoadToHopeHamilton("--road-to-hope-hamilton"),
    RevelCanyonCity("--revel-canyon-city"),
    CharlottesThunderRoad("--charlottes-thunder-road"),
    Ventura("--ventura"),
    NiagraFalls("--niagra-falls"),
    RiteAidCleveland("--rite-ait-cleveland"),
    SaltLakeCity("--salt-lake-city"),
    Georgia("--georgia"),
    Maine("--maine"),
    TelAviv("--tel-aviv"),
    TaipeiStandardChartered("--taipei-standard-chartered"),
    Ergebnis("--ergebnis"),
    PKO("--pko"),
    Treviso("--treviso"),
    Rome("--rome"),
    Florence("--florence"),
    Oslo("--oslo"),
    Ljubljanski("--ljubljanski"),
    Dublin("--dublin"),
    Padova("--padova"),
    London("--london"),
    Turin("--turin"),
    Barcelona("--barcelona"),
    Taipei("--taipei"),
    Berlin("--berlin"),
    Ottawa("--ottawa"),
    Kaiser("--kaiser"),
    Snowdonia("--snowdonia"),
    Cracovia("--cracovia"),
    Columbus("--columbus"),
    SanSebastian("--san-sebastian"),
    Freiburg("--freiburg"),
    Milano("--milano"),
    Hca("--hca"),
    Dresden("--dresden"),
    Woodlands("--woodlands"),
    Axexander("--alexander-the-great"),
    Jungfrau("--junfrau"),
    Hamburg("--hamburg"),
    Antwerp("--antwerp"),
    Singapore("--singapore"),
    Athens("--athens"),
    Dubai("--dubai"),
    Capetown("--capetown"),
    Auckland("--auckland"),
    WhiteKnightInternational("--white-knight-international"),
    Eindhoven("--eindhoven"),
    EdpPorto("--edp-porto"),
    BaxtersLochNess("--baxters-loch-ness"),
    SwissCity("--swiss-city"),
    Ogden("--ogden"),
    Frankfurt("--frankfurt"),
    Rotterdam("--rotterdam"),
    Edinburgh("--edinburgh"),
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
    California("--california"),
    RocketCity("--rocket-city"),
    Dallas("--dallas"),
    Charleston("--charleston"),
    Carlsbad("--carlsbad"),
    NewOrleans("--new-orleans"),
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
    MyrtleBeach("--myrtle-beach"),
    Maritzburg("--maritzburg"),
    Cottonwood("--cottonwood"),
    Helsinki("--helsinki"),
    NoredaRiga("--noreda-riga"),
    PfChangsArizona("--pf-changs-arizona"),
    Unassigned("")
}