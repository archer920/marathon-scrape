package com.stonesoupprogramming.marathonscrape

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import javax.annotation.PreDestroy
import javax.validation.ConstraintViolationException

@Deprecated("All scraping classes should insert their own data")
@Component
class RunnerDataConsumer(@Autowired private val runnerDataQueue: BlockingQueue<RunnerData>,
                         @Autowired private val repository: RunnerDataRepository) {

    private val logger = LoggerFactory.getLogger(RunnerDataConsumer::class.java)
    var signalShutdown = false

    private var duplicates = mutableListOf<RunnerData>()
    private var validationFailures = mutableListOf<RunnerData>()

    @Async
    fun insertValues(): CompletableFuture<String> {
        try {
            do {
                insertRecord(true)
            } while(!signalShutdown)

            //Received shutdown signal so run until empty
            while(runnerDataQueue.isNotEmpty()){
                insertRecord(true)
            }
        } catch (e : Exception){
            when (e){
                is InterruptedException -> logger.trace("Caught interrupted exception on signalShutdown")
                else -> logger.error("Exception in the consumer", e)
            }
        }
        return successResult()
    }

    fun insertRecord(logInsert : Boolean = false){
        val record = runnerDataQueue.take()
        if(logInsert){
            logger.info("Inserting: $record")
        }
        try {
            repository.save(record)
        } catch (e: Exception) {
            when (e) {
                is ConstraintViolationException -> saveValidationFailure(record)
                is DataIntegrityViolationException -> saveDuplicate(record)
                else -> logger.error("Exception: $record", e)
            }
        }
    }

    @Synchronized
    fun saveDuplicate(runnerData: RunnerData){
        logger.debug("Duplicate record: $runnerData")
        duplicates.add(runnerData)
    }

    @Synchronized
    fun saveValidationFailure(runnerData : RunnerData){
        logger.debug("Validation Failure: $runnerData")
        validationFailures.add(runnerData)
    }

    @PreDestroy
    fun destroy(){
        duplicates.saveToCSV("Duplicates.csv")
        validationFailures.saveToCSV("Validation_Failures.csv")
    }
}