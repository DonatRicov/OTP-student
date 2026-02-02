package hr.foi.air.otpstudent.ui.internship

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hr.foi.air.otpstudent.R
import java.util.Calendar

class MentorshipDetailsFragment : Fragment(R.layout.fragment_mentorship_details) {

    private var userMajor: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // header
        val headerContainer = view.findViewById<FrameLayout>(R.id.headerContainer)
        headerContainer.removeAllViews()
        val headerView = layoutInflater.inflate(R.layout.view_header_secondary, headerContainer, false)
        headerContainer.addView(headerView)

        // Back
        headerView.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Chatbot
        headerView.findViewById<View>(R.id.btnChatbot)?.setOnClickListener {
            if (findNavController().currentDestination?.id != R.id.chatbotFragment) {
                findNavController().navigate(R.id.chatbotFragment)
            }
        }

        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val tvUserRole = view.findViewById<TextView>(R.id.tvUserRole)
        tvUserRole.text = getString(R.string.internship_details_user_role)

        val imgAvatar = view.findViewById<ShapeableImageView>(R.id.imgAvatar)

        loadUserName(tvUserName)
        loadUserAvatar(imgAvatar)

        val etStudy = view.findViewById<TextInputEditText>(R.id.etStudy)
        val acMentor = view.findViewById<MaterialAutoCompleteTextView>(R.id.acMentor)

        fetchAndApplyMajor(etStudy)

        val tvStartValue = view.findViewById<TextView>(R.id.tvStartValue)
        val tvEndValue = view.findViewById<TextView>(R.id.tvEndValue)

        tvStartValue.setOnClickListener { showDatePicker { tvStartValue.text = it } }
        tvEndValue.setOnClickListener { showDatePicker { tvEndValue.text = it } }

        acMentor.setSimpleItems(arrayOf("Mentor 1", "Mentor 2", "Mentor 3"))

        view.findViewById<MaterialButton>(R.id.btnSendRequest).setOnClickListener {
            if (userMajor.isNullOrBlank()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.profile_finish_setup_toast),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val study = userMajor?.trim().orEmpty()
            val mentor = acMentor.text?.toString()?.trim().orEmpty()

            if (study.isBlank() || mentor.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.mentorship_fill_required),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            Toast.makeText(
                requireContext(),
                getString(R.string.mentorship_request_sent),
                Toast.LENGTH_SHORT
            ).show()

            findNavController().navigateUp()
        }
    }

    private fun fetchAndApplyMajor(etStudy: TextInputEditText) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        fun lockEditing() {
            etStudy.apply {
                inputType = InputType.TYPE_NULL
                keyListener = null
                isCursorVisible = false
                isFocusable = false
                isFocusableInTouchMode = false
            }
        }

        fun setToastOnClickIfMissing() {
            etStudy.apply {
                isEnabled = true
                isClickable = true
                setOnClickListener {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.profile_finish_setup_toast),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        lockEditing()

        if (uid == null) {
            userMajor = null
            etStudy.setText("")
            setToastOnClickIfMissing()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                userMajor = doc.getString("major")?.trim()

                if (!userMajor.isNullOrBlank()) {
                    etStudy.setText(userMajor)
                    etStudy.isEnabled = false
                    etStudy.isClickable = false
                    etStudy.setOnClickListener(null)
                } else {
                    etStudy.setText("")
                    setToastOnClickIfMissing()
                }
            }
            .addOnFailureListener {
                userMajor = null
                etStudy.setText("")
                setToastOnClickIfMissing()
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

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                val fullName = doc.getString("fullName")?.trim().orEmpty()
                val email = user.email?.trim().orEmpty()

                tv.text = when {
                    fullName.isNotBlank() -> fullName
                    email.isNotBlank() -> email
                    else -> getString(R.string.placeholder_user_name)
                }
            }
            .addOnFailureListener {
                val email = user.email?.trim().orEmpty()
                tv.text = if (email.isNotBlank()) email else getString(R.string.placeholder_user_name)
            }
    }

    private fun loadUserAvatar(img: ShapeableImageView) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            img.setImageResource(R.drawable.ic_profile_placeholder)
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                val avatarUrl = doc.getString("avatarUrl")?.trim().orEmpty()

                if (avatarUrl.isBlank()) {
                    img.setImageResource(R.drawable.ic_profile_placeholder)
                    return@addOnSuccessListener
                }

                Glide.with(this)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(img)
            }
            .addOnFailureListener {
                img.setImageResource(R.drawable.ic_profile_placeholder)
            }
    }
}
