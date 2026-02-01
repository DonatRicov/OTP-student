package hr.foi.air.otpstudent.data.auth

import android.content.Context

object AppLockStore {

    private const val PREFS = "app_lock_prefs"
    private const val KEY_LAST_BACKGROUND_AT = "last_background_at"

    fun markBackgrounded(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_BACKGROUND_AT, System.currentTimeMillis())
            .apply()
    }

    fun getLastBackgroundAt(context: Context): Long {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_BACKGROUND_AT, 0L)
    }

    fun shouldLock(context: Context, thresholdMs: Long): Boolean {
        val last = getLastBackgroundAt(context)
        if (last == 0L) return false
        return (System.currentTimeMillis() - last) >= thresholdMs
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_LAST_BACKGROUND_AT)
            .apply()
    }
}
