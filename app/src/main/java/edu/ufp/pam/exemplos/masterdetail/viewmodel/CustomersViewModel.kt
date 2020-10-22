package edu.ufp.pam.exemplos.masterdetail.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import edu.ufp.pam.examplos.masterdetail.dbcontacts.Customer
import edu.ufp.pam.examplos.masterdetail.dbcontacts.CustomerDao
import edu.ufp.pam.examplos.masterdetail.dbcontacts.CustomersDatabase
import edu.ufp.pam.examplos.masterdetail.dbcontacts.LoaderCustomersContentDatabase
import edu.ufp.pam.exemplos.masterdetail.dbcontacts.CustomersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CustomersViewModel(app: Application) : AndroidViewModel(app) {

    private val repository: CustomersRepository

    // Use LiveData and cache customers returned by repository:
    //  - Put an observer on data fr UI to receive updates (instead of polling for changes).
    //  - The ViewModel separates the UI from the Repository.
    var allCustomers: LiveData<List<Customer>>

    init {
        //val customerDao = CustomersDatabase.getCustomerDatabaseInstance(application).customerDao()
        val customerDao =
            CustomersDatabase.getCustomerDatabaseInstance(app, viewModelScope).customerDao()
        repository = CustomersRepository(customerDao)
        allCustomers = repository.allCustomers
    }

    /** Launch new (non-blocking) coroutine to insert a customer */
    fun insert(customer: Customer) =
        viewModelScope.launch(Dispatchers.IO) {
            Log.e(this.javaClass.simpleName,
                "launch(): async insert new customer ${customer}"
            )
            repository.insertCustomer(customer)
        }

    private val customers: MutableLiveData<List<Customer>> by lazy {
        MutableLiveData<List<Customer>>().also {
            loadUsers()
        }
    }

    fun getCustomers(): LiveData<List<Customer>> {
        return customers
    }

    /** Do an asynchronous operation to fetch customers */
    private fun loadUsers() {
        Log.e(
            this.javaClass.simpleName,
            "loadUsers(): going to load customer to DB..."
        )
        /* 1. Execute and AsynTask... deprecated! */
        //val existingCustomers: List<Customer> = LoadCustomersFromDatabaseAsyncTask().execute("").get()
        //customers.postValue(existingCustomers)

        /* 2. Create a new coroutine to move the execution off the UI thread */
        viewModelScope.launch(Dispatchers.IO) {
            Log.e(this.javaClass.simpleName,
                "launch(): loading all customers..."
            )
            val customerDao: CustomerDao = LoaderCustomersContentDatabase.getCustomerDao()
            //val allCustomers: LiveData<List<Customer>> = customerDao.loadAllCustomers()
            allCustomers = customerDao.loadAllCustomers()
        }
    }
}