package com.stonesoupprogramming.marathonscrape.models.sites

import com.stonesoupprogramming.marathonscrape.enums.Gender
import com.stonesoupprogramming.marathonscrape.models.SequenceLinks
import com.stonesoupprogramming.marathonscrape.scrapers.sites.TdsLivePreWebScrapeEvent

data class TdsScrapeInfo(val gender: Gender,
                         val preWebScrapeEvent: TdsLivePreWebScrapeEvent?,
                         val sequenceLinks: SequenceLinks)