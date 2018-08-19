package com.stonesoupprogramming.marathonscrape

import org.springframework.data.jpa.repository.JpaRepository

interface RunnerDataRepository : JpaRepository<RunnerData, Long> {

    fun findByMarathonYearAndSourceOrderByAge(year : Int, source : String) : List<RunnerData>

    fun countBySource(source : String) : Long
}

interface UrlPageRepository : JpaRepository<UrlPage, Long>{
    fun findBySource(source : String) : List<UrlPage>
}