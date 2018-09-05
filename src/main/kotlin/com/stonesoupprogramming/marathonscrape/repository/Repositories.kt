package com.stonesoupprogramming.marathonscrape.repository

import com.stonesoupprogramming.marathonscrape.*
import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.RunnerData
import org.springframework.data.jpa.repository.JpaRepository

interface PagedRepository<T> {
    fun findBySource(source : MarathonSources) : List<T>

    fun findBySourceAndMarathonYearAndCategory(source : MarathonSources, year : Int, category : String? = null) : List<T>

}

interface RunnerDataRepository : JpaRepository<RunnerData, Long> {

    fun findByMarathonYearAndSourceOrderByAge(year : Int, source : MarathonSources) : List<RunnerData>

    fun countBySource(source : MarathonSources) : Long
}

interface UrlPageRepository : JpaRepository<UrlPage, Long>, PagedRepository<UrlPage> {
    companion object {
        try {

        }
    }
}

interface PagedResultsRepository : JpaRepository<PagedResults, Long>, PagedRepository<PagedResults>

interface GenderPagedResultsRepository : JpaRepository<GenderPagedResults, Long>, PagedRepository<GenderPagedResults>

interface CategoryResultsRepository : JpaRepository<CategoryResults, Long>, PagedRepository<CategoryResults>

