package com.heqichang.dailymoney2.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val preferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "daily_money_prefs"
        private const val KEY_LAST_LEDGER_ID = "last_ledger_id"
    }

    fun saveLastLedgerId(ledgerId: Long) {
        preferences.edit().putLong(KEY_LAST_LEDGER_ID, ledgerId).apply()
    }

    fun getLastLedgerId(): Long? {
        val id = preferences.getLong(KEY_LAST_LEDGER_ID, -1L)
        return if (id == -1L) null else id
    }

    fun clearLastLedgerId() {
        preferences.edit().remove(KEY_LAST_LEDGER_ID).apply()
    }
}

