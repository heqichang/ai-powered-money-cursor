package com.heqichang.dailymoney2.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.heqichang.dailymoney2.data.local.DailyMoneyDatabase
import com.heqichang.dailymoney2.data.local.dao.CategoryDao
import com.heqichang.dailymoney2.data.local.dao.LedgerDao
import com.heqichang.dailymoney2.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 创建新表（不包含 currency_code 列）
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS ledgers_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT NOT NULL,
                    created_at_epoch_millis INTEGER NOT NULL
                )
            """.trimIndent())
            
            // 复制数据（排除 currency_code 列）
            database.execSQL("""
                INSERT INTO ledgers_new (id, name, description, created_at_epoch_millis)
                SELECT id, name, description, created_at_epoch_millis
                FROM ledgers
            """.trimIndent())
            
            // 删除旧表
            database.execSQL("DROP TABLE ledgers")
            
            // 重命名新表
            database.execSQL("ALTER TABLE ledgers_new RENAME TO ledgers")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): DailyMoneyDatabase = Room.databaseBuilder(
        context,
        DailyMoneyDatabase::class.java,
        DailyMoneyDatabase.DATABASE_NAME
    ).addMigrations(MIGRATION_1_2).build()

    @Provides
    fun provideLedgerDao(database: DailyMoneyDatabase): LedgerDao = database.ledgerDao()

    @Provides
    fun provideCategoryDao(database: DailyMoneyDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideTransactionDao(database: DailyMoneyDatabase): TransactionDao = database.transactionDao()
}

