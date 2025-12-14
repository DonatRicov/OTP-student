package hr.foi.air.auth.pin

import android.content.Context
import androidx.startup.Initializer
import hr.foi.air.core.auth.AuthRegistry

class PinAuthInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        AuthRegistry.register(PinAuthPlugin())
    }
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}