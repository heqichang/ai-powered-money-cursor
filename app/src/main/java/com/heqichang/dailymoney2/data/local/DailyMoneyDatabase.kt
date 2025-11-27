package com.heqichang.dailymoney2.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.heqichang.dailymoney2.data.local.converter.CategoryTypeConverter
import com.heqichang.dailymoney2.data.local.converter.LocalDateConverter
import com.heqichang.dailymoney2.data.local.converter.YearMonthConverter
import com.heqichang.dailymoney2.data.local.dao.CategoryDao
import com.heqichang.dailymoney2.data.local.dao.LedgerDao
import com.heqichang.dailymoney2.data.local.dao.TransactionDao
import com.heqichang.dailymoney2.data.local.entity.CategoryEntity
import com.heqichang.dailymoney2.data.local.entity.LedgerEntity
import com.heqichang.dailymoney2.data.local.entity.TransactionEntity

@Database(
    entities = [
        LedgerEntity::class,
        CategoryEntity::class,
        TransactionEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(
    LocalDateConverter::class,
    YearMonthConverter::class,
    CategoryTypeConverter::class
)
abstract class DailyMoneyDatabase : RoomDatabase() {
    abstract fun ledgerDao(): LedgerDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "daily_money.db"
    }
}

