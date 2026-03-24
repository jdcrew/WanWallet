package com.jdcrew.wanwallet.data.local

import androidx.room.*
import com.jdcrew.wanwallet.data.model.Budget
import com.jdcrew.wanwallet.data.model.BudgetPeriod
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    
    @Query("SELECT * FROM budgets WHERE isEnabled = 1 ORDER BY startDate DESC")
    fun getAllBudgets(): Flow<List<Budget>>
    
    @Query("SELECT * FROM budgets WHERE period = :period AND isEnabled = 1 ORDER BY startDate DESC")
    fun getBudgetsByPeriod(period: BudgetPeriod): Flow<List<Budget>>
    
    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Long): Budget?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget): Long
    
    @Update
    suspend fun update(budget: Budget)
    
    @Delete
    suspend fun delete(budget: Budget)
    
    @Query("UPDATE budgets SET spent = :spent WHERE id = :id")
    suspend fun updateSpent(id: Long, spent: Double)
    
    @Query("UPDATE budgets SET isEnabled = 0 WHERE id = :id")
    suspend fun disableBudget(id: Long)
}
