package com.stonesoupprogramming.marathonscrape

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.BlockingQueue
import javax.validation.ConstraintViolationException

@Component
class RunnerDataConsumer(@Autowired private val runnerDataQueue: BlockingQueue<RunnerData>,
                         @Autowired private val repository: RunnerDataRepository) {

    private val logger = LoggerFactory.getLogger(RunnerDataConsumer::class.java)
    var signalShutdown = false

    @Async
    fun insertValues() {

        try {
            var record = runnerDataQueue.take()

            insertLoop@while (true) {
                try {
                    repository.save(record)
                    logger.info("Inserted: $record")
                } catch (e: Exception) {
                    when (e) {
                        is ConstraintViolationException -> logger.info("Validation Failure: $record")
                        is DataIntegrityViolationException -> logger.info("Duplicate Entry: $record")
                        else -> logger.error("Exception: $record", e)
                    }
                }
                if(signalShutdown && runnerDataQueue.isEmpty()){
                    break@insertLoop
                } else {
                    record = runnerDataQueue.take()
                }
            }
        } catch (e : Exception){
            when (e){
                is InterruptedException -> logger.trace("Caught interrupted exception on signalShutdown")
                else -> logger.error("Exception in the consumer", e)
            }
        }
    }
}