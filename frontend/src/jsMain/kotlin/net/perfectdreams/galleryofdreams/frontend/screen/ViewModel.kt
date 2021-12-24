package net.perfectdreams.galleryofdreams.frontend.screen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

abstract class ViewModel {
    private var rejectNewJobs = false
    private val jobs = mutableListOf<Job>()

    fun dispose() {
        rejectNewJobs = true
        println("Disposing ${jobs.size} jobs...")
        jobs.forEach { it.cancel() }
        jobs.clear()
    }

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        if (rejectNewJobs)
            error("All new jobs are being rejected!")

        val job = GlobalScope.launch(block = block)
        jobs.add(job)
        job.invokeOnCompletion {
            jobs.remove(job)
        }
        return job
    }

    fun <T> async(block: suspend CoroutineScope.() -> T): Deferred<T> {
        if (rejectNewJobs)
            error("All new jobs are being rejected!")

        val job = GlobalScope.async(block = block)
        jobs.add(job)
        job.invokeOnCompletion {
            jobs.remove(job)
        }
        return job
    }
}