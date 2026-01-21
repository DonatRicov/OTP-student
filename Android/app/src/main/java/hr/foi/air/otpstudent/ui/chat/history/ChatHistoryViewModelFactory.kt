package hr.foi.air.otpstudent.ui.chat.history

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import hr.foi.air.otpstudent.ui.chat.ChatConversationsStore

class ChatHistoryViewModelFactory(
    private val app: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("Nema ulogiranog korisnika.")

        val historyKey = "chat_conversations_$uid"
        val store = ChatConversationsStore(app.applicationContext)

        @Suppress("UNCHECKED_CAST")
        return ChatHistoryViewModel(store, historyKey) as T
    }
}
