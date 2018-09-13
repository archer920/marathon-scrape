package com.stonesoupprogramming.marathonscrape.models.sites

import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumnPositions

data class MarathonGuideInfo(
        val url : String,
        val year : Int,
        val columnPositions: MergedAgedGenderColumnPositions,
        val numRecords : Int,
        val categoryIncrement : Int = 100)