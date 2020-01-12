package vn.semicolon.filepicker.data

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

interface StringEncryptor {
    fun encrypt(s: String): String
}

class MD5StringEncryptor : StringEncryptor {
    override fun encrypt(s: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digested = md.digest(s.toByteArray())
        return digested.joinToString("") {
            String.format("%02x", it)
        }
    }
}

open class BaseSharedPreferences(name: String, c: Context) : AppSharedPreference,
    Editor {

    open protected val stringEncryptor: StringEncryptor =
        MD5StringEncryptor()

    open protected var encryptKey: Boolean = true

    private var shared: SharedPreferences = c.getSharedPreferences(name, Context.MODE_PRIVATE)

    private val editor: SharedPreferences.Editor
        get() {
            return shared.edit()
        }

    override fun clear(): Editor {
        editor.clear().apply()
        return this
    }

    private fun encryptKey(s: String): String {
        return if (encryptKey) stringEncryptor.encrypt(s) else s
    }

    override fun putLong(key: String, value: Long): Editor {
        editor.putLong(encryptKey(key), value).apply()
        return this
    }

    override fun putInt(key: String, value: Int): Editor {
        editor.putInt(encryptKey(key), value).apply()
        return this
    }

    override fun remove(key: String): Editor {
        editor.remove(key).apply()
        return this
    }

    override fun putBoolean(key: String, value: Boolean): Editor {
        editor.putBoolean(encryptKey(key), value).apply()
        return this
    }

    override fun putStringSet(key: String, values: Set<String>?): Editor {
        editor.putStringSet(encryptKey(key), values).apply()
        return this
    }

    override fun putFloat(key: String, value: Float): Editor {
        editor.putFloat(encryptKey(key), value).apply()
        return this
    }

    override fun putString(key: String, value: String?): Editor {
        editor.putString(encryptKey(key), value).apply()
        return this
    }

    override fun contains(key: String): Boolean {
        return shared.contains(encryptKey(key))
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return shared.getBoolean(encryptKey(key), defValue)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        return shared.unregisterOnSharedPreferenceChangeListener(listener)
    }

    override fun getInt(key: String, defValue: Int): Int {
        return shared.getInt(encryptKey(key), defValue)
    }

    override fun getAll(): MutableMap<String, *> {
        return shared.all
    }

    override fun getLong(key: String, defValue: Long): Long {
        return shared.getLong(encryptKey(key), defValue)
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return shared.getFloat(encryptKey(key), defValue)
    }

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        return shared.getStringSet(encryptKey(key), defValues)
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        return shared.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun getString(key: String, defValue: String?): String? {
        return shared.getString(encryptKey(key), defValue)
    }

}

interface Editor {

    fun putString(key: String, value: String?): Editor

    fun putStringSet(key: String, values: Set<String>?): Editor

    fun putInt(key: String, value: Int): Editor

    fun putLong(key: String, value: Long): Editor

    fun putFloat(key: String, value: Float): Editor

    fun putBoolean(key: String, value: Boolean): Editor

    fun remove(key: String): Editor

    fun clear(): Editor

}

interface AppSharedPreference {

    fun getAll(): Map<String, *>

    fun getString(key: String, defValue: String?): String?

    fun getStringSet(key: String, defValues: Set<String>?): Set<String>?

    fun getInt(key: String, defValue: Int): Int

    fun getLong(key: String, defValue: Long): Long

    fun getFloat(key: String, defValue: Float): Float

    fun getBoolean(key: String, defValue: Boolean): Boolean

    operator fun contains(key: String): Boolean

    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener)

    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener)
}