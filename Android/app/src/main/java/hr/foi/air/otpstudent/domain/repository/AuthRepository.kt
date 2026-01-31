package hr.foi.air.otpstudent.domain.repository

import android.net.Uri
import hr.foi.air.otpstudent.domain.model.UserProfile

interface AuthRepository {
    suspend fun login(email: String, password: String)
    suspend fun register(email: String, password: String)
    suspend fun saveUserDocument(uid: String, email: String)
    fun currentUserId(): String?
    fun logout()

    suspend fun updateUserFields(uid: String, fields: Map<String, Any?>)

    suspend fun getUserDocument(uid: String): Map<String, Any?>?
    suspend fun uploadAvatar(uid: String, imageUri: Uri): String

    suspend fun getUserProfile(uid: String): UserProfile

}
