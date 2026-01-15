package hr.foi.air.otpstudent.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.functions.FirebaseFunctions
import hr.foi.air.otpstudent.data.chat.ChatbotRepository
import hr.foi.air.otpstudent.data.chat.DialogflowRemoteDataSource

class ChatbotViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val functions = FirebaseFunctions.getInstance("us-central1")
        val remote = DialogflowRemoteDataSource(functions)
        val repo = ChatbotRepository(remote)

        @Suppress("UNCHECKED_CAST")
        return ChatbotViewModel(repo) as T
    }
}
