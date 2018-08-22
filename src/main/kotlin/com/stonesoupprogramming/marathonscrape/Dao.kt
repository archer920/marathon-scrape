package com.stonesoupprogramming.marathonscrape

import org.springframework.data.jpa.repository.JpaRepository

interface RunnerDataRepository : JpaRepository<RunnerData, Long> {

    fun findByMarathonYearAndSourceOrderByAge(year : Int, source : String) : List<RunnerData>

    fun countBySource(source : MarathonSources) : Long
}

interface UrlPageRepository : JpaRepository<UrlPage, Long>{
    fun findBySource(source : MarathonSources) : List<UrlPage>
}

interface PagedResultsRepository : JpaRepository<PagedResults, Long>{
    fun findBySourceAndMarathonYear(source : MarathonSources, year : Int) : List<PagedResults>
}

interface GenderPagedResultsRepository : JpaRepository<GenderPagedResults, Long> {
    fun findBySource(source : MarathonSources) : List<GenderPagedResults>
}