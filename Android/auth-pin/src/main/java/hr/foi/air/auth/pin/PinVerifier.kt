package hr.foi.air.auth.pin

import android.content.Context

private const val PIN_LENGTH = 6

object PinVerifier {
    //Validira format PINa i provjerava za odredjenog korisnika
    fun verify(context: Context, uid: String, pin: String): Boolean {
        if (!pin.matches(Regex("^\\d{$PIN_LENGTH}$"))) return false
        return PinStore.verify(context, uid, pin)
    }
}
