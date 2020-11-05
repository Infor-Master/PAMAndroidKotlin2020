package edu.ufp.pam.someservices.downloadintentservice

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import okhttp3.OkHttpClient
import okhttp3.Request

/** Defines custom Intent actions */
const val BROADCAST_DOWNLOAD_STATUS_ACTION =
    "examples2019.pam.ufp.edu.pamandroidkotlin2019.someservices.BROADCAST_DOWNLOAD_STATUS"
const val BROADCAST_ZOOM_IMAGE_STATUS_ACTION =
    "examples2019.pam.ufp.edu.pamandroidkotlin2019.someservices.BROADCAST_ZOOM_IMAGE_STATUS"
/** Defines the key for the status "extra" in the Intent BROADCAST_DOWNLOAD_STATUS_ACTION */
const val EXTENDED_DATA_STATUS =
    "examples2019.pam.ufp.edu.pamandroidkotlin2019.someservices.STATUS"


// TODO: Rename actions, choose action names describing tasks
//  that this IntentService can perform, e.g. ACTION_FOO -> ACTION_DOWNLOAD_FILE
private const val ACTION_DOWNLOAD_FILE =
    "examples2019.pam.ufp.edu.pamandroidkotlin2019.someservices.action.DOWNLOAD_FILE"
private const val ACTION_BAZ =
    "examples2019.pam.ufp.edu.pamandroidkotlin2019.someservices.action.BAZ"

// TODO: Rename parameters e.g. EXTRA_PARAM1 -> EXTRA_PARAM1_URL
private const val EXTRA_PARAM1_URL =
    "examples2019.pam.ufp.edu.pamandroidkotlin2019.someservices.extra.PARAM1_URL"
private const val EXTRA_PARAM2_FILENAME =
    "examples2019.pam.ufp.edu.pamandroidkotlin2019.someservices.extra.PARAM2_FILENAME"

/**
 * The IntentService base class allows to handle async requests on demand (via Intents):
 *  1. Clients send requests through startService(Intent) calls;
 *  2. Service is started and handles each Intent in turn using a worker thread, and stops itself
 *     when it runs out of work.
 *
 * The IntentService class simplifies this "work queue processor" pattern and takes care of async
 * mechanics.
 *
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * TODO: Customize class - update intent actions, extra parameters and static helper methods.
 *
 */
class DownloadIntentService : IntentService("DownloadIntentService") {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //Just shows that service is starting
        Toast.makeText(this, "${this.javaClass.simpleName} starting", Toast.LENGTH_SHORT).show()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_DOWNLOAD_FILE -> {
                val param1_url = intent.getStringExtra(EXTRA_PARAM1_URL)
                val param2_filename = intent.getStringExtra(EXTRA_PARAM2_FILENAME)
                handleActionDownloadFile(param1_url, param2_filename)
            }
            ACTION_BAZ -> {
                val param1 = intent.getStringExtra(EXTRA_PARAM1_URL)
                val param2 = intent.getStringExtra(EXTRA_PARAM2_FILENAME)
                handleActionBaz(param1, param2)
            }
        }
    }
    /**
     * Handle action Foo (ACTION_DOWNLOAD_FILE) in the provided background thread with the
     * provided parameters.
     */
    private fun handleActionDownloadFile(param1Url: String, param2Filename: String) {
        // TODO("Handle action Foo")
        val resource = "${param1Url}/${param2Filename}"
        Log.e(this.javaClass.simpleName, "handleActionDownloadFile(): resource=$resource")
        val responseBody = runHttpGetCallWithOkHttp(resource)
        returnBackResponseToActivity(responseBody ?: "NOK")
    }

    /**
     * Use a LocalBroadcastManager to send/receive local status
     * (LocalBroadcast limits broadcast Intent objects to components to own app).
     */
    private fun returnBackResponseToActivity(status : String){
        Log.e(this.javaClass.simpleName, "returnBackResponseToActivity(): status=${status}")

        /*
         * Creates a new Intent containing a Uri object
         * BROADCAST_DOWNLOAD_STATUS_ACTION is a custom Intent action
         */
        val localIntent = Intent(BROADCAST_DOWNLOAD_STATUS_ACTION).apply {
            // Puts the status into the Intent
            putExtra(EXTENDED_DATA_STATUS, status)
        }
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent)
    }

    /** Helper method for http get request */
    private fun runHttpGetCallWithOkHttp(urlStr: String): String? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(urlStr)
            .get()
            .build()

        //client.newCall(request).execute().use { response -> return response.body?.string() }
        val response = client.newCall(request).execute()

        val responseBody = response.body?.string()
        Log.e(this.javaClass.simpleName, "runHttpGetCallWithOkHttp(): response=" + response.request)
        Log.e(this.javaClass.simpleName, "runHttpGetCallWithOkHttp(): message=" + response.message)
        Log.e(this.javaClass.simpleName, "runHttpGetCallWithOkHttp(): body=$responseBody")

        return responseBody
    }

    /**
     * Handle action Baz in the provided background thread with the provided parameters.
     */
    private fun handleActionBaz(param1: String, param2: String) {
        //TODO("Handle action Baz")
        Log.e(this.javaClass.simpleName, "handleActionBaz(): params=${param1}, ${param2}")
    }

    companion object {
        /**
         * Starts this service to perform action Foo with the given parameters.
         * If the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method Foo -> DownloadFile
        @JvmStatic
        fun startActionDownloadFile(context: Context, param1Url: String, param2Filename: String) {
            val intent = Intent(context, DownloadIntentService::class.java).apply {
                action = ACTION_DOWNLOAD_FILE
                putExtra(EXTRA_PARAM1_URL, param1Url)
                putExtra(EXTRA_PARAM2_FILENAME, param2Filename)
            }
            context.startService(intent)
        }

        /**
         * Starts this service to perform action Baz with the given parameters.
         * If the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method
        @JvmStatic
        fun startActionBaz(context: Context, param1: String, param2: String) {
            val intent = Intent(context, DownloadIntentService::class.java).apply {
                action = ACTION_BAZ
                putExtra(EXTRA_PARAM1_URL, param1)
                putExtra(EXTRA_PARAM2_FILENAME, param2)
            }
            context.startService(intent)
        }
    }
}
