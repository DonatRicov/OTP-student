package hr.foi.air.auth.pin

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import hr.foi.air.core.auth.*

class PinAuthPlugin : AuthPlugin {

    private companion object {
        const val PIN_LENGTH = 6
    }
    override fun uiSpec() = AuthUiSpec(
        id = "pin",
        title = "Prijava PIN-om",
        order = 20
    )

    override fun isEnabled(context: Context): Boolean =
        PinStore.isEnabled(context)

    override fun setEnabled(context: Context, enabled: Boolean) {
        PinStore.setEnabled(context, enabled)
    }

    override fun isConfigured(context: Context): Boolean =
        PinStore.hasPin(context)

    override fun configure(activity: FragmentActivity, callback: (AuthResult) -> Unit) {
        PinDialogs.showSetup(activity) { p1, p2 ->
            when {
                p1.length != PIN_LENGTH -> callback(AuthResult.Error("PIN mora imati $PIN_LENGTH znamenki"))
                p1 != p2 -> callback(AuthResult.Error("PIN se ne podudara"))
                else -> {
                    PinStore.savePin(activity, p1)
                    callback(AuthResult.Success())
                }
            }
        }
    }

    override fun authenticate(activity: FragmentActivity, request: AuthRequest, callback: (AuthResult) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            callback(AuthResult.Error("Prvo se prijavi e-mailom i lozinkom."))
            return
        }
        if (!PinStore.hasPin(activity)) {
            callback(AuthResult.Error("PIN nije postavljen."))
            return
        }

        PinDialogs.showVerify(activity) { entered ->
            if (PinStore.verify(activity, entered)) callback(AuthResult.Success(user.uid))
            else callback(AuthResult.Error("Neispravan PIN"))
        }
    }
}
