package edu.ufp.pam.exemplos.masterdetail

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.ufp.pam.examplos.masterdetail.dbcontacts.Customer
import edu.ufp.pam.examplos.masterdetail.dbcontacts.LoaderCustomersContentDatabase
import edu.ufp.pam.exemplos.R
import edu.ufp.pam.exemplos.masterdetail.viewmodel.CustomersViewModel
import kotlinx.android.synthetic.main.activity_main_customer_create_database.*
import java.lang.ref.WeakReference

class MainCustomerCreateDatabaseActivity : AppCompatActivity() {

    //Declare property for associated CustomersViewModel
    private lateinit var customersViewModel: CustomersViewModel

    //Code for communication between activities
    private val newCustomerActivityRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_customer_create_database)

        val textViewListContacts = findViewById<TextView>(R.id.textViewListContacts)

        // Get new or existing CustomersViewModel from ViewModelProvider
        //customersViewModel = ViewModelProvider(this).get(CustomersViewModel::class.java)
        customersViewModel =
            ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
            ).get(CustomersViewModel::class.java)

        customersViewModel.allCustomers.observe(
            this,
            Observer { customers ->
                // Update cached list of customers
                customers?.let {
                    Log.e(this.javaClass.simpleName,
                        "onChanged(): customers.size=${customers.size}"
                    )
                    //Clear textViewListContacts
                    textViewListContacts.text=""
                    var i = 0
                    for (c in it) {
                        Log.e(this.javaClass.simpleName,
                            "onChanged(): customer[$i++]=${c}"
                        )
                        val item = "[${c.customerId}] ${c.customerName}"
                        textViewListContacts.append("${item} | ")
                    }
                }
            })

        //Open the NewWordActivity when tapping on the FAB button...
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            // .setAction("Action", null).show()

            //Create an Intent to launch the NewCustomerActivity
            val intent =
                Intent(
                    this@MainCustomerCreateDatabaseActivity,
                    NewCustomerActivity::class.java
                )
            startActivityForResult(intent, newCustomerActivityRequestCode)
        }

        buttonCreateDatabaseCustomers.setOnClickListener {
            //Use an AsyncTask to create the database and populate it with sample data
            //CustomersDatabaseAsyncTask(this@MainCustomerCreateDatabaseActivity).execute("Nothing useful for this task! :)")

            /*
            val replyIntent = Intent()
            val EXTRA_REPLY = "com.example.android.wordlistsql.REPLY"
            replyIntent.putExtra(EXTRA_REPLY, "Teste")
            setResult(Activity.RESULT_OK, replyIntent)
            finish()
             */
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)

        Log.e(
            this.javaClass.simpleName,
            "onActivityResult(): back from NewCustomerActivity..."
        )
        if (requestCode == newCustomerActivityRequestCode && resultCode == Activity.RESULT_OK) {
            intentData?.let { data ->
                val customerName : String? =
                    data.getStringExtra(NewCustomerActivity.EXTRA_CUSTOMER_NAME_REPLY_KEY)
                Log.e(
                    this.javaClass.simpleName,
                    "onActivityResult(): new customer = $customerName"
                )
                val customerIndex : String? = customerName!!.subSequence(
                    customerName.length - 2,
                    customerName.length
                ).toString()
                //Customer(i, "Tio Patinhas $i", "Patinhas $i Lda", "Rua Sesamo $i", "Porto", "+35122000000$i")
                val customer = Customer(
                    0,
                    customerName,
                    "Patinhas ${customerIndex} Lda",
                    "Rua Sesamo $customerIndex",
                    "Porto",
                    "+35122000000$customerIndex",
                    null
                )
                customersViewModel.insert(customer)
                Unit
            }
        } else {
            Toast.makeText(applicationContext, "Empty customer... not saved!!", Toast.LENGTH_LONG).show()
        }
    }

    protected fun startFollowingActivity(){
        //Call CustomerItemContactDetailListActivity to list dababase content
        val intent = Intent(this, CustomerItemListActivity::class.java)
        //Pass any data to next activity
        intent.putExtra("CUSTOMER_ITEMS_SIZE", LoaderCustomersContentDatabase.CUSTOMER_ITEMS.size)
        //Start your next activity
        startActivity(intent)
    }

    companion object {

        /**
         * class SomeTask : AsyncTask<Params, Progress, Result>
         *      Params: type of the parameters sent to doInBackground()
         *      Progress: type of the progress parameter sent to onProgressUpdate()
         *      Result: type of result of the background computation passed to onPostExecute().
         */
        class CustomersDatabaseAsyncTask(activity: MainCustomerCreateDatabaseActivity) :
            AsyncTask<String, Int, String>() {

            //private val parentActivity: CustomerItemContactDetailListActivity = activity
            private val activityReference: WeakReference<MainCustomerCreateDatabaseActivity> =
                WeakReference(activity)

            /**
             * Invoked on the UI thread before the task is executed.
             */
            override fun onPreExecute() {
                super.onPreExecute()
                Log.e(
                    this.javaClass.simpleName,
                    "onPreExecute(): going to do something before create CustomersDatabase..."
                )
                val activity = activityReference.get()
                if (activity == null || activity.isFinishing) return
                //...
            }

            /**
             * invoked on the background thread immediately after onPreExecute().
             */
            override fun doInBackground(vararg params: String?): String {
                Log.e(
                    this.javaClass.simpleName,
                    "doInBackground(): going to create CustomersDatabase, params = ${params[0]}"
                )
                val parentActivity = activityReference.get()
                Log.e(
                    this.javaClass.simpleName,
                    "doInBackground(): LoaderCustomersContentDatabase.CUSTOMER_ITEMS.size = ${LoaderCustomersContentDatabase.CUSTOMER_ITEMS.size}"
                )
                if (parentActivity != null && LoaderCustomersContentDatabase.CUSTOMER_ITEMS.size == 0) {
                    //parentActivity.buildCustomersContentDatabase.createDb(parentActivity.applicationContext)

                    //val context: Context = InstrumentationRegistry.getTargetContext()
                    LoaderCustomersContentDatabase.createDb(parentActivity.applicationContext)
                    this.publishProgress(50)
                    LoaderCustomersContentDatabase.addSampleItemsToDatabase()
                    this.publishProgress(100)
                    //return "OK"
                }
                return "OK"
            }

            /**
             * invoked on the UI thread after a call to publishProgress().
             */
            override fun onProgressUpdate(vararg progress: Int?) {
                val parentActivity = activityReference.get()
                if (parentActivity != null) {
                    Log.e(
                        this.javaClass.simpleName,
                        "onProgressUpdate(): Progress: ${progress[0]}%"
                    )
                    Toast.makeText(parentActivity, "Progress: ${progress[0]}%", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            /**
             * invoked on the UI thread after the background computation finishes.
             */
            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                Log.e(
                    this.javaClass.simpleName,
                    "onPostExecute(): after creating CustomersDatabase, result=$result"
                )
                val parentActivity = activityReference.get()
                if (parentActivity != null && result == "OK") {
                    //parentActivity.setupRecyclerView(parentActivity.itemcontactdetail_list)
                    Toast.makeText(parentActivity, "Customers.db... Done!!", Toast.LENGTH_LONG).show()
                    parentActivity.startFollowingActivity()
                }
            }
        }
    }
}