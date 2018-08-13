package com.stonesoupprogramming.marathonscrape

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface RunnerDataRepository : JpaRepository<RunnerData, Long> {

    //@Query("SELECT age, gender, nationality, finishTime, halfwayTime, company, marathonYear FROM RunnerData rd where rd.year = :? and rd.source = ? ORDER BY marathonYear, age, place")
    //fun queryForExport(source : String, year: Int) : List<RunnerData>
    fun findByMarathonYearAndSourceOrderByAge(year : Int, source : String) : List<RunnerData>
}