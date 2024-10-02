package com.mayowa.permissiondemo.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferenceUtil @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
    }

    fun put(key: PrefKey, value: String) {
        sharedPreferences.edit().putString(key.name, value).apply()
    }

    fun put(key: PrefKey, value: Set<String>) {
        sharedPreferences.edit().putStringSet(key.name, value).apply()
    }

    fun get(key: PrefKey): String? {
        return sharedPreferences.getString(key.name, null)
    }

    fun has(key: PrefKey): Boolean {
        return sharedPreferences.contains(key.name)
    }

    fun contains(key: PrefKey, value: String): Boolean {
        return sharedPreferences.getStringSet(key.name, null)?.contains(value) ?: false
    }

    fun containsAny(key: PrefKey, value: Set<String>): Boolean {
        val values = sharedPreferences.getStringSet(key.name, null) ?: return false
        return value.any { values.contains(it) }
    }


    companion object {
        const val PREF_FILE = "pref_file"
    }
}

enum class PrefKey {
    DENIED_PERMISSIONS
}