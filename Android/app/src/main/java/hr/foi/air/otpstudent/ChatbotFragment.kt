package hr.foi.air.otpstudent

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import hr.foi.air.otpstudent.ui.chat.ChatAdapter
import hr.foi.air.otpstudent.ui.chat.ChatbotViewModel
import hr.foi.air.otpstudent.ui.chat.ChatbotViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class ChatbotFragment : Fragment(R.layout.fragment_chatbot) {


    private val sessionId = UUID.randomUUID().toString()

    private val vm: ChatbotViewModel by viewModels {
        ChatbotViewModelFactory(requireActivity().application)
    }

    private val adapter = ChatAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val header = view.findViewById<View>(R.id.headerChatbot)
        header.findViewById<ImageButton>(R.id.btnHome).setOnClickListener {
            findNavController().popBackStack(R.id.nav_home, false)
        }
        header.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            findNavController().navigateUp()
        }


        val rv = view.findViewById<RecyclerView>(R.id.rvChat)
        rv.adapter = adapter


        val inputRoot = view.findViewById<View>(R.id.chatInput)
        val etMessage = inputRoot.findViewById<EditText>(R.id.etMessage)
        val btnSend = inputRoot.findViewById<ImageButton>(R.id.btnSend)
        val btnAdd = inputRoot.findViewById<ImageButton>(R.id.btnAdd)

        btnSend.setOnClickListener {
            vm.sendMessage(etMessage.text?.toString().orEmpty(), sessionId)
            etMessage.setText("")
        }


        btnAdd.setOnClickListener {
            // funkcionalnost za dodavanje datoteka
        }


        viewLifecycleOwner.lifecycleScope.launch {
            vm.state.collectLatest { state ->
                adapter.submitList(state.messages)
                btnSend.isEnabled = !state.isSending
                if (state.messages.isNotEmpty()) {
                    rv.scrollToPosition(state.messages.size - 1)
                }
            }
        }
    }
}
