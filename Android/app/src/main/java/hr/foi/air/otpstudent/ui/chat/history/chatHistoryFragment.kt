package hr.foi.air.otpstudent.ui.chat.history

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import hr.foi.air.otpstudent.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatHistoryFragment : Fragment(R.layout.fragment_chat_history) {

    private val vm: ChatHistoryViewModel by viewModels {
        ChatHistoryViewModelFactory(requireActivity().application)
    }

    private val adapter = ChatHistoryAdapter { conv ->
        val args = Bundle().apply { putString("conversationId", conv.id) }
        findNavController().navigate(R.id.chatbotFragment, args)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val header = view.findViewById<View>(R.id.headerHistory)
        header.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            findNavController().navigateUp()
        }
        header.findViewById<ImageButton>(R.id.btnHome).setOnClickListener {
            findNavController().popBackStack(R.id.nav_home, false)
        }

        view.findViewById<RecyclerView>(R.id.rvHistory).adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            vm.state.collectLatest { state ->
                adapter.submitList(state.conversations)
            }
        }

        vm.load()
    }
}
