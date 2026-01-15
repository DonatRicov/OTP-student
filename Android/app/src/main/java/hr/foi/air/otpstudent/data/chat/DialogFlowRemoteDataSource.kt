package hr.foi.air.otpstudent.data.chat

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class DialogflowRemoteDataSource(
    private val functions: FirebaseFunctions
) {
    suspend fun detectIntent(message: String, sessionId: String): String {
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
