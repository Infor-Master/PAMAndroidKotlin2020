package edu.ufp.pam.examplos.masterdetail.dbcontacts

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * The Room database that contains the tables for all entities (e.g. Customer, CustomerTask, etc.).
 *
 * Get an instance of created database using following code:
 *   val db = Room.databaseBuilder(applicationContext, CustomersDatabase::class.java, "Customers.db").build()
 *
 */
@Database(
    entities = [Customer::class],
    views = [],
    version = 1
)
abstract class CustomersDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao

    //Behaves like a static attribute
    companion object {

        @Volatile
        private var INSTANCE: CustomersDatabase? = null

        fun getCustomerDatabaseInstance(context: Context): CustomersDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                CustomersDatabase::class.java,
                "Customers.db"
            )
                .fallbackToDestructiveMigration()
                //.addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()

        fun getCustomerDatabaseInstance(context: Context, scope: CoroutineScope): CustomersDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context, scope).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context, scope: CoroutineScope) =
            Room.databaseBuilder(
                context.applicationContext,
                CustomersDatabase::class.java,
                "Customers.db"
            )
                .addCallback(CustomersRoomDatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                //.addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
    }

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE `tasktypes` (`id` INTEGER, `tasktitle` TEXT, " +
                        "PRIMARY KEY(`id`))"
            )
        }
    }

    val MIGRATION_2_3: Migration
        get() = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tasktypes ADD COLUMN taskpriority INTEGER")
            }
        }

    private class CustomersRoomDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {

        /* Populate the database just the first time app is launched
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            //...
        }*/

        /** Delete all content and repopulate the database whenever the app is started */
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.customerDao())
                }
            }
        }

        suspend fun populateDatabase(customerDao: CustomerDao) {
            // Delete all content here.
            customerDao.deleteAllcustomers()

            for (i in 1..LoaderCustomersContentDatabase.COUNT) {
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
        }
    }
}