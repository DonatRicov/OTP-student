package hr.foi.air.auth.pin

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import hr.foi.air.core.auth.AuthPlugin
import hr.foi.air.core.auth.AuthRequest
import hr.foi.air.core.auth.AuthResult
import hr.foi.air.core.auth.AuthUiSpec

class PinAuthPlugin : AuthPlugin {

    private companion object {
        const val PIN_LENGTH = 6
    }

    override fun uiSpec() = AuthUiSpec(
        id = "pin",
        title = "Prijava PIN-om",
        order = 20
    )

    private fun resolvedUid(context: Context): String? =
        FirebaseAuth.getInstance().currentUser?.uid ?: PinStore.getLastUid(context)

    private fun emailPrefix(email: String): String {
        val at = email.indexOf('@')
        return if (at > 0) email.substring(0, at) else email
    }

    private fun formatUserLabel(raw: String?): String? {
        val s = raw?.trim()
        if (s.isNullOrBlank()) return null
        return if (s.contains("@")) emailPrefix(s) else s
    }


     //Prioritet:
     //1. Firebase displayName (ime)
     //2. Firebase email prefix
     //3 PinStore last label

    private fun resolvedUserLabelFormatted(context: Context): String? {
        val u = FirebaseAuth.getInstance().currentUser
        val fromName = formatUserLabel(u?.displayName)
        if (!fromName.isNullOrBlank()) return fromName

        val fromEmail = formatUserLabel(u?.email)
        if (!fromEmail.isNullOrBlank()) return fromEmail

        return formatUserLabel(PinStore.getLastUserLabel(context))
    }

    override fun isEnabled(context: Context): Boolean {
        val uid = resolvedUid(context)
        return if (uid != null) {
            PinStore.isEnabled(context, uid)
        } else {
            PinStore.hasAnyEnabledPin(context)
        }
    }

    override fun setEnabled(context: Context, enabled: Boolean) {
        val uid = resolvedUid(context) ?: return
        PinStore.setEnabled(context, uid, enabled)
        if (enabled) {
            PinStore.setLastUid(context, uid)
            PinStore.setLastUserLabel(context, resolvedUserLabelFormatted(context))
        }
    }

    override fun isConfigured(context: Context): Boolean {
        val uid = resolvedUid(context) ?: return false
        return PinStore.hasPin(context, uid)
    }

    override fun configure(activity: FragmentActivity, callback: (AuthResult) -> Unit) {
        val uid = resolvedUid(activity)
        if (uid == null) {
            callback(AuthResult.Error("Prvo se prijavi e-mailom i lozinkom."))
            return
        }

        PinDialogs.showSetup(activity) { p1, p2 ->
            when {
                p1.length != PIN_LENGTH -> callback(AuthResult.Error("PIN mora imati $PIN_LENGTH znamenki"))
                p1 != p2 -> callback(AuthResult.Error("PIN se ne podudara"))
                else -> {
                    PinStore.savePin(activity, uid, p1)
                    PinStore.setEnabled(activity, uid, true)

                    //zapamti formatirani label
                    PinStore.setLastUid(activity, uid)
                    PinStore.setLastUserLabel(activity, resolvedUserLabelFormatted(activity))

                    callback(AuthResult.Success())
                }
            }
        }
    }

    override fun authenticate(activity: FragmentActivity, request: AuthRequest, callback: (AuthResult) -> Unit) {
        val uid = resolvedUid(activity)
        if (uid == null) {
            callback(AuthResult.Error("PIN prijava nije dostupna."))
            return
        }

        if (!PinStore.hasPin(activity, uid)) {
            callback(AuthResult.Error("PIN nije postavljen."))
            return
        }

        PinDialogs.showVerify(activity) { entered ->
            if (PinStore.verify(activity, uid, entered)) {
                //update last user info na uspjeh
                PinStore.setLastUid(activity, uid)
                PinStore.setLastUserLabel(activity, resolvedUserLabelFormatted(activity))
                callback(AuthResult.Success(uid))
            } else {
                callback(AuthResult.Error("Neispravan PIN"))
            }
        }
    }
}
