package hr.foi.air.core.auth

import android.content.Context
import androidx.fragment.app.FragmentActivity

interface AuthPlugin {

    fun uiSpec(): AuthUiSpec

    fun authenticate(
        activity: FragmentActivity,
        request: AuthRequest,
        callback: (AuthResult) -> Unit
    )

    fun isEnabled(context: Context): Boolean = true
    fun setEnabled(context: Context, enabled: Boolean) {}

    fun isConfigured(context: Context): Boolean = true

    fun configure(activity: FragmentActivity, callback: (AuthResult) -> Unit) {
        callback(AuthResult.Success())
    }
}
