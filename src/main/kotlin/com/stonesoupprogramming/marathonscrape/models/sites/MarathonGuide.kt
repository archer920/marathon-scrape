package com.stonesoupprogramming.marathonscrape.models.sites

import com.stonesoupprogramming.marathonscrape.models.MergedAgedGenderColumPositions

data class MarathonGuideInfo(
        val url : String,
        val year : Int,
        val columnPositions: MergedAgedGenderColumPositions,
        val numRecords : Int,
        val categoryIncrement : Int = 100)