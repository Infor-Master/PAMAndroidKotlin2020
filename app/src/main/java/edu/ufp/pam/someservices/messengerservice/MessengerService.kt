package edu.ufp.pam.someservices.messengerservice

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.android.volley.toolbox.StringRequest
import edu.ufp.pam.exemplos.R
import edu.ufp.pam.farrusco.SingletonVolleyRequestQueue
import okhttp3.OkHttpClient
import okhttp3.Request


/** Command for service to display a message  */
public const val MSG_TO_SERVICE_SAY_HELLO = 1
public const val MSG_TO_CLIENT_REPLY_OK = 2
public const val MSG_TO_SERVICE_DOWNLOAD_FILE = 3
public const val MSG_TO_CLIENT_REPLY_FILE_CONTENT = 4

/**
 * Service to perform multi-threading tasks
 * (instead of processing start requests through a work queue)
 *
 * A service provides an IBinder (programming interface that clients use to call the service).
 * There are 3 ways to define the interface:
 *
 *  Extending the Binder class:
 *      when service is merely a background worker for your own application;
 *      service private to own application and runs in same process as client (which is common);
 *      create interface by extending the Binder class and return instance from onBind();
 *
 *  Using a Messenger:
 *      when service works across different processes, create an interface with a Messenger;
 *      service defines an Handler that responds to different types of Message objects;
 *      the Handler is the basis for a Messenger that can then share the IBinder;
 *      client can define a Messenger of its own, so the service can send messages back;
 *      Messenger queues all requests into single thread (service needs not be thread-safe).
 *
 *  Using AIDL:
 *      Android Interface Definition Language (AIDL) decomposes objects into primitives that the
 *      operating system can understand and marshals them across processes to perform IPC;
 *      The previous technique (Messenger) is actually based on AIDL as its underlying structure;
 *      Must create an .aidl file that defines the programming interface (similar to RMI).
 *
 */
class MessengerService : Service() {

    /** Target published for clients to send messages to IncomingHandler. */
    private lateinit var serviceMessenger: Messenger

    /** Target published for service to send messages to client. */
    //private lateinit var clientMessenger: Messenger

    /** Handler for incoming messages from clients via Messenger. */
    //internal class IncomingHandler((context: Context) : Handler() {
    inner class IncomingHandler() : Handler() {
        override fun handleMessage(msg: Message) {
            Log.e(this.javaClass.simpleName,"handleMessage(): service side msg.obj=${msg.obj.toString()}")
            when (msg.what) {
                MSG_TO_SERVICE_SAY_HELLO -> {
                    //Toast.makeText(applicationContext, "Hello on service side!", Toast.LENGTH_SHORT).show()
                    Log.e(this.javaClass.simpleName,"handleMessage(): received msg on service side... msg.obj=${msg.obj}")
                    val msgToClient: Message = Message.obtain(null, MSG_TO_CLIENT_REPLY_OK, "Ok from Service!")
                    msg.replyTo.send(msgToClient)
                }
                MSG_TO_SERVICE_DOWNLOAD_FILE -> {
                    //Toast.makeText(applicationContext, "Going to download file on service side!", Toast.LENGTH_LONG).show()
                    Log.e(this.javaClass.simpleName,"handleMessage(): going to download file on service side!")
                    val url : String = msg.obj.toString()
                    val responseBody = runHttpGetCallWithOkHttp(url)
                    //val responseBody = launchAsyncVolleyHttpRequest(url)
                    val msgToClient: Message = Message.obtain(null, MSG_TO_CLIENT_REPLY_FILE_CONTENT, responseBody)
                    msg.replyTo.send(msgToClient)
                }
                else -> super.handleMessage(msg)
            }
        }

        /** Helper method for http get request
         *  Always use withContext() inside a suspend function for main-safety, e.g.
         *  reading from or writing to disk, performing network operations, or
         *  running CPU-intensive operations.
         *
         *  Using a dispatcher that uses a thread pool like Dispatchers.IO or Dispatchers.Default
         *  does not guarantee that the block executes on the same thread from top to bottom.
         *  In some situations, Kotlin coroutines might move execution to another thread after a
         *  suspend-and-resume. This means thread-local variables might not point to the same value
         *  for the entire withContext() block.
         */
        fun runHttpGetCallWithOkHttp(urlStr: String): String? {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(urlStr)
                .get()
                .build()

            var responseBody : String? = "NOK"

            // Dispatchers.IO (main-safety block)
            //withContext(Dispatchers.IO){
            /* Perform network IO here */
            //client.newCall(request).execute().use { response -> return response.body?.string() }
            val response = client.newCall(request).execute()

            responseBody = response.body?.string()
            Log.e(this.javaClass.simpleName, "runHttpGetCallWithOkHttp(): body=$responseBody")
            //}

            return responseBody
        }
    }


    /**
     * When client binds to the service, it returns an interface handler to the service Messenger,
     * so that client can send messages to the service.
     */
    override fun onBind(intent: Intent): IBinder {
        //TODO("Return the communication channel that client uses to send msg to the service.")
        Toast.makeText(applicationContext, "binding...", Toast.LENGTH_SHORT).show()
        serviceMessenger = Messenger(IncomingHandler())
        return serviceMessenger.binder
    }
}
