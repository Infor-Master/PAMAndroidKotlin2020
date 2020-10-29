package edu.ufp.pam.farrusco

import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkInfo

import com.android.volley.toolbox.StringRequest
import com.android.volley.RequestQueue
import edu.ufp.pam.exemplos.R

import okhttp3.OkHttpClient
import kotlinx.android.synthetic.main.activity_main_farrusco.*
import kotlinx.android.synthetic.main.content_main_farrusco.*
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.net.HttpURLConnection

import edu.ufp.pam.farrusco.workers.WorkManagerHelper
import edu.ufp.pam.farrusco.workers.WorkManagerHelper.Companion.TAG_WORKER_DOWNLOAD_OUTPUT

class MainFarruscoActivity : AppCompatActivity() {

    private lateinit var farruscoViewModel: FarruscoViewModel
    private val TAG_TO_CANCEL_HTTP_REQUEST = "TAG_TO_CANCEL_HTTP_REQUEST"
    //private val requestQueue: RequestQueue? = null

    //Just like C# properties
    var reply: String
        get() = ""
        set(value) {
            reply = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_farrusco)
        setSupportActionBar(toolbar)

        Log.e(this.javaClass.simpleName, "onCreate(): going to get FarruscoViewModel...")

        // Get ViewModel do launch async download
        farruscoViewModel =
            ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
            ).get(FarruscoViewModel::class.java)

        //Set ViewModel URI
        val urlTest = "http://homepage.ufp.pt/rmoreira/LP2/data.txt"
        Log.e(this.javaClass.simpleName,"onCreate(): urlTest = $urlTest"
        )
        farruscoViewModel.setFileURI("$urlTest")

        // Attach Observer for LiveData associated with ViewModel.httpReply
        // The onChanged() is triggered whenever observed data changes with activity in foreground.
        farruscoViewModel.getHttpReply().observe(
            this,
            Observer { httpReply ->
                // Update httpReply
                httpReply?.let {
                    val reply = farruscoViewModel.getHttpReply().value
                    Log.e(this.javaClass.simpleName, "onChanged(): reply = $reply")
                    //Put file content into textview
                    textViewReply.text = reply
                }
            })

        fab.setOnClickListener { view ->
            /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()*/
            //Use async Worker to handle HttpRequest
            val urlStr = "http://homepage.ufp.pt/rmoreira/LP2"
            val queryStr = "/data.txt"

            //1. Use Volley async HttpRequest
            launchAsyncVolleyHttpRequest("$urlStr$queryStr")

            //2. Use ViewModel to launch async HTTP call and receive response through observer LiveDate
            //launchViewModelAsyncHttpRequest(urlStr, queryStr)

            //3. Use WorkManager to enqueue and asyn execute a DownloadWorker
            //launchDownloadWorkerAsyncHttpRequest(urlStr, queryStr)
        }

        /* ============================== AndroidManifest ========================================
         * NB: changes to android manifest...
         * 1. Insert the user permissions in the manifest:
         *      <uses-permission android:name="android.permission.INTERNET" />
         *      <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
         * 2. Load net config an
         *      android:usesCleartextTraffic="true">
         * ============================== AndroidManifest ========================================
         */

        imageButtonUp.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                //url + "?i=" + farruscoID + "&m=12&t=" + time
                setFarruscoMovementAndLaunch("12")
            }
        })

        //Use lambda function
        imageButtonRight.setOnClickListener {
            //url + "?i=" + farruscoID + "&m=3&t=" + time
            setFarruscoMovementAndLaunch("3")
        }

        imageButtonLeft.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                //url + "?i=" + farruscoID + "&m=9&t=" + time
                setFarruscoMovementAndLaunch("9")
            }
        })

        //Use lambda function
        imageButtonDown.setOnClickListener {
            //url + "?i=" + farruscoID + "&m=6&t=" + time
            setFarruscoMovementAndLaunch("6")
        }
    }

    /**
     * Use onStop() to cancel all Activity of pending requests.
     */
    protected override fun onStop() {
        super.onStop()
        val queue = SingletonVolleyRequestQueue.getInstance(this.applicationContext).requestQueue
        queue?.cancelAll(TAG_TO_CANCEL_HTTP_REQUEST)
    }

    /**
     * =============== WHEN HAVING NET PROBLEMS ON EMULATOR ===============
     * On AVD Manager select Virtual Device and choose following options:
     *   1. Cold Boot Now will reboot android device
     *   2. Wipe Data will factory reset the device
     * =============== WHEN HAVING NET PROBLEMS ON EMULATOR ===============
     */
    private fun setFarruscoMovementAndLaunch(move: String) {
        //URL where script is listenning
        val urlStr = editTextUrl.text.toString()
        //Time of movement
        val timeStr = editTextTime.text.toString()
        //Farrusco ID (2)
        val idStr = editTextID.text.toString()
        //Assembling query string, e.g., ?i=2&m=12&t=500 (ID=2, MOVE=FW, TIME=500ms)
        val queryStr = "?i=$idStr&m=$move&t=$timeStr"
        Log.e(this.javaClass.simpleName, "setFarruscoMovementAndLaunch(): urlStr=$urlStr")
        Log.e(this.javaClass.simpleName, "setFarruscoMovementAndLaunch(): queryStr=$queryStr")

        /* Need to change execution permissions when using sync calls - no need with AsyncTasks
        if (android.os.Build.VERSION.SDK_INT > 9) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        */
        //Run sync call (with execution permissions activate previously)):
        //runHttpGet(url)

        //Run async call:
        //GetHttpTask(this.textViewReply).execute(urlStr, queryStr)
        //launchDownloadWorkerAsyncHttpRequest(urlStr, queryStr)
        launchAsyncVolleyHttpRequest("$urlStr$queryStr")
    }

    /**
     * Use the ViewModel to async execute HTTP Call.
     */
    private fun launchViewModelAsyncHttpRequest(urlStr: String, queryStr: String) {
        Log.e(this.javaClass.simpleName, "launchViewModelAsyncHttpRequest(): url+query=$urlStr$queryStr")
        //return GetHttpTask(this.textViewReply).execute(urlStr, queryStr).get()
        farruscoViewModel.setFileURI("$urlStr$queryStr")
        farruscoViewModel.launchAsyncDownload()
    }

    /**
     * Use a DownloadWorker to sync execute HTTP Call.
     */
    private fun launchDownloadWorkerAsyncHttpRequest(urlStr: String, queryStr: String) {
        Log.e(this.javaClass.simpleName, "launchDownloadWorkerAsyncHttpRequest(): url+query=$urlStr$queryStr")
        val workManagerHelper  = WorkManagerHelper(this.application)

        //Add Observer to handle for HTTP Response
        workManagerHelper.getOutputWorkInfos().observe(this, Observer {
            val workInfo = it.get(0)
            Log.e(this.javaClass.simpleName,"observer(): workInfo.state = ${workInfo.state}")
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                val response = workInfo.outputData.getString(TAG_WORKER_DOWNLOAD_OUTPUT)
                Log.e(this.javaClass.simpleName,"observer(): response = ${response}")
            }
        }
        )
        workManagerHelper.setFileURI("$urlStr$queryStr")
        workManagerHelper.launchDownloadWorker()
    }

    /**
     * Volley uses and async task but delivers parsed responses on the main thread, which may be
     * convenient for populating UI controls with received data.
     * However, this may be critical to many important semantics provided by the library,
     * particularly related to canceling requests... done on Activity onStop().
     *
     * There is also the Cronet Library, which provides Chromium network stack to perform network
     * operations in Android apps:
     *  implementation 'com.google.android.gms:play-services-cronet:16.0.0'
     */
    private fun launchAsyncVolleyHttpRequest(urlStr: String) {
        val textViewReply = findViewById<TextView>(R.id.textViewReply)
        // Get the RequestQueue (singleton)
        //val queue = Volley.newRequestQueue(this)
        val queue = SingletonVolleyRequestQueue.getInstance(this.applicationContext).requestQueue

        // Create Request with Listeners:
        //  1 listener to handle Response from the provided URL;
        //  1 listener for error handling.
        val stringRequest = StringRequest(com.android.volley.Request.Method.GET, urlStr,
            { //Handle Response
                    response ->
                Log.e(this.javaClass.simpleName,
                    "launchAsyncVolleyHttpRequest(): Response.Listener Response=${response}")
                textViewReply.text = "Response is:\n$response"
            },
            { //Handle Error
                    error ->
                Log.e(this.javaClass.simpleName,
                    "launchAsyncVolleyHttpRequest(): Response.Listener Error=$error")
                textViewReply.text = "Download did not work!!!!"
            }
        )
        // Set the cancel tag on the request
        stringRequest.tag = TAG_TO_CANCEL_HTTP_REQUEST

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }


    /**
     * AsyncTack to download file
     * */
    private class GetHttpTask(textViewReply: TextView) : AsyncTask<String, Unit, String>() {

        val innerTextView: TextView? = textViewReply

        override fun doInBackground(vararg params: String): String? {
            //val url = URL("http://homepage.ufp.pt/rmoreira/LP2/data.txt")
            //val url = URL(params[0])
            val url = params[0]
            Log.e(this.javaClass.simpleName, "doInBackground(): url=$url")
            //return doHttpGetWithPlainJava(url)
            return runHttpGetCallWithOkHttp(url)
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            Log.e(this.javaClass.simpleName, "onPostExecute(): result=$result")
            innerTextView?.text = result
        }

        fun runHttpGetCallWithOkHttp(urlStr: String): String? {
            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(urlStr)
                .get()
                .build()

            //client.newCall(request).execute().use { response -> return response.body?.string() }
            val response = client.newCall(request).execute()

            val responseBody = response.body?.string()
            Log.e(
                this.javaClass.simpleName,
                "runHttpGetCallWithOkHttp(): response=" + response.request
            )
            Log.e(
                this.javaClass.simpleName,
                "runHttpGetCallWithOkHttp(): message=" + response.message
            )
            Log.e(this.javaClass.simpleName, "runHttpGetCallWithOkHttp(): body=$responseBody")

            return responseBody
        }

        fun doHttpGetWithPlainJava(urlStr: String): String {
            val url = URL(urlStr)
            Log.e(this.javaClass.simpleName, "doHttpGetWithPlainJava(): url=$urlStr")
            val httpClient = url.openConnection() as HttpURLConnection
            if (httpClient.responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    val stream = BufferedInputStream(httpClient.inputStream)
                    val data: String = readStream(inputStream = stream)
                    return data
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    httpClient.disconnect()
                }
            } else {
                Log.e(
                    this.javaClass.simpleName,
                    "doHttpGetWithPlainJava(): ERROR ${httpClient.responseCode}"
                )
            }
            return "ERROR ${httpClient.responseCode}"
        }

        fun readStream(inputStream: BufferedInputStream): String {
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            bufferedReader.forEachLine { stringBuilder.append(it) }
            Log.e(this.javaClass.simpleName, "readStream(): reply=$stringBuilder")
            return stringBuilder.toString()
        }
    }
}