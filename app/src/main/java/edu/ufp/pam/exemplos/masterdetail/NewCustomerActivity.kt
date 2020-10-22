package edu.ufp.pam.exemplos.masterdetail

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import edu.ufp.pam.exemplos.R

class NewCustomerActivity : AppCompatActivity() {

    private lateinit var editTextNewCustomerName: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_customer)

        //Get EditText with new Customer name
        editTextNewCustomerName = findViewById(R.id.editTextNewCustomerName)

        val button = findViewById<Button>(R.id.buttonNewCustomer)
        button.setOnClickListener {
            val replyIntent = Intent()
            if (TextUtils.isEmpty(editTextNewCustomerName.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                val customerName = editTextNewCustomerName.text.toString()
                replyIntent.putExtra(EXTRA_CUSTOMER_NAME_REPLY_KEY, customerName)
                setResult(Activity.RESULT_OK, replyIntent)
            }
            finish()
        }
    }

    companion object {
        const val EXTRA_CUSTOMER_NAME_REPLY_KEY = "edu.ufp.pam.exemplos.masterdetail.dbcontacts.REPLY"
    }
}