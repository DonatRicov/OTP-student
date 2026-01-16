package hr.foi.air.otpstudent.data.chat

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class DialogflowRemoteDataSource(
    private val functions: FirebaseFunctions
) {
    suspend fun detectIntent(message: String, sessionId: String): String {
        // 1) Osiguraj da je user stvarno logiran i da ima token
        val user = FirebaseAuth.getInstance().currentUser
            ?: throw IllegalStateException("User is not logged in (FirebaseAuth.currentUser is null).")


        user.getIdToken(false).await()

        // 2) Payload (kao i prije)
        val payload = hashMapOf(
            "message" to message,
            "sessionId" to sessionId
        )

        // 3) Poziv funkcije
        val res = functions
            .getHttpsCallable("dialogflowDetectIntent")
            .call(payload)
            .await()

        val data = res.data as? Map<*, *> ?: emptyMap<String, Any>()
        return (data["reply"] as? String).orEmpty()
    }
}
