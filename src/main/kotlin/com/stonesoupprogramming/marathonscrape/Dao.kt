package com.stonesoupprogramming.marathonscrape

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface RunnerDataRepository : JpaRepository<RunnerData, Long> {

    @Query("SELECT age, gender, nationality, finishTime, halfwayTime, company, marathonYear FROM RunnerData rd ORDER BY marathonYear, age, place")
    fun queryForExport(source : String) : List<RunnerData>
}