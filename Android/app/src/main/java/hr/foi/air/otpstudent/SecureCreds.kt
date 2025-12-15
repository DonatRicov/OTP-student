package hr.foi.air.otpstudent

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureCreds {
    private const val PREFS = "secure_creds"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASS = "pass"

    private fun prefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREFS,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }

    fun save(context: Context, email: String, pass: String) {
        prefs(context).edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASS, pass)
            .apply()
    }

    fun getEmail(context: Context): String? = prefs(context).getString(KEY_EMAIL, null)
    fun getPass(context: Context): String? = prefs(context).getString(KEY_PASS, null)

    fun hasCreds(context: Context): Boolean = getEmail(context) != null && getPass(context) != null
}
