package hr.foi.air.otpstudent.ui.internship

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
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

    companion object {
        const val ARG_INTERNSHIP_ID = "internshipId"
        private const val FOI_PRAKSA_URL = "https://strucnapraksa.foi.hr/hr/"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val internshipId = arguments?.getString(ARG_INTERNSHIP_ID).orEmpty()
        if (internshipId.isBlank()) {
            Toast.makeText(requireContext(), getString(R.string.error_missing_internship_id), Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
            return
        }

        //header secondary
        val headerContainer = view.findViewById<FrameLayout>(R.id.headerContainer)
        headerContainer.removeAllViews()
        val headerView = layoutInflater.inflate(R.layout.view_header_secondary, headerContainer, false)
        headerContainer.addView(headerView)

        headerView.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            findNavController().navigateUp()
        }
        headerView.findViewById<View>(R.id.btnChatbot)?.setOnClickListener {
            if (findNavController().currentDestination?.id != R.id.chatbotFragment) {
                findNavController().navigate(R.id.chatbotFragment)
            }
        }

        //USER
        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val tvUserRole = view.findViewById<TextView>(R.id.tvUserRole)
        tvUserRole.text = getString(R.string.internship_details_user_role)
        loadUserNameInto(tvUserName)

        //AVATAR iz profila
        val imgAvatar = view.findViewById<ShapeableImageView>(R.id.imgAvatar)
        loadUserAvatarInto(imgAvatar)

        // TITLE + FAVORITE
        val ivFavorite = view.findViewById<ImageView>(R.id.ivFavorite)
        val tvTitle = view.findViewById<TextView>(R.id.tvInternshipTitle)

        // VALUES
        val tvDescriptionValue = view.findViewById<TextView>(R.id.tvDescriptionValue)

        val tvStudyDirection = view.findViewById<TextView>(R.id.tvStudyDirectionValue)
        loadUserMajorInto(tvStudyDirection)

        val tvMentor = view.findViewById<TextView>(R.id.tvMentorValue)
        val tvMentorEmail = view.findViewById<TextView>(R.id.tvMentorEmailValue)
        val tvStartDate = view.findViewById<TextView>(R.id.tvStartDateValue)
        val tvEndDate = view.findViewById<TextView>(R.id.tvEndDateValue)

        // BUTTONS
        val btnApplyOrDetails = view.findViewById<MaterialButton>(R.id.btnApplyOrDetails)
        val btnGoToFoi = view.findViewById<MaterialButton>(R.id.btnToggleFavorite)

        // CV include
        val itemCv = view.findViewById<View>(R.id.itemCv)
        val tvCvFileName = itemCv.findViewById<TextView>(R.id.tvFileName)
        val tvCvUploaderName = itemCv.findViewById<TextView>(R.id.tvUploaderName)
        val btnCvOverflow = itemCv.findViewById<ImageView>(R.id.btnDelete)

        // FAVORITE toggle na zvjezdici i toast (dodaj/ukloni)
        ivFavorite.setOnClickListener {
            val wasFavorite = viewModel.state.value.isFavorite
            viewModel.toggleFavorite()

            Toast.makeText(
                requireContext(),
                if (wasFavorite) "Praksa uklonjena iz favorita" else "Praksa dodana u favorite",
                Toast.LENGTH_SHORT
            ).show()
        }

        //FOI button
        btnGoToFoi.text = getString(R.string.internship_go_to_foi)
        btnGoToFoi.setOnClickListener { openUrl(FOI_PRAKSA_URL) }

        // Apply / Odjavi i toastovi
        btnApplyOrDetails.setOnClickListener {
            val trenutno = btnApplyOrDetails.text?.toString().orEmpty()

            if (trenutno == getString(R.string.internship_details_button_apply)) {
                btnApplyOrDetails.text = "Odjavi praksu"
                Toast.makeText(requireContext(), "Praksa je prijavljena", Toast.LENGTH_SHORT).show()
            } else {
                btnApplyOrDetails.text = getString(R.string.internship_details_button_apply)
                Toast.makeText(requireContext(), "Praksa odjavljena", Toast.LENGTH_SHORT).show()
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

                    tvDescriptionValue.text =
                        internship.description.ifBlank { getString(R.string.placeholder_dash) }

                    tvMentor.text = internship.company.ifBlank { getString(R.string.placeholder_dash) }
                    tvMentorEmail.text = internship.location.ifBlank { getString(R.string.placeholder_dash) }

                    tvStartDate.text = formatDate(internship.postedAt) ?: getString(R.string.placeholder_dash)
                    tvEndDate.text = formatDate(internship.expiresAt) ?: getString(R.string.placeholder_dash)
                }

                updateFavoriteIcon(ivFavorite, s.isFavorite)

                val cv = s.cvDocument
                if (cv == null) {
                    itemCv.visibility = View.GONE
                } else {
                    itemCv.visibility = View.VISIBLE

                    tvCvFileName.text = cv.fileName.ifBlank { getString(R.string.placeholder_dash) }
                    tvCvUploaderName.text = cv.uploaderName.ifBlank { getString(R.string.placeholder_dash) }

                    // ikona download
                    btnCvOverflow.setImageResource(R.drawable.ic_download)

                    itemCv.setOnClickListener { openUrl(cv.fileUrl) }
                    btnCvOverflow.setOnClickListener { openUrl(cv.fileUrl) }
                }
            }
        }

        viewModel.load(internshipId)
    }

    private fun loadUserMajorInto(tv: TextView) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            tv.text = getString(R.string.placeholder_dash)
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                val major = doc.getString("major")?.trim().orEmpty()
                tv.text = if (major.isBlank()) getString(R.string.placeholder_dash) else major
            }
            .addOnFailureListener {
                tv.text = getString(R.string.placeholder_dash)
            }
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
                val fallback = user.email
                    ?.substringBefore("@")
                    ?.replaceFirstChar { it.uppercase() }
                    ?: getString(R.string.placeholder_user_name)

                tv.text = if (!fullName.isNullOrBlank()) fullName else fallback
            }
            .addOnFailureListener {
                val fallback = user.email
                    ?.substringBefore("@")
                    ?.replaceFirstChar { it.uppercase() }
                    ?: getString(R.string.placeholder_user_name)
                tv.text = fallback
            }
    }

    private fun loadUserAvatarInto(img: ShapeableImageView) {
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
        } catch (_: Exception) {
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

    private class VmFactory : ViewModelProvider.Factory {
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
}
