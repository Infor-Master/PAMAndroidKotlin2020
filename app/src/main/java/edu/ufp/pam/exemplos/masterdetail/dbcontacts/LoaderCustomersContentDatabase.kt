package edu.ufp.pam.examplos.masterdetail.dbcontacts

import android.content.Context
import android.util.Log
import java.util.ArrayList
import java.util.HashMap

@Suppress("JAVA_CLASS_ON_COMPANION")
class LoaderCustomersContentDatabase {

    companion object Factory {
        private lateinit var customerDao: CustomerDao
        //private lateinit var customerTaskDao: CustomerTaskDao
        //private lateinit var customerTaskDetailViewDao: CustomerTaskDetailViewDao
        private lateinit var db: CustomersDatabase //Database

        val COUNT = 18

        private var _CUSTOMER_ITEMS: MutableList<CustomerItem> = ArrayList()
        val CUSTOMER_ITEMS: MutableList<CustomerItem>
            get() {
                /*if (_CUSTOMER_ITEMS == null) {
                    _CUSTOMER_ITEMS = ArrayList() // Type parameters are inferred
                }*/
                return _CUSTOMER_ITEMS
            }

        private var _CUSTOMER_ITEM_MAP: MutableMap<String, CustomerItem> = HashMap()
        val CUSTOMER_ITEM_MAP: MutableMap<String, CustomerItem>
            get() {
                /*if (_CUSTOMER_ITEM_MAP == null) {
                    _CUSTOMER_ITEM_MAP = HashMap() // Type parameters are inferred
                }*/
                return _CUSTOMER_ITEM_MAP
            }

        fun getCustomerDao() : CustomerDao{
            return customerDao
        }

        /**
         * Create the database by getting its INSTANCE
         */
        fun createDb(context: Context) {
            Log.e(
                this.javaClass.simpleName,
                "createDb(): going to create DB and get customerDao..."
            )
            //Create a version of the DB
            db = CustomersDatabase.getCustomerDatabaseInstance(context)
            customerDao = db.customerDao()
        }

        /**
         * Close database
         */
        fun closeDb() {
            Log.e(this.javaClass.simpleName, "closeDb(): going to close DB...")
            db.close()
        }

        /**
         * Insert some data into database for presenting into CustomerItemContactDetailListActivity
         */
        fun addSampleItemsToDatabase() {
            // Create and Insert some sample Customers.
            for (i in 1..COUNT) {
                //CREATE
                val customer: Customer =
                    Customer(
                        i, "Tio Patinhas $i", "Patinhas $i Lda",
                        "Rua Sesamo $i", "Porto", "+35122000000$i"
                    )
                Log.e(
                    this.javaClass.simpleName,
                    "addSampleItemsToDatabase(): create customer = $customer"
                )
                //INSERT
                val id: Long = customerDao.insertCustomer(customer)
                Log.e(
                    this.javaClass.simpleName,
                    "addSampleItemsToDatabase(): added record id = $id"
                )
            }
            //Query for all Customers
            val allCustomers = customerDao.loadAllCustomers()
            Log.e(
                this.javaClass.simpleName,
                "testeInsertCustomersAndListInserted(): allCustomers.size = ${allCustomers.size}"
            )
            //Populate CUSTOMER_ITEMS and CUSTOMER_ITEM_MAP with all Customers
            for (c: Customer in allCustomers) {
                addItem(createCostumerItem(c))
            }
        }

        private fun addItem(item: CustomerItem) {
            CUSTOMER_ITEMS.add(item)
            CUSTOMER_ITEM_MAP.put(item.id, item)
        }

        private fun createCostumerItem(c: Customer): CustomerItem {
            return CustomerItem("${c.customerId}", c.customerName, makeDetails(c))
        }

        private fun makeDetails(c: Customer): String {
            val builder = StringBuilder()
            builder.append(c.customerCompany).append("\n")
            builder.append(c.customerAddress).append("\n")
            builder.append(c.customerCity).append("\n")
            builder.append(c.customerPhone)
            return builder.toString()
        }
    }

    /**
     * A CustomerItem item representing a piece of content from Customer.
     */
    data class CustomerItem(val id: String, val content: String?, val details: String) {
        override fun toString(): String = if (content!=null) content else ""
    }
}