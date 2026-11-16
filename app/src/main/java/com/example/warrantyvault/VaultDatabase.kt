package com.example.warrantyvault

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow



@Dao
interface ItemDao {

    // Query matches tableName = "items"
    @Query("SELECT * FROM items")
    fun getAllItems(): Flow<List<VaultItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultItem): Long

    @Delete
    suspend fun deleteItem(item: VaultItem)
}

@Database(entities = [VaultItem::class], version = 4)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: VaultDatabase? = null

        fun getDatabase(context: Context): VaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VaultDatabase::class.java,
                    "warranty_vault.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}