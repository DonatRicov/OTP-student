package hr.foi.air.otpstudent.data.auth

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import hr.foi.air.auth.bio.BioStore
import hr.foi.air.auth.pin.PinStore
import hr.foi.air.core.auth.SecureCreds

object QuickLoginManager {

    private const val PREFS = "quick_login_manager"
    private const val KEY_LAST_UID = "last_uid"

    fun enforceUserScope(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        val lastUid = prefs.getString(KEY_LAST_UID, null)

        if (lastUid != null && currentUid != null && lastUid != currentUid) {
            resetQuickLogin(context)
        }

        prefs.edit().putString(KEY_LAST_UID, currentUid).apply()
    }

    fun resetQuickLogin(context: Context) {
        SecureCreds.clear(context)

        val lastKnownUid =
            FirebaseAuth.getInstance().currentUser?.uid ?: PinStore.getLastUid(context)

        PinStore.clearLastUser(context)

        if (lastKnownUid != null) {
            PinStore.resetForUid(context, lastKnownUid)
        }

        BioStore.setEnabled(context, false)

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_LAST_UID)
            .apply()
    }

}