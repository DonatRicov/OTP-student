package hr.foi.air.auth.pin

import android.content.Context

private const val PIN_LENGTH = 6

object PinVerifier {
    fun verify(context: Context, pin: String): Boolean {
        if (!pin.matches(Regex("^\\d{$PIN_LENGTH}$"))) return false
        return PinStore.verify(context, pin)
    }
}
