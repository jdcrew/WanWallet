package com.jdcrew.wanwallet.data.repository

import com.jdcrew.wanwallet.data.local.BudgetDao
import com.jdcrew.wanwallet.data.model.Budget
import com.jdcrew.wanwallet.data.model.BudgetPeriod
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao
) {
    
    val allBudgets: Flow<List<Budget>> = budgetDao.getAllBudgets()
    
    fun getBudgetsByPeriod(period: BudgetPeriod): Flow<List<Budget>> {
        return budgetDao.getBudgetsByPeriod(period)
    }
    
    suspend fun getBudgetById(id: Long): Budget? {
        return budgetDao.getBudgetById(id)
    }
    
    suspend fun createBudget(budget: Budget): Long {
        return budgetDao.insert(budget)
    }
    
    suspend fun updateBudget(budget: Budget) {
        budgetDao.update(budget)
    }
    
    suspend fun deleteBudget(budget: Budget) {
        budgetDao.delete(budget)
    }
    
    suspend fun updateSpent(id: Long, spent: Double) {
        budgetDao.updateSpent(id, spent)
    }
    
    suspend fun disableBudget(id: Long) {
        budgetDao.disableBudget(id)
    }
}
