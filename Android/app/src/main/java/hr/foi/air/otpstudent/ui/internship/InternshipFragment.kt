package hr.foi.air.otpstudent.ui.internship

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import hr.foi.air.otpstudent.R

class InternshipFragment : Fragment(R.layout.fragment_internship) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Dinamicki header (view_header.xml)
        val headerContainer = view.findViewById<FrameLayout>(R.id.headerContainer)
        headerContainer.removeAllViews()
        val headerView = layoutInflater.inflate(R.layout.view_header, headerContainer, false)
        headerContainer.addView(headerView)

        // Chatbot
        headerView.findViewById<View>(R.id.btnChatbot)?.setOnClickListener {
            findNavController().navigate(R.id.chatbotFragment)
        }

        // Kartice
        view.findViewById<View>(R.id.cardMentorship).setOnClickListener {
            findNavController().navigate(R.id.mentorshipDetailsFragment)
        }

        view.findViewById<View>(R.id.cardOtpInternship).setOnClickListener {
            val hasListDestination =
                runCatching { findNavController().graph.findNode(R.id.internshipListFragment) }.getOrNull() != null

            if (hasListDestination) {
                findNavController().navigate(R.id.internshipListFragment)
            } else {
                Toast.makeText(requireContext(), getString(R.string.not_implemented_yet), Toast.LENGTH_SHORT).show()
            }
        }

        // "Moje prijave"
        view.findViewById<View>(R.id.tvMyApplications).setOnClickListener {
            findNavController().navigate(R.id.internshipMyApplicationsFragment)
        }
        view.findViewById<View>(R.id.ivMyApplications).setOnClickListener {
            findNavController().navigate(R.id.internshipMyApplicationsFragment)
        }
    }
}
