package com.stonesoupprogramming.marathonscrape.extension

import org.slf4j.Logger

fun Logger.printBlankLines(lines : Int = 2){
    for(i in 0 until lines){
        info("")
    }
}