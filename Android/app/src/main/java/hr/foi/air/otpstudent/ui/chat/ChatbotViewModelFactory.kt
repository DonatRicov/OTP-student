package hr.foi.air.otpstudent.ui.chat

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import hr.foi.air.otpstudent.data.chat.ChatbotRepository
import hr.foi.air.otpstudent.data.chat.DialogflowRemoteDataSource

class ChatbotViewModelFactory(
    private val app: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val functions = FirebaseFunctions.getInstance("us-central1")
        val remote = DialogflowRemoteDataSource(functions)
        val repo = ChatbotRepository(remote)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("Nema ulogiranog korisnika (uid je null).")

        val historyKey = "chat_conversations_$uid"
        val store = ChatConversationsStore(app.applicationContext)

        @Suppress("UNCHECKED_CAST")
        return ChatbotViewModel(repo, store, historyKey) as T
    }
}
