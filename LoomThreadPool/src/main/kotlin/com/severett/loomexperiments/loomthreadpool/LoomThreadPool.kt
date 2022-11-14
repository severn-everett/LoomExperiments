package com.severett.loomexperiments.loomthreadpool

import com.severett.loomexperiments.common.CustomThreadPool
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
open class LoomThreadPool {
    @Param(value = ["10", "100", "1000", "10000", "100000"])
    private var repeat: Int = 0
    // Simulating work in increasingly-heavier loads
    @Param(value = ["0", "10", "100"])
    private var delay: Long = 0

    @Benchmark
    fun unbound(blackhole: Blackhole) {
        (0 until repeat).map { i ->
            Thread.startVirtualThread {
                Thread.sleep(delay)
                blackhole.consume(i)
            }
        }.forEach { it.join() }
    }

    @Benchmark
    fun threadpool(blackhole: Blackhole, threadPoolHelper: ThreadPoolHelper) {
        (0 until repeat).map { i ->
            threadPoolHelper.customThreadPool.submitTask {
                Thread.sleep(delay)
                blackhole.consume(i)
            }
        }.forEach { it.get() }
    }

    @State(Scope.Thread)
    open class ThreadPoolHelper {
        lateinit var customThreadPool: CustomThreadPool

        @Setup(Level.Iteration)
        fun setup() {
            customThreadPool = CustomThreadPool(1000, 1000)
        }

        @TearDown(Level.Iteration)
        fun teardown() {
            customThreadPool.close()
        }
    }
}
