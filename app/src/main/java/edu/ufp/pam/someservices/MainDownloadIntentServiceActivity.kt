package edu.ufp.pam.someservices

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import edu.ufp.pam.exemplos.R
import kotlinx.android.synthetic.main.content_main_download_intent_service.*

class MainDownloadIntentServiceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_download_intent_service)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        /*
        buttonDownload.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
        */

        buttonDownload.setOnClickListener {
            /* Call IntentService to download file */
            val url: String = editTextUrl.text.toString()
            val filename: String = editTextFilename.text.toString()
            Log.e(this.javaClass.simpleName, "onClick(): url=${url}/${filename}")
            //Start the service to perform a *Foo* action with given parameters...
            DownloadIntentService.startActionDownloadFile(applicationContext, url, filename)

            /* Call service to download file
            Intent(this, MultiThreadService::class.java).also {
                intent ->startService(intent)
            }
            */
        }


        // The filter's action is BROADCAST_DOWNLOAD_STATUS_ACTION
        /*val statusIntentFilter1 = IntentFilter(BROADCAST_DOWNLOAD_STATUS_ACTION).apply {
            // Adds a data filter for the HTTP scheme
            addDataScheme("http")
        }*/
        val statusIntentFilter1 = IntentFilter(BROADCAST_DOWNLOAD_STATUS_ACTION)

        // Instantiates a new action filter (No data filter is needed).
        //val statusIntentFilter2 = IntentFilter(BROADCAST_ZOOM_IMAGE_STATUS_ACTION)

        /*
         * A single BroadcastReceiver can handle more than one type of broadcast Intent object,
         * each with its own action. This feature allows to run different code for each action,
         * without having to define a separate BroadcastReceiver for each action.
         */
        // Instantiates a DownloadStateReceiver
        val downloadStateReceiver = DownloadStateReceiver(editTextFileContent)
        Log.e(this.javaClass.simpleName, "onCreate(): downloadStateReceiver=${downloadStateReceiver}")

        // Register DownloadStateReceiver for BROADCAST_DOWNLOAD_STATUS_ACTION intent with filters
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(downloadStateReceiver, statusIntentFilter1)

        // Registers the receiver for BROADCAST_ZOOM_IMAGE_STATUS_ACTION intent without filter
        //LocalBroadcastManager.getInstance(this).registerReceiver(downloadStateReceiver, statusIntentFilter2)


    }

    /**
     * Declare BroadcastReceiver for receiving status updates from the IntentService.
     * Must be declared in AndroidManifest.xml file ONLY IF NOT inner class.
     */
    private class DownloadStateReceiver(val editTextOutput: TextView) : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            /*
             * Handle Intents here.
             */
            val status: String? = intent.getStringExtra(EXTENDED_DATA_STATUS)
            Log.e(this.javaClass.simpleName, "onReceive(): status=${status}")
            editTextOutput.setText(status)
        }
    }


}