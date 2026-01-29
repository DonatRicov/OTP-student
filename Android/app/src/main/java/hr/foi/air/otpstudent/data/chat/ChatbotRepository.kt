package hr.foi.air.otpstudent.data.chat

class ChatbotRepository(
    private val remote: DialogflowRemoteDataSource
) {
    suspend fun send(text: String, sessionId: String): String {
        return remote.detectIntent(text, sessionId)
            .trim()
            .ifEmpty { "Ne znam odgovor na to." }
    }
}
