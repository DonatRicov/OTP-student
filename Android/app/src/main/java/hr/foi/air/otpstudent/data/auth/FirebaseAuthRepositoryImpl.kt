package hr.foi.air.otpstudent.data.auth

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hr.foi.air.otpstudent.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import hr.foi.air.otpstudent.domain.model.UserProfile

class FirebaseAuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : AuthRepository {

    override suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    override suspend fun saveUserDocument(uid: String, email: String) {
        val userDoc = mapOf(
            "email" to email,
            "createdAt" to Timestamp.now()
        )
        firestore.collection("users").document(uid).set(userDoc).await()
    }

    override fun currentUserId(): String? = auth.currentUser?.uid

    override fun logout() {
        auth.signOut()
    }

    override suspend fun updateUserFields(uid: String, fields: Map<String, Any?>) {
        firestore.collection("users")
            .document(uid)
            .set(fields, SetOptions.merge())
            .await()
    }

    override suspend fun getUserDocument(uid: String): Map<String, Any?>? {
        val doc = firestore.collection("users").document(uid).get().await()
        return if (doc.exists()) doc.data else null
    }

    override suspend fun uploadAvatar(uid: String, imageUri: Uri): String {
        val ref = storage.reference.child("avatars/$uid/profile.jpg")
        ref.putFile(imageUri).await()
        val url = ref.downloadUrl.await().toString()

        firestore.collection("users").document(uid)
            .set(mapOf("avatarUrl" to url), SetOptions.merge())
            .await()

        return url
    }

    override suspend fun getUserProfile(uid: String): UserProfile {
        val authEmail = auth.currentUser?.email.orEmpty()

        val doc = firestore.collection("users").document(uid).get().await()

        return UserProfile(
            fullName = doc.getString("fullName").orEmpty(),
            email = doc.getString("email") ?: authEmail,
            major = doc.getString("major").orEmpty(),
            location = doc.getString("location").orEmpty(),
            avatarUrl = doc.getString("avatarUrl").orEmpty()
        )
    }
}
