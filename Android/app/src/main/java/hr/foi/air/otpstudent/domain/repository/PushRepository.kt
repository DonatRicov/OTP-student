package hr.foi.air.otpstudent.domain.repository

interface PushRepository {
    suspend fun setEnabled(enabled: Boolean)
    suspend fun isEnabled(): Boolean
}
