package hr.foi.air.otpstudent.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String)
    suspend fun register(email: String, password: String)
    suspend fun saveUserDocument(uid: String, email: String)
    fun currentUserId(): String?
    fun logout()
}
