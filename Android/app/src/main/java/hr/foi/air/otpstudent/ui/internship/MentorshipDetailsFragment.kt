package hr.foi.air.otpstudent.ui.internship

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hr.foi.air.otpstudent.R
import java.util.Calendar

class MentorshipDetailsFragment : Fragment(R.layout.fragment_mentorship_details) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val header = view.findViewById<View>(R.id.includeHeader)
        header.findViewById<View>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }
        header.findViewById<View>(R.id.btnHome).setOnClickListener {

            findNavController().navigate(R.id.nav_home)
        }


        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val tvUserRole = view.findViewById<TextView>(R.id.tvUserRole)
        tvUserRole.text = getString(R.string.internship_details_user_role)
        loadUserName(tvUserName)


        val etStudy = view.findViewById<TextInputEditText>(R.id.etStudy)
        val acMentor = view.findViewById<MaterialAutoCompleteTextView>(R.id.acMentor)

        val tvStartValue = view.findViewById<TextView>(R.id.tvStartValue)
        val tvEndValue = view.findViewById<TextView>(R.id.tvEndValue)

        tvStartValue.setOnClickListener { showDatePicker { tvStartValue.text = it } }
        tvEndValue.setOnClickListener { showDatePicker { tvEndValue.text = it } }


        acMentor.setSimpleItems(arrayOf("Mentor 1", "Mentor 2", "Mentor 3"))


        view.findViewById<MaterialButton>(R.id.btnSendRequest).setOnClickListener {
            val study = etStudy.text?.toString()?.trim().orEmpty()
            val mentor = acMentor.text?.toString()?.trim().orEmpty()

            if (study.isBlank() || mentor.isBlank()) {
                Toast.makeText(requireContext(), getString(R.string.mentorship_fill_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(requireContext(), getString(R.string.mentorship_request_sent), Toast.LENGTH_SHORT).show()


            findNavController().popBackStack()
        }
    }

    private fun showDatePicker(onPicked: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                val day = d.toString().padStart(2, '0')
                val month = (m + 1).toString().padStart(2, '0')
                onPicked("$day.$month.$y.")
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadUserName(tv: TextView) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            tv.text = getString(R.string.placeholder_user_name)
            return
        }

        val uid = user.uid
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val fullName = doc.getString("fullName")
                val fallback = user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                    ?: getString(R.string.placeholder_user_name)
                tv.text = if (!fullName.isNullOrBlank()) fullName else fallback
            }
            .addOnFailureListener {
                val fallback = user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                    ?: getString(R.string.placeholder_user_name)
                tv.text = fallback
            }
    }
}
