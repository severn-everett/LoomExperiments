package com.severett.loomexperiments.loomvscoroutines

import com.severett.loomexperiments.common.CustomThreadPool
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
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
    @Param(value = ["10", "100", "1000", "10000"])
    private var repeat: Int = 0
    // Simulating work in increasingly-heavier loads
    @Param(value = ["0", "10", "100"])
    private var delay: Long = 0

    @Benchmark
    fun usingLoom(blackhole: Blackhole, loomHelper: LoomHelper) {
        (0 until repeat).map { i ->
            loomHelper.customThreadPool.submitTask {
                Thread.sleep(delay)
                blackhole.consume(i)
            }
        }.forEach { it.get() }
    }

    @Benchmark
    fun usingCoroutines(blackhole: Blackhole) {
        runBlocking {
            (0 until repeat).map { i ->
                delay(delay)
                blackhole.consume(i)
            }
        }
    }

    @Benchmark
    fun usingBoth(blackhole: Blackhole, hybridHelper: HybridHelper) {
        runBlocking(hybridHelper.dispatcher) {
            (0 until repeat).map { i ->
                delay(delay)
                blackhole.consume(i)
            }
        }
    }

    @State(Scope.Thread)
    open class LoomHelper {
        lateinit var customThreadPool: CustomThreadPool

        @Setup(Level.Iteration)
        fun setup() {
            customThreadPool = CustomThreadPool(1000, 1000)
        }

        @TearDown(Level.Iteration)
        fun teardown() {
            customThreadPool.shutdown()
        }
    }

    @State(Scope.Thread)
    open class HybridHelper {
        private lateinit var customThreadPool: CustomThreadPool
        val dispatcher: CoroutineDispatcher
            get() = customThreadPool.asCoroutineDispatcher()

        @Setup(Level.Iteration)
        fun setup() {
            customThreadPool = CustomThreadPool(1000, 1000)
        }

        @TearDown(Level.Iteration)
        fun teardown() {
            customThreadPool.shutdown()
        }
    }
}