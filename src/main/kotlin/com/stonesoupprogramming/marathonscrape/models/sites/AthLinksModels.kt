package com.stonesoupprogramming.marathonscrape.models.sites

data class SequenceAthLinks(
        val year: Int,
        val url: String,
        val endPage: Int)

data class CategoryAthLinks(
        val year: Int,
        val url: String,
        val category: String,
        val divisionCss: String,
        val endPage: Int)