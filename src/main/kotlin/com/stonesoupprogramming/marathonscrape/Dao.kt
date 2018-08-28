package com.stonesoupprogramming.marathonscrape

import org.springframework.data.jpa.repository.JpaRepository

interface PagedRepository<T> {
    fun findBySource(source : MarathonSources) : List<T>

    fun findBySourceAndMarathonYear(source : MarathonSources, year : Int) : List<T>

}

interface RunnerDataRepository : JpaRepository<RunnerData, Long> {

    fun findByMarathonYearAndSourceOrderByAge(year : Int, source : MarathonSources) : List<RunnerData>

    fun countBySource(source : MarathonSources) : Long
}

interface UrlPageRepository : JpaRepository<UrlPage, Long>, PagedRepository<UrlPage>

interface PagedResultsRepository : JpaRepository<PagedResults, Long>, PagedRepository<PagedResults>

interface GenderPagedResultsRepository : JpaRepository<GenderPagedResults, Long>, PagedRepository<GenderPagedResults>

interface CategoryResultsRepository : JpaRepository<CategoryResults, Long>, PagedRepository<CategoryResults>