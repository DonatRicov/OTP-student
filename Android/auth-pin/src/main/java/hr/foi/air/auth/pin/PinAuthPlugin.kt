package hr.foi.air.auth.pin

import androidx.fragment.app.FragmentActivity
import hr.foi.air.core.auth.AuthPlugin
import hr.foi.air.core.auth.AuthRequest
import hr.foi.air.core.auth.AuthResult
import hr.foi.air.core.auth.AuthUiSpec

class PinAuthPlugin : AuthPlugin {

    override fun uiSpec() = AuthUiSpec(
        id = "pin",
        title = "Prijava PIN-om",
        order = 20
    )

    override fun authenticate(
        activity: FragmentActivity,
        request: AuthRequest,
        callback: (AuthResult) -> Unit
    ) {
        callback(AuthResult.Error("PIN modul radi, ali još nije implementiran UI."))
    }
}