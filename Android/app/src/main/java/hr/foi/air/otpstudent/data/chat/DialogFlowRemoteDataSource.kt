package hr.foi.air.otpstudent.data.chat

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class DialogflowRemoteDataSource(
    private val functions: FirebaseFunctions
) {
    suspend fun detectIntent(message: String, sessionId: String): String {

        val user = FirebaseAuth.getInstance().currentUser
            ?: throw IllegalStateException("User is not logged in (FirebaseAuth.currentUser is null).")


        user.getIdToken(false).await()


        val payload = hashMapOf(
            "message" to message,
            "sessionId" to sessionId
        )

        
        val res = functions
            .getHttpsCallable("dialogflowDetectIntent")
            .call(payload)
            .await()

        val data = res.data as? Map<*, *> ?: emptyMap<String, Any>()
        return (data["reply"] as? String).orEmpty()
    }
}
