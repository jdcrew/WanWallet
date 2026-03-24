package com.jdcrew.wanwallet.di

import android.content.Context
import androidx.room.Room
import com.jdcrew.wanwallet.data.local.CategoryDao
import com.jdcrew.wanwallet.data.local.TransactionDao
import com.jdcrew.wanwallet.data.local.WanWalletDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WanWalletDatabase {
        return Room.databaseBuilder(
            context,
            WanWalletDatabase::class.java,
            WanWalletDatabase.DATABASE_NAME
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideTransactionDao(database: WanWalletDatabase): TransactionDao {
        return database.transactionDao()
    }
    
    @Provides
    @Singleton
    fun provideCategoryDao(database: WanWalletDatabase): CategoryDao {
        return database.categoryDao()
    }
}
