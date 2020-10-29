package edu.ufp.pam.farrusco.workers

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import edu.ufp.pam.farrusco.workers.WorkManagerHelper.Companion.KEY_WORKER_DOWNLOAD_URL
import edu.ufp.pam.farrusco.workers.WorkManagerHelper.Companion.TAG_WORKER_DOWNLOAD_OUTPUT
import okhttp3.OkHttpClient
import okhttp3.Request


/**
 * There are several WorkManager classes:
 *  - Worker: class where to perform tasks in the background. Extend class and override doWork().
 *  - WorkRequest: class representing request to do some work and passed to Worker as part of a
 *    WorkRequest; may also specify Constraints on when the Worker should run.
 *  - WorkManager: class that schedules WorkRequest and makes it run.
 */
class DownloadWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    //override val coroutineContext = Dispatchers.IO
    /**
     * CoroutineWorker.doWork() is suspending fun which by defaults runs on Dispatchers.Default.
     * It is possible to change the Coroutine Dispatcher to, e.g., Dispatchers.IO.
     */
    //override suspend fun doWork(): Result = coroutineScope {

    /**
     * The Worker will be scheduled by WorkManager, i.e., the doWork() runs synchronously on a
     * background thread provided by WorkManager.
     * The Result returned by doWork() will be:
     *  - Result.success(): work finished successfully.
     *  - Result.failure(): work failed.
     *  - Result.retry(): work failed and should be tried at another time according to retry policy.
     */
    override fun doWork(): Result {
        val resourceUri = inputData.getString(KEY_WORKER_DOWNLOAD_URL)
        Log.e(this.javaClass.simpleName, "doWork(): resourceUri=$resourceUri")

        val response = resourceUri?.let {
            downloadWorkerSyncHttpCall(it)
        }
        Log.e(this.javaClass.simpleName, "doWork(): response=$response")
        val outputData : Data = workDataOf(TAG_WORKER_DOWNLOAD_OUTPUT to response)
        Log.e(this.javaClass.simpleName, "doWork(): outputData=${outputData.getString(TAG_WORKER_DOWNLOAD_OUTPUT)}")
        return Result.success(outputData)
    }

    private fun downloadWorkerSyncHttpCall(urlStr: String): String? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(urlStr)
            .get()
            .build()
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
        Log.e(this.javaClass.simpleName, "downloadSynchronously(): response.request=" + response.request)
        Log.e(this.javaClass.simpleName, "downloadSynchronously(): response.message=" + response.message)
        Log.e(this.javaClass.simpleName, "downloadSynchronously(): response.body=$responseBody")
        return responseBody
    }
}