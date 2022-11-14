package com.severett.loomexperiments.threadvsloom

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.*
import kotlin.concurrent.thread

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(1)
open class ThreadVsLoom {
    @Param(value = ["10", "100", "1000", "10000"])
    private var repeat: Int = 0
    // Simulating work in increasingly-heavier loads
    @Param(value = ["0", "10", "100"])
    private var delay: Long = 0

    @Benchmark
    fun traditionalThreads(blackhole: Blackhole) {
        (0 until repeat).map { i ->
            thread {
                Thread.sleep(delay)
                blackhole.consume(i)
            }
        }.forEach { it.join() }
    }

    @Benchmark
    fun virtualThreads(blackhole: Blackhole) {
        (0 until repeat).map { i ->
            Thread.startVirtualThread {
                Thread.sleep(delay)
                blackhole.consume(i)
            }
        }.forEach { it.join() }
    }
}