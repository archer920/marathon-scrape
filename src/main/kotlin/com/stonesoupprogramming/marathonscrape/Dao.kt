package com.stonesoupprogramming.marathonscrape

import org.springframework.data.mongodb.repository.MongoRepository

interface NyRunnerDataRepository : MongoRepository<NyRunnerData, String>