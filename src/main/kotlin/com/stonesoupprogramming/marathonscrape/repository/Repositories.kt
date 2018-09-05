package com.stonesoupprogramming.marathonscrape.repository

import com.stonesoupprogramming.marathonscrape.enums.MarathonSources
import com.stonesoupprogramming.marathonscrape.models.NumberedResultsPage
import com.stonesoupprogramming.marathonscrape.models.ResultsPage
import com.stonesoupprogramming.marathonscrape.models.RunnerData
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface RunnerDataRepository : JpaRepository<RunnerData, Long> {

    fun findByMarathonYearAndSourceOrderByAge(year : Int, source : MarathonSources) : List<RunnerData>

    fun countBySource(source : MarathonSources) : Long
}

interface ResultsRepository<T : ResultsPage> : JpaRepository<T, Long> {
    fun findBySource(source: MarathonSources): List<T>
}

interface NumberedResultsPageRepository : ResultsRepository<NumberedResultsPage> {

    @Query("select max(nrp.pageNum) from NumberedResultsPage nrp where nrp.marathonYear = :year and nrp.source = :source")
    fun queryLastPage(@Param("year") year: Int, @Param("source") source: MarathonSources): Int

    @Query("select max(nrp.pageNum) from NumberedResultsPage nrp where nrp.marathonYear = :year and nrp.source = :source and nrp.category = :category")
    fun queryLastPage(@Param("year") year: Int, @Param("source") source: MarathonSources, @Param("category") category: String): Int
}