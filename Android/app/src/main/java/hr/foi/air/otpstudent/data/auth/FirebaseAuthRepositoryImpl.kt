package hr.foi.air.otpstudent.data.auth

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hr.foi.air.otpstudent.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.SetOptions

class FirebaseAuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
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

}
