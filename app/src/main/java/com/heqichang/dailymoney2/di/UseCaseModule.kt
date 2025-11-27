package com.heqichang.dailymoney2.di

import com.heqichang.dailymoney2.domain.repository.CategoryRepository
import com.heqichang.dailymoney2.domain.repository.LedgerRepository
import com.heqichang.dailymoney2.domain.repository.TransactionRepository
import com.heqichang.dailymoney2.domain.usecase.category.DeleteCategoryUseCase
import com.heqichang.dailymoney2.domain.usecase.category.ObserveCategoriesUseCase
import com.heqichang.dailymoney2.domain.usecase.category.UpsertCategoryUseCase
import com.heqichang.dailymoney2.domain.usecase.ledger.DeleteLedgerUseCase
import com.heqichang.dailymoney2.domain.usecase.ledger.ExportLedgerDataUseCase
import com.heqichang.dailymoney2.domain.usecase.ledger.ImportLedgerDataUseCase
import com.heqichang.dailymoney2.domain.usecase.ledger.ObserveLedgerUseCase
import com.heqichang.dailymoney2.domain.usecase.ledger.ObserveLedgersUseCase
import com.heqichang.dailymoney2.domain.usecase.ledger.UpsertLedgerUseCase
import com.heqichang.dailymoney2.domain.usecase.statistics.ObserveMonthlyStatsUseCase
import com.heqichang.dailymoney2.domain.usecase.transaction.DeleteTransactionUseCase
import com.heqichang.dailymoney2.domain.usecase.transaction.ObserveTransactionsForDateUseCase
import com.heqichang.dailymoney2.domain.usecase.transaction.ObserveTransactionsUseCase
import com.heqichang.dailymoney2.domain.usecase.transaction.UpsertTransactionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideObserveLedgersUseCase(
        ledgerRepository: LedgerRepository
    ): ObserveLedgersUseCase = ObserveLedgersUseCase(ledgerRepository)

    @Provides
    @Singleton
    fun provideObserveLedgerUseCase(
        ledgerRepository: LedgerRepository
    ): ObserveLedgerUseCase = ObserveLedgerUseCase(ledgerRepository)

    @Provides
    @Singleton
    fun provideUpsertLedgerUseCase(
        ledgerRepository: LedgerRepository
    ): UpsertLedgerUseCase = UpsertLedgerUseCase(ledgerRepository)

    @Provides
    @Singleton
    fun provideDeleteLedgerUseCase(
        ledgerRepository: LedgerRepository,
        transactionRepository: TransactionRepository
    ): DeleteLedgerUseCase = DeleteLedgerUseCase(ledgerRepository, transactionRepository)

    @Provides
    @Singleton
    fun provideExportLedgerDataUseCase(
        ledgerRepository: LedgerRepository,
        categoryRepository: CategoryRepository,
        transactionRepository: TransactionRepository
    ): ExportLedgerDataUseCase = ExportLedgerDataUseCase(ledgerRepository, categoryRepository, transactionRepository)

    @Provides
    @Singleton
    fun provideImportLedgerDataUseCase(
        ledgerRepository: LedgerRepository,
        categoryRepository: CategoryRepository,
        transactionRepository: TransactionRepository
    ): ImportLedgerDataUseCase = ImportLedgerDataUseCase(ledgerRepository, categoryRepository, transactionRepository)

    @Provides
    @Singleton
    fun provideObserveCategoriesUseCase(
        categoryRepository: CategoryRepository
    ): ObserveCategoriesUseCase = ObserveCategoriesUseCase(categoryRepository)

    @Provides
    @Singleton
    fun provideUpsertCategoryUseCase(
        categoryRepository: CategoryRepository
    ): UpsertCategoryUseCase = UpsertCategoryUseCase(categoryRepository)

    @Provides
    @Singleton
    fun provideDeleteCategoryUseCase(
        categoryRepository: CategoryRepository
    ): DeleteCategoryUseCase = DeleteCategoryUseCase(categoryRepository)

    @Provides
    @Singleton
    fun provideObserveTransactionsUseCase(
        transactionRepository: TransactionRepository
    ): ObserveTransactionsUseCase = ObserveTransactionsUseCase(transactionRepository)

    @Provides
    @Singleton
    fun provideObserveTransactionsForDateUseCase(
        transactionRepository: TransactionRepository
    ): ObserveTransactionsForDateUseCase = ObserveTransactionsForDateUseCase(transactionRepository)

    @Provides
    @Singleton
    fun provideUpsertTransactionUseCase(
        transactionRepository: TransactionRepository
    ): UpsertTransactionUseCase = UpsertTransactionUseCase(transactionRepository)

    @Provides
    @Singleton
    fun provideDeleteTransactionUseCase(
        transactionRepository: TransactionRepository
    ): DeleteTransactionUseCase = DeleteTransactionUseCase(transactionRepository)

    @Provides
    @Singleton
    fun provideObserveMonthlyStatsUseCase(
        transactionRepository: TransactionRepository
    ): ObserveMonthlyStatsUseCase = ObserveMonthlyStatsUseCase(transactionRepository)
}

