package vn.semicolon.filepicker.data

import android.content.Context

class UserGuideInfo(c: Context) :
    BaseSharedPreferences("UserGuideInfo", c) {
    fun isUseFirstTime(): Boolean {
        return getBoolean("first_time", true)
    }

    fun setUseFirstTime(isFirstTime: Boolean) {
        putBoolean("first_time", isFirstTime)
    }
}