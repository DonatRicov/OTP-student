package hr.foi.air.auth.pin

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object PinStore {

    private const val PREFS = "pin_secure_prefs"

    // legacy global keys (stari nacin
    private const val LEGACY_KEY_PIN = "pin"
    private const val LEGACY_KEY_ENABLED = "enabled"

    // global info o zadnjem korisniku
    private const val KEY_LAST_UID = "pin_last_uid"
    private const val KEY_LAST_USER_LABEL = "pin_last_user_label"

    private fun prefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREFS,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private fun pinKey(uid: String) = "pin_$uid"
    private fun enabledKey(uid: String) = "enabled_$uid"

    //Migracija ako postoje legacy pin il enabled prebaci ih na prvi uid koji se traži
    private fun migrateIfNeeded(context: Context, uid: String) {
        val p = prefs(context)

        val newPinKey = pinKey(uid)
        val newEnabledKey = enabledKey(uid)

        val hasLegacyPin = p.contains(LEGACY_KEY_PIN)
        val hasLegacyEnabled = p.contains(LEGACY_KEY_ENABLED)
        val hasNewPin = p.contains(newPinKey)
        val hasNewEnabled = p.contains(newEnabledKey)

        // Nema legacy podataka ili vec postoje novi podaci pa nema migracije
        if ((!hasLegacyPin && !hasLegacyEnabled) || (hasNewPin || hasNewEnabled)) return

        val legacyPin = p.getString(LEGACY_KEY_PIN, null)
        val legacyEnabled = p.getBoolean(LEGACY_KEY_ENABLED, false)

        p.edit().apply {
            if (!legacyPin.isNullOrBlank()) putString(newPinKey, legacyPin)
            if (hasLegacyEnabled) putBoolean(newEnabledKey, legacyEnabled)
            remove(LEGACY_KEY_PIN)
            remove(LEGACY_KEY_ENABLED)
        }.apply()

        if (legacyEnabled) setLastUid(context, uid)
    }

    fun getLastUid(context: Context): String? =
        prefs(context).getString(KEY_LAST_UID, null)

    fun setLastUid(context: Context, uid: String) {
        prefs(context).edit().putString(KEY_LAST_UID, uid).apply()
    }

    fun getLastUserLabel(context: Context): String? =
        prefs(context).getString(KEY_LAST_USER_LABEL, null)

    fun setLastUserLabel(context: Context, label: String?) {
        prefs(context).edit().putString(KEY_LAST_USER_LABEL, label).apply()
    }

    fun isEnabled(context: Context, uid: String): Boolean {
        migrateIfNeeded(context, uid)
        return prefs(context).getBoolean(enabledKey(uid), false)
    }

    fun setEnabled(context: Context, uid: String, enabled: Boolean) {
        migrateIfNeeded(context, uid)
        prefs(context).edit().putBoolean(enabledKey(uid), enabled).apply()
        if (enabled) setLastUid(context, uid)
    }

    fun hasPin(context: Context, uid: String): Boolean {
        migrateIfNeeded(context, uid)
        return prefs(context).contains(pinKey(uid))
    }

    fun savePin(context: Context, uid: String, pin: String) {
        migrateIfNeeded(context, uid)
        prefs(context).edit().putString(pinKey(uid), pin).apply()
        setLastUid(context, uid)
    }

    fun verify(context: Context, uid: String, pin: String): Boolean {
        migrateIfNeeded(context, uid)
        return prefs(context).getString(pinKey(uid), null) == pin
    }

    fun hasAnyEnabledPin(context: Context): Boolean {
        val all = prefs(context).all
        return all.any { (k, v) -> k.startsWith("enabled_") && v is Boolean && v }
    }
}
