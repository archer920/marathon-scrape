package com.stonesoupprogramming.marathonscrape.models.sites

data class CategoryAthLinks(
        val year: Int,
        val url: String,
        val category: String,
        val divisionCss: String,
        val endPage: Int) {

    companion object {
        fun selectOption(num : Int) : String =
                "#option-$num > div > div > div"
    }
}