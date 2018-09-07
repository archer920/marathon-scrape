package com.stonesoupprogramming.marathonscrape.extension

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom

fun successResult(): CompletableFuture<String> {
    return CompletableFuture.completedFuture("Success")
}

fun failResult(): CompletableFuture<String> {
    return CompletableFuture.completedFuture("Error")
}

fun sleepRandom(min : Int = 5, max : Int = 60) {
    val amount = if(min == 0){
        1
    } else {
        1000 * ThreadLocalRandom.current().nextInt(min, max)
    }
    try {
        Thread.sleep(amount.toLong())
    } catch (e: Exception) {
    }
}