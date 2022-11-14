package com.severett.loomthreadpool

import java.util.concurrent.*
import java.util.concurrent.locks.*
import kotlin.concurrent.withLock

private const val EMPTY = 0

class BockingQueue<Type>(private val maxTaskAmt: Int) {
    private val queue = ArrayDeque<Type>()
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    fun enqueue(task: Type) {
        lock.withLock {
            while (queue.size == maxTaskAmt) condition.await()
            if (queue.size == EMPTY) condition.signalAll()
            queue.add(task)
        }
    }

    fun dequeue(): Type {
        return lock.withLock {
            while (queue.size == EMPTY) condition.await()
            if (queue.size == maxTaskAmt) condition.signalAll()
            queue.removeFirst()
        }
    }
}

class CustomThreadPool(queueSize: Int, nThread: Int) {
    private val queue = BockingQueue<Runnable>(queueSize)
    private val pool: List<Thread>

    init {
        pool = (0 until nThread).map {
            val task = TaskExecutor(queue)
            Thread.startVirtualThread(task)
        }
    }

    fun shutdown() {
        pool.forEach { it.interrupt() }
    }

    fun submitTask(task: Runnable): Future<Unit> {
        val ft = FutureTask(task, Unit)
        queue.enqueue(ft)
        return ft
    }
}

class TaskExecutor(private val queue: BockingQueue<Runnable>) : Runnable {
    override fun run() {
        try {
            while (true) {
                val task = queue.dequeue()
                task.run()
            }
        } catch (ie: InterruptedException) {
            // Disabled for testing purposes
            // ie.printStackTrace()
        }
    }
}