package hr.foi.air.auth.bio

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import hr.foi.air.core.auth.*

class BiometricAuthPlugin : AuthPlugin {

    override fun uiSpec() = AuthUiSpec(
        id = "bio",
        title = "Prijava biometrijom",
        order = 10
    )

    override fun isEnabled(context: Context): Boolean =
        BioStore.isEnabled(context)

    override fun setEnabled(context: Context, enabled: Boolean) {
        BioStore.setEnabled(context, enabled)
    }

    override fun isConfigured(context: Context): Boolean {
        val mgr = BiometricManager.from(context)
        return mgr.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    override fun configure(activity: FragmentActivity, callback: (AuthResult) -> Unit) {
        authenticateInternal(activity) { callback(it) }
    }

    override fun authenticate(activity: FragmentActivity, request: AuthRequest, callback: (AuthResult) -> Unit) {
        authenticateInternal(activity) { callback(it) }
    }

    private fun authenticateInternal(
        activity: FragmentActivity,
        onResult: (AuthResult) -> Unit
    )
    {
        val executor = ContextCompat.getMainExecutor(activity)

        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onResult(AuthResult.Success())
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_CANCELED
                    ) {
                        onResult(AuthResult.Cancelled)
                    } else {
                        onResult(AuthResult.Error(errString.toString()))
                    }
                }

                override fun onAuthenticationFailed() {
                    onResult(AuthResult.Error("Biometrijska provjera nije uspjela"))
                }

            }
        )

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Prijava biometrijom")
            .setSubtitle("Potvrdi identitet")
            .setNegativeButtonText("Odustani")
            .build()

        prompt.authenticate(info)
    }
}
