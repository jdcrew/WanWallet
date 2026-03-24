package com.jdcrew.wanwallet.di

import com.jdcrew.wanwallet.data.repository.CategoryRepository
import com.jdcrew.wanwallet.data.repository.TransactionRepository
import com.jdcrew.wanwallet.ui.viewmodel.AddTransactionViewModel
import com.jdcrew.wanwallet.ui.viewmodel.HomeViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {
    
    @Provides
    @ViewModelScoped
    fun provideHomeViewModel(
        transactionRepository: TransactionRepository
    ): HomeViewModel {
        return HomeViewModel(transactionRepository)
    }
    
    @Provides
    @ViewModelScoped
    fun provideAddTransactionViewModel(
        transactionRepository: TransactionRepository,
        categoryRepository: CategoryRepository
    ): AddTransactionViewModel {
        return AddTransactionViewModel(transactionRepository, categoryRepository)
    }
}
