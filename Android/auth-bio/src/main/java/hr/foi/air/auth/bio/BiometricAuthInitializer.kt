package hr.foi.air.auth.bio

import android.content.Context
import androidx.startup.Initializer
import hr.foi.air.core.auth.AuthRegistry

class BiometricAuthInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        AuthRegistry.register(BiometricAuthPlugin())
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}
