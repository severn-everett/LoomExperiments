package com.severett.loomexperiments.loomvscoroutines

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.*
import java.util.concurrent.atomic.*
import kotlin.coroutines.CoroutineContext

private const val ROUNDS = 5
private const val REPETITIONS = 10000
private const val DELAY_AMT = 10L

fun main() {
    runControlTest()

    runPoolTest("Naive") { coroutineContext ->
        var counter = 0
        runBlocking(coroutineContext) {
            repeat(REPETITIONS) {
                launch {
                    delay(DELAY_AMT)
                    counter++
                }
            }
        }
        counter
    }

    runPoolTest("Atomic Integer") { coroutineContext ->
        val counter = AtomicInteger(0)
        runBlocking(coroutineContext) {
            repeat(REPETITIONS) {
                launch {
                    delay(DELAY_AMT)
                    counter.incrementAndGet()
                }
            }
        }
        counter.get()
    }

    runPoolTest("Confinement") { coroutineContext ->
        val counterContext = newSingleThreadContext("CounterContext")
        var counter = 0
        runBlocking(coroutineContext) {
            repeat(REPETITIONS) {
                launch {
                    withContext(counterContext) {
                        counter++
                    }
                }
            }
        }
        counter
    }

    runPoolTest("Mutex") { coroutineContext ->
        val mutex = Mutex()
        var counter = 0
        runBlocking(coroutineContext) {
            repeat(REPETITIONS) {
                launch {
                    mutex.withLock {
                        counter++
                    }
                }
            }
        }
        counter
    }
}

private fun runControlTest() {
    executeTest("Control") {
        val counter = AtomicInteger(0)
        runBlocking {
            repeat(REPETITIONS) {
                launch {
                    delay(DELAY_AMT)
                    counter.incrementAndGet()
                }
            }
        }
        counter.get()
    }
}

private fun runPoolTest(title: String, block: (CoroutineContext) -> Int) {
    Executors.newVirtualThreadPerTaskExecutor().use { threadPool ->
        executeTest(title) {
            block.invoke(threadPool.asCoroutineDispatcher())
        }
    }
}

private fun executeTest(title: String, block: () -> Int) {
    println("Test: $title")
    val successes = (0 until ROUNDS).sumOf { i ->
        val result = block.invoke()
        println("Round ${i + 1} result: $result")
        if (result == REPETITIONS) 1 else 0L
    }
    println("Successes: $successes/$ROUNDS\n")
}
