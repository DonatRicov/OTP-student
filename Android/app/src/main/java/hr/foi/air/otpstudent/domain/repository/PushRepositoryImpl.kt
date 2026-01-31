package hr.foi.air.otpstudent.data.repository

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import hr.foi.air.otpstudent.domain.repository.PushRepository
import kotlinx.coroutines.tasks.await

class PushRepositoryImpl(
    private val context: Context
) : PushRepository {

    private val prefs = context.getSharedPreferences("push_prefs", Context.MODE_PRIVATE)

    override suspend fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("enabled", enabled).apply()

        val fm = FirebaseMessaging.getInstance()
        if (enabled) {
            fm.subscribeToTopic("jobs").await()
            fm.subscribeToTopic("internships").await()
        } else {
            fm.unsubscribeFromTopic("jobs").await()
            fm.unsubscribeFromTopic("internships").await()
        }
    }

    override suspend fun isEnabled(): Boolean {
        return prefs.getBoolean("enabled", false)
    }
}
