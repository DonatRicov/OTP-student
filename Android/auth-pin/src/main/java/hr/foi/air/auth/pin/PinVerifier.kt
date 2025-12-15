package hr.foi.air.auth.pin

import android.content.Context

object PinVerifier {
    fun verify(context: Context, pin: String): Boolean {
        if (pin.length != 4) return false
        return PinStore.verify(context, pin)
    }
}
