package edu.ufp.pam.examplos.masterdetail.dbcontacts

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Completable

/**
 * Data Access Object for the customers table.
 *
 * O Room oferece compatibilidade com valores de retorno dos tipos RxJava2:
 *   Métodos @Query: compativeis com valores de retorno do tipo Publisher, Flowable e Observable;
 *   Métodos @Insert, @Update e @Delete: compativeis com valores de retorno do tipo Completable, Single<T> e Maybe<T>.
 *
 *
 * RxJava2 introduces different types of Observables:
 *  Single: an Observable which only emits one item or throws an error (e.g. a network call,
 *          with retrofit, you return an Observable or Flowable; care for the response once
 *          you can replace this with Single<T>)
 *
 *  Maybe: similar to Single but it allows for no emissions as well;
 *
 *  Completable: only concerned with execution completion, i.e., whether the task has reach to
 *          completion or some error has occurred;
 *
 *
 *  Flowable: just like an Observable with backpressure mechanism (when observable
 *          generates huge amounts of events, flowable limits buffer size).
 */
@Dao
interface CustomerDao {

    /**
     * Get all customers.
     * @return all customers from the table.
     */
    @Query("SELECT * FROM customers")
    fun loadAllCustomers(): LiveData<List<Customer>>

    /**
     * Get a customer by id.
     * @return the customer from the table with a specific id.
     */
    @Query("SELECT * FROM customers WHERE customerid = :id LIMIT 1")
    fun getCustomerById(id: String): Customer

    /**
     * Get customers by city.
     * @return the customers from the table with city in cityname.
     */
    @Query("SELECT * FROM customers WHERE customercity LIKE :city")
    fun getCustomersByCity(city: String): Array<Customer>

    /**
     * Get customers by cities.
     * @return the customers from the table with specific cities.
     */
    @Query("SELECT * FROM customers WHERE customercity IN (:cities)")
    fun loadCustomersFromCities(cities: List<String>): List<Customer>

    ///**
    // * Get all customers minimal info.
    // * @return the customers from the table with specific cities.
    // */
    //@Query("SELECT customername, customercompany FROM customers")
    //fun loadFullCustomerMinimalInfo(): List<CustomerMinimal>

    /**
     * Insert a customer in the database (returns id), replace it if already exists.
     * @param customer the customer to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomer(customer: Customer): Long

    /**
     * Insert a customer in the database (returns id), replace it if already exists.
     * @param customer the customer to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomerCompletable(customer: Customer): Completable

    /**
     * Insert 1+ customers into database. If the customers already exists, replace them.
     * @param customers the set of customers to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomers(vararg customers: Customer): List<Long>

    /**
     * Update 1+ customers into database (returns number of updates rows).
     * @param customers the set of customers to be updated.
     */
    @Update
    fun updateCustomers(vararg customers: Customer): Int

    /**
     * Delete 1+ customers from database (returns number of deleted rows).
     * @param customers the set of customers to be deleted.
     */
    @Delete
    fun deleteCustomers(vararg customers: Customer): Int

    /**
     * Delete all customers.
     */
    @Query("DELETE FROM customers")
    fun deleteAllcustomers(): Int
}