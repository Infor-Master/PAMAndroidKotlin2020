package edu.ufp.pam.someservices.messengerservice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton


import edu.ufp.pam.exemplos.R
import kotlinx.android.synthetic.main.content_main_messenger_service.*

/**
 * A Service is an app component to perform long-running operations, though it runs in the main
 * thread of its hosting process (does not create its own thread). Therefore, services MUST use
 * *WorkManager* to run/schedule async jobs/tasks such as:
 *   - handle network transactions;
 *   - play music;
 *   - perform file I/O;
 *   - interact with a content provider.
 *
 * A Service does not provide user interface, and once started it might continue running for some
 * time, even after user switches to another app.
 *
 * ================================ Service vs Thread: ================================
 *  - Service is simply a component that can run in background, even when the user is not
 *  interacting with app.
 *  - Thread performs in parallel with main thread but while user interacting with app, e.g.,
 *  play some music but only while activity is running.
 *
 *
 * Types of services:
 *  1. *Started Services* (run indefinitely) have 2 sub-types:
 *      1.1. Foreground: performs some operation that is noticeable to the user, e.g., play an
 *           audio track.
 *           Foreground services must display a Notification so that users are aware of running
 *           service (the notification cannot be dismissed unless service is either stopped or
 *           removed from foreground).
 *           Foreground services continue running even when the user is not interacting with app.
 *
 *      1.2. Background: performs an operation that is not directly noticed by user, e.g.,
 *           compact storage may usually be on a background service.
 *
 *  2. *Bound Services* (allow binding and runs only during bind exists):
 *      Offers InterProcess Communication (IPC) mechanisms(~RMI).
 *      An app component binds to service by calling bindService().
 *      Multiple components can bind to service at once, but when all unbind, service is destroyed.
 *      Activities, Services and ContentProviders CAN bind to service.
 *      BroadcastReceiver CANNOT bind to a service.
 *
 */

class MainMessengerServiceActivity : AppCompatActivity() {

    /**
     * When client sends Message to service, it includes the client's Messenger in the
     * replyTo parameter of the send() method.
     * The client Messenger allows to receive reply on the onServiceConnected() callback.
     */

    /** Messenger for client to communicate with service.  */
    private var serviceMessenger: Messenger? = null
    /** Flag indicating whether we have called bind on the service.  */
    private var serviceBound: Boolean = false
    /** Messenger for service to communicate back with client.  */
    private var clientMessenger: Messenger? = null

    /** Object used to create local Messenger for connecting with the IBinder of service. */
    private val serviceConnection = object : ServiceConnection {
        /** Method called when the connection with the service has been established, providing an
        object (IBinder) used to interact with the service. */
        override fun onServiceConnected(className: ComponentName, serviceBinder: IBinder) {
            // Client communicates with the service using a Messenger object, i.e. a client-side
            // representation of the raw service IBinder object.
            serviceMessenger = Messenger(serviceBinder)
            serviceBound = true
        }

        /** Method called when the connection with the service has been unexpectedly disconnected
         * (i.e. its process crashed). */
        override fun onServiceDisconnected(className: ComponentName) {
            serviceMessenger = null
            serviceBound = false
        }
    }

    /** Handler for incoming messages from service via Messenger. */
    internal class IncomingHandler(context: Context,
                                   val editTextDataOutput : EditText,
                                   private val applicationContext: Context = context.applicationContext
    ) : Handler() {
        //Client-side handler callback which receives reply from service
        override fun handleMessage(msg: Message) {
            Log.e(this.javaClass.simpleName, "handleMessage(): client side msg.obj=${msg.obj}")
            when (msg.what) {
                MSG_TO_CLIENT_REPLY_OK -> {
                    Toast.makeText(applicationContext, "Hello on client side!", Toast.LENGTH_SHORT).show()
                    editTextDataOutput.setText(msg.obj.toString())
                }
                MSG_TO_CLIENT_REPLY_FILE_CONTENT -> {
                    Toast.makeText(applicationContext, "Client side will show downloaded file content!", Toast.LENGTH_LONG).show()
                    editTextDataOutput.setText(msg.obj.toString())
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    fun sayHelloToService(v: View) {
        Log.e(this.javaClass.simpleName,"sayHelloToService(): button pressed serviceBound=${serviceBound}")
        if (!serviceBound) return
        // Create and send a message to the service, using a supported 'what' value
        try {
            //Prepare a message to be sent to service
            //val msg: Message = Message.obtain(null, MSG_TO_SERVICE_SEND_HELLO, 0, 0)
            //val msg: Message = Message.obtain(null, MSG_TO_SERVICE_SAY_HELLO, "Hello World from client!")
            val url = "http://homepage.ufp.pt/rmoreira/LP2/data.txt"
            val msg: Message = Message.obtain(null, MSG_TO_SERVICE_DOWNLOAD_FILE, url)

            //Send also the client Messenger for service to callback
            msg.replyTo = clientMessenger
            serviceMessenger?.send(msg)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    /** Bind could be done during onCreate() and unbind during onDestroy() whenever the activity
     * needs to receive responses even while it is stopped in the background (but may increase
     * the weight of the process and probability of system to kill it). */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_messenger_service)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        //Create client Messenger
        if (this.clientMessenger==null){
            clientMessenger = Messenger(IncomingHandler(this, editTextDataOutput))
        }

        //Create button listener to call service
        buttonSayHello.setOnClickListener {
            sayHelloToService(it)
        }
    }

    /** Bind during onStart() assures interaction with service only while activity is visible */
    override fun onStart() {
        super.onStart()
        // Use an Intent to bind to the service, i.e. call bindService():
        // binding is asynchronous, hence bindService() returns immediately without returning the
        // IBinder to the client. The client must create an instance of ServiceConnection and pass
        // it to bindService() to receive the IBinder (ServiceConnection includes a callback method
        // to deliver the IBinder back to client).
        Intent(this, MessengerService::class.java).also {
                intent -> bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    /** Unbind during onStop() assures interaction with service stops while activity is not visible */
    override fun onStop() {
        super.onStop()
        // Unbind from the service
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }
}