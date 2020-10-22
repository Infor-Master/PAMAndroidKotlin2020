package edu.ufp.pam.exemplos.masterdetail.dbcontacts

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import edu.ufp.pam.examplos.masterdetail.dbcontacts.Customer
import edu.ufp.pam.examplos.masterdetail.dbcontacts.CustomerDao

class CustomersRepository(private val customerDao: CustomerDao) {

    // Room executes all queries on a separate thread.
    // The public property is an observable LiveData which notifies  observer when  data changes.
    val allCustomers: LiveData<List<Customer>> = customerDao.loadAllCustomers()

    // Make this a suspend function so the caller knows this must be called on a non-UI thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertCustomer(customer: Customer) {
        Log.e(
            this.javaClass.simpleName,
            "insertCustomer(): going to insert new customer ${customer}"
        )
        customerDao.insertCustomer(customer)
    }
}