package com.jdcrew.wanwallet.data.local

import androidx.room.*
import com.jdcrew.wanwallet.data.model.Category
import com.jdcrew.wanwallet.data.model.Transaction
import com.jdcrew.wanwallet.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions ORDER BY time DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY time DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY time DESC")
    fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long
    
    @Update
    suspend fun update(transaction: Transaction)
    
    @Delete
    suspend fun delete(transaction: Transaction)
    
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND time >= :startTime AND time <= :endTime")
    suspend fun getTotalByTypeAndTimeRange(type: TransactionType, startTime: Long, endTime: Long): Double?
    
    @Query("SELECT * FROM transactions WHERE time >= :startTime AND time <= :endTime ORDER BY time DESC")
    fun getTransactionsByTimeRange(startTime: Long, endTime: Long): Flow<List<Transaction>>
}

@Dao
interface CategoryDao {
    
    @Query("SELECT * FROM categories ORDER BY `order` ASC")
    fun getAllCategories(): Flow<List<Category>>
    
    @Query("SELECT * FROM categories WHERE type = :type ORDER BY `order` ASC")
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>>
    
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long
    
    @Update
    suspend fun update(category: Category)
    
    @Delete
    suspend fun delete(category: Category)
    
    @Query("UPDATE categories SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateCategoryEnabled(id: Long, isEnabled: Boolean)
}

@Database(
    entities = [Transaction::class, Category::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class WanWalletDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    
    companion object {
        const val DATABASE_NAME = "wanwallet_database"
    }
}

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }
    
    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }
    
    @TypeConverter
    fun fromPaymentChannel(value: com.jdcrew.wanwallet.data.model.PaymentChannel): String {
        return value.name
    }
    
    @TypeConverter
    fun toPaymentChannel(value: String): com.jdcrew.wanwallet.data.model.PaymentChannel {
        return com.jdcrew.wanwallet.data.model.PaymentChannel.valueOf(value)
    }
}
