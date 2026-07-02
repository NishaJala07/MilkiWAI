package com.example.milkiwai.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.milkiwai.data.dao.*
import com.example.milkiwai.data.entity.*
import com.google.firebase.auth.FirebaseAuth

@Database(
    entities = [
        Customer::class,
        MilkBrand::class,
        CustomerSubscription::class,
        QuantityChange::class,
        MonthlyBill::class,
        BillItem::class,
        Vacation::class,
        VoiceHistory::class,
        Delivery::class,
        NotificationEntity::class,
        Stock::class,
        DailyTransaction::class
    ],
    version = 15
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun milkBrandDao(): MilkBrandDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun quantityChangeDao(): QuantityChangeDao
    abstract fun monthlyBillDao(): MonthlyBillDao
    abstract fun billItemDao(): BillItemDao
    abstract fun vacationDao(): VacationDao
    abstract fun voiceHistoryDao(): VoiceHistoryDao
    abstract fun deliveryDao(): DeliveryDao
    abstract fun notificationDao(): NotificationDao
    abstract fun stockDao(): StockDao
    abstract fun dailyTransactionDao(): DailyTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private var CURRENT_UID: String? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS milk_brands (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        brandName TEXT NOT NULL,
                        pricePerLitre REAL NOT NULL
                    )
                    """
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS customer_subscriptions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        customerId INTEGER NOT NULL,
                        brandId INTEGER NOT NULL,
                        quantity REAL NOT NULL,
                        FOREIGN KEY(customerId) REFERENCES customers(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(brandId) REFERENCES milk_brands(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE customer_subscriptions ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS quantity_changes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        customerId INTEGER NOT NULL,
                        brandId INTEGER NOT NULL,
                        oldQuantity REAL NOT NULL,
                        newQuantity REAL NOT NULL,
                        startDate TEXT NOT NULL,
                        endDate TEXT NOT NULL,
                        remarks TEXT NOT NULL
                    )
                    """
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE quantity_changes ADD COLUMN status TEXT NOT NULL DEFAULT 'ACTIVE'")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS monthly_bills (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        customerId INTEGER NOT NULL,
                        month INTEGER NOT NULL,
                        year INTEGER NOT NULL,
                        totalAmount REAL NOT NULL,
                        billDate TEXT NOT NULL,
                        paymentStatus TEXT NOT NULL DEFAULT 'PENDING'
                    )
                    """
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS bill_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        billId INTEGER NOT NULL,
                        brandId INTEGER NOT NULL,
                        totalQuantity REAL NOT NULL,
                        ratePerLitre REAL NOT NULL,
                        amount REAL NOT NULL,
                        FOREIGN KEY(billId) REFERENCES monthly_bills(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS vacations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        customerId INTEGER NOT NULL,
                        startDate TEXT NOT NULL,
                        endDate TEXT NOT NULL,
                        remarks TEXT NOT NULL,
                        FOREIGN KEY(customerId) REFERENCES customers(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """
                )
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE monthly_bills ADD COLUMN pdfPath TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS voice_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        command TEXT NOT NULL,
                        date TEXT NOT NULL,
                        status TEXT NOT NULL
                    )
                    """
                )
            }
        }

        private val MIGRATION_9_11 = object : Migration(9, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS deliveries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        customerId INTEGER NOT NULL,
                        brandId INTEGER NOT NULL,
                        quantity REAL NOT NULL,
                        deliveryDate TEXT NOT NULL,
                        status TEXT NOT NULL,
                        remarks TEXT NOT NULL DEFAULT ''
                    )
                    """
                )
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE customers ADD COLUMN area TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE customers ADD COLUMN landmark TEXT NOT NULL DEFAULT ''")
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS notifications (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        customerId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        message TEXT NOT NULL,
                        type TEXT NOT NULL,
                        createdDate TEXT NOT NULL,
                        status TEXT NOT NULL
                    )
                    """
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS stock (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        brandId INTEGER NOT NULL,
                        availableQuantity REAL NOT NULL
                    )
                    """
                )
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE monthly_bills ADD COLUMN paymentMode TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE monthly_bills ADD COLUMN transactionId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE monthly_bills ADD COLUMN paymentDate TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE customers ADD COLUMN latitude REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE customers ADD COLUMN longitude REAL NOT NULL DEFAULT 0.0")
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS daily_transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        customerId INTEGER NOT NULL,
                        brandId INTEGER NOT NULL,
                        transactionDate TEXT NOT NULL,
                        quantity REAL NOT NULL
                    )
                    """
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "default"
            
            val currentInstance = INSTANCE
            if (currentInstance != null && CURRENT_UID == uid) {
                return currentInstance
            }

            return synchronized(this) {
                // Re-check after acquiring lock
                if (INSTANCE != null && CURRENT_UID != uid) {
                    INSTANCE?.close()
                    INSTANCE = null
                }
                
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "milk_database_$uid"
                )
                    .addMigrations(
                        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, 
                        MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
                        MIGRATION_9_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14,
                        MIGRATION_14_15
                    )
                    .build()
                INSTANCE = instance
                CURRENT_UID = uid
                instance
            }
        }
    }
}
