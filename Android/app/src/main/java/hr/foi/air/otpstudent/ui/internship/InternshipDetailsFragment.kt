package hr.foi.air.otpstudent.ui.internship

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.data.repository.FirebaseCvRepositoryImpl
import hr.foi.air.otpstudent.di.AppModule
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class InternshipDetailsFragment : Fragment(R.layout.fragment_internship_details) {

    private val viewModel: InternshipDetailsViewModel by lazy {
        ViewModelProvider(this, VmFactory())[InternshipDetailsViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val internshipId = arguments?.getString(ARG_INTERNSHIP_ID).orEmpty()
        if (internshipId.isBlank()) {
            Toast.makeText(requireContext(), getString(R.string.error_missing_internship_id), Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
            return
        }


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
        loadUserNameInto(tvUserName)


        val ivFavorite = view.findViewById<ImageView>(R.id.ivFavorite)
        val tvTitle = view.findViewById<TextView>(R.id.tvInternshipTitle)

        val tvStudyDirection = view.findViewById<TextView>(R.id.tvStudyDirectionValue)
        val tvMentor = view.findViewById<TextView>(R.id.tvMentorValue)
        val tvMentorEmail = view.findViewById<TextView>(R.id.tvMentorEmailValue)
        val tvStartDate = view.findViewById<TextView>(R.id.tvStartDateValue)
        val tvEndDate = view.findViewById<TextView>(R.id.tvEndDateValue)

        val btnApplyOrDetails = view.findViewById<MaterialButton>(R.id.btnApplyOrDetails)
        val btnToggleFavorite = view.findViewById<MaterialButton>(R.id.btnToggleFavorite)


        val cvCard = view.findViewById<View>(R.id.cvCard)
        val tvCvFileName = view.findViewById<TextView>(R.id.tvCvFileName)
        val tvCvUploaderName = view.findViewById<TextView>(R.id.tvCvUploaderName)
        val btnCvOverflow = view.findViewById<ImageView>(R.id.btnCvOverflow)

        ivFavorite.setOnClickListener { viewModel.toggleFavorite() }
        btnToggleFavorite.setOnClickListener { viewModel.toggleFavorite() }


        btnApplyOrDetails.setOnClickListener {
            if (viewModel.state.value.isApplied) {
                openUrl(PRAKSA_DETAILS_URL)
            } else {
                viewModel.onApplyOrDetailsClicked { url ->
                    openUrl(url)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { s ->

                s.error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }

                val internship = s.internship
                if (internship != null) {
                    tvTitle.text = internship.title.ifBlank { getString(R.string.placeholder_dash) }

                    tvStudyDirection.text = internship.description.ifBlank { getString(R.string.placeholder_dash) }
                    tvMentor.text = internship.company.ifBlank { getString(R.string.placeholder_dash) }
                    tvMentorEmail.text = internship.location.ifBlank { getString(R.string.placeholder_dash) }

                    tvStartDate.text = formatDate(internship.postedAt) ?: getString(R.string.placeholder_dash)
                    tvEndDate.text = formatDate(internship.expiresAt) ?: getString(R.string.placeholder_dash)
                }

                btnApplyOrDetails.text =
                    if (s.isApplied) getString(R.string.internship_details_button_details)
                    else getString(R.string.internship_details_button_apply)

                updateFavoriteIcon(ivFavorite, s.isFavorite)

                btnToggleFavorite.text =
                    if (s.isFavorite) getString(R.string.internship_remove_favorite)
                    else getString(R.string.internship_add_favorite)

                val cv = s.cvDocument
                if (cv == null) {
                    cvCard.visibility = View.GONE
                } else {
                    cvCard.visibility = View.VISIBLE
                    tvCvFileName.text = cv.fileName.ifBlank { getString(R.string.placeholder_dash) }
                    tvCvUploaderName.text = cv.uploaderName.ifBlank { getString(R.string.cv_uploader_default) }

                    cvCard.setOnClickListener { openUrl(cv.fileUrl) }
                    btnCvOverflow.setOnClickListener { openUrl(cv.fileUrl) }
                }
            }
        }

        viewModel.load(internshipId)
    }

    private fun loadUserNameInto(tv: TextView) {
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
                val nameFromEmail = user.email
                    ?.substringBefore("@")
                    ?.replaceFirstChar { it.uppercase() }
                    ?: getString(R.string.placeholder_user_name)

                tv.text = if (!fullName.isNullOrBlank()) fullName else nameFromEmail
            }
            .addOnFailureListener {
                val nameFromEmail = user.email
                    ?.substringBefore("@")
                    ?.replaceFirstChar { it.uppercase() }
                    ?: getString(R.string.placeholder_user_name)
                tv.text = nameFromEmail
            }
    }

    private fun formatDate(ts: Timestamp?): String? {
        ts ?: return null
        val sdf = SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault())
        return sdf.format(ts.toDate())
    }

    private fun openUrl(rawUrl: String) {
        val url = rawUrl.trim()
        if (url.isBlank()) {
            Toast.makeText(requireContext(), getString(R.string.error_link_not_available), Toast.LENGTH_SHORT).show()
            return
        }

        val finalUrl =
            if (url.startsWith("http://") || url.startsWith("https://")) url
            else "https://$url"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), getString(R.string.error_cannot_open_link), Toast.LENGTH_LONG).show()
        }
    }

    private fun updateFavoriteIcon(iv: ImageView, isFavorite: Boolean) {
        iv.setImageResource(
            if (isFavorite) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
        iv.setColorFilter(
            requireContext().getColor(
                if (isFavorite) R.color.otp_green_dark else R.color.otp_grey
            )
        )
    }

    private inner class VmFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InternshipDetailsViewModel::class.java)) {
                val internshipRepo = AppModule.internshipRepository
                val uidProvider = { FirebaseAuth.getInstance().currentUser?.uid }
                val cvRepoProvider = { uid: String -> FirebaseCvRepositoryImpl(uid) }

                @Suppress("UNCHECKED_CAST")
                return InternshipDetailsViewModel(internshipRepo, cvRepoProvider, uidProvider) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        const val ARG_INTERNSHIP_ID = "internshipId"


        private const val PRAKSA_DETAILS_URL = "https://strucnapraksa.foi.hr/hr/"
    }
}
