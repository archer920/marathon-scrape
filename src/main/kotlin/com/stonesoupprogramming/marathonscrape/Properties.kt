package com.stonesoupprogramming.marathonscrape

import org.springframework.stereotype.Component
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

@Component
class MarathonProperties {
    object PropertyKeys {
        const val LAST_PAGE = "LAST_PAGE"
    }

    private val propertiesFileLocation = "${System.getProperty("user.home")}/.marathon.properties"

    val properties: Properties = Properties()

    init {
        if(!Files.exists(Paths.get(propertiesFileLocation))){
            Files.createFile(Paths.get(propertiesFileLocation))
        }
        properties.load(FileInputStream(Paths.get(propertiesFileLocation).toFile()))
    }

    fun saveProperties() {
        properties.store(FileOutputStream(Paths.get(propertiesFileLocation).toFile()), null)
    }
}