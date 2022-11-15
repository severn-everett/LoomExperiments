package com.severett.loomexperiments.loomvscoroutines

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.*

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(1)
open class LoomVsCoroutines {
    @Param(value = ["10", "100", "1000", "10000", "100000"])
    private var repeat: Int = 0
    // Simulating work in increasingly-heavier loads
    @Param(value = ["0", "10", "100"])
    private var delay: Long = 0

    @Benchmark
    fun loom(blackhole: Blackhole, threadPoolHelper: ThreadPoolHelper) {
        (0 until repeat).map { i ->
            threadPoolHelper.executorService.submit {
                Thread.sleep(delay)
                blackhole.consume(i)
            }
        }.forEach { it.get() }
    }

    @Benchmark
    fun coroutines(blackhole: Blackhole) {
        runBlocking {
            (0 until repeat).map { i ->
                launch {
                    delay(delay)
                    blackhole.consume(i)
                }
            }.forEach { it.join() }
        }
    }

    @Benchmark
    fun hybrid(blackhole: Blackhole, threadPoolHelper: ThreadPoolHelper) {
        runBlocking(threadPoolHelper.executorService.asCoroutineDispatcher()) {
            (0 until repeat).map { i ->
                launch {
                    delay(delay)
                    blackhole.consume(i)
                }
            }.forEach { it.join() }
        }
    }

    @State(Scope.Thread)
    open class ThreadPoolHelper {
        lateinit var executorService: ExecutorService

        @Setup(Level.Iteration)
        fun setup() {
            executorService = Executors.newVirtualThreadPerTaskExecutor()
        }

        @TearDown(Level.Iteration)
        fun teardown() {
            executorService.close()
        }
    }
}