package com.example.remindme.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Patient::class,
        Medicine::class,
        MedicineSchedule::class,
        MedicineTaken::class,
        MedicineRefill::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MedicineDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
    abstract fun patientDao(): PatientDao

    companion object {
        @Volatile
        private var INSTANCE: MedicineDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the medicine_refills table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `medicine_refills` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `medicineId` INTEGER NOT NULL,
                        `weeklyCount` INTEGER NOT NULL,
                        `lastRefillDate` TEXT NOT NULL,
                        `nextRefillDate` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        FOREIGN KEY(`medicineId`) REFERENCES `medicines`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                
                // Create index for medicineId
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_medicine_refills_medicineId` ON `medicine_refills` (`medicineId`)")
            }
        }

        fun getDatabase(context: Context): MedicineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicineDatabase::class.java,
                    "medicine_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 