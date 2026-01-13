package hr.foi.air.otpstudent

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class ChatbotFragment : Fragment(R.layout.fragment_chatbot) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val header = view.findViewById<View>(R.id.headerChatbot)

        header.findViewById<ImageButton>(R.id.btnHome).setOnClickListener {
            findNavController().popBackStack(R.id.nav_home, false)
        }

        header.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            findNavController().navigateUp()
        }
    }
}
