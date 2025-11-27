package com.heqichang.dailymoney2.data.local.converter

import androidx.room.TypeConverter
import com.heqichang.dailymoney2.data.local.entity.CategoryType
import java.time.LocalDate
import java.time.YearMonth

class LocalDateConverter {
    @TypeConverter
    fun toDatabase(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun fromDatabase(value: String?): LocalDate? = value?.let(LocalDate::parse)
}

class YearMonthConverter {
    @TypeConverter
    fun toDatabase(value: YearMonth?): String? = value?.toString()

    @TypeConverter
    fun fromDatabase(value: String?): YearMonth? = value?.let(YearMonth::parse)
}

class CategoryTypeConverter {
    @TypeConverter
    fun toDatabase(value: CategoryType?): String? = value?.name

    @TypeConverter
    fun fromDatabase(value: String?): CategoryType? = value?.let(CategoryType::valueOf)
}

