package com.heqichang.dailymoney2.di

import com.heqichang.dailymoney2.data.local.dao.CategoryDao
import com.heqichang.dailymoney2.data.local.dao.LedgerDao
import com.heqichang.dailymoney2.data.local.dao.TransactionDao
import com.heqichang.dailymoney2.data.repository.DefaultCategoryRepository
import com.heqichang.dailymoney2.data.repository.DefaultLedgerRepository
import com.heqichang.dailymoney2.data.repository.DefaultTransactionRepository
import com.heqichang.dailymoney2.domain.repository.CategoryRepository
import com.heqichang.dailymoney2.domain.repository.LedgerRepository
import com.heqichang.dailymoney2.domain.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideLedgerRepository(
        ledgerDao: LedgerDao
    ): LedgerRepository = DefaultLedgerRepository(ledgerDao)

    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryDao: CategoryDao
    ): CategoryRepository = DefaultCategoryRepository(categoryDao)

    @Provides
    @Singleton
    fun provideTransactionRepository(
        transactionDao: TransactionDao
    ): TransactionRepository = DefaultTransactionRepository(transactionDao)
}

