package hr.foi.air.otpstudent.ui.points

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import hr.foi.air.otpstudent.R
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

class RedeemedRewardDetailsFragment : Fragment(R.layout.fragment_reward_redeemed) {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private fun setBottomNavVisible(visible: Boolean) {
        activity?.findViewById<View>(R.id.bottomNavigationView)?.isVisible = visible
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rewardId = requireArguments().getString(ARG_REWARD_ID).orEmpty()
        val redemptionId = requireArguments().getString(ARG_REDEMPTION_ID).orEmpty()

        // Views
        val btnBack: ImageButton = view.findViewById(R.id.btnBack)
        val ivReward: ImageView = view.findViewById(R.id.ivRewardImage)

        val badgeContainer: View = view.findViewById(R.id.tvImageBadge)
        val tvBadgeValue: TextView = view.findViewById(R.id.tvBadgeValue)
        val tvBadgeLabel: TextView = view.findViewById(R.id.tvBadgeLabel)

        val tvTitle: TextView = view.findViewById(R.id.tvStoreTitle)
        val tvStatus: TextView = view.findViewById(R.id.tvStatusChip)
        val tvRedeemedMsg: TextView = view.findViewById(R.id.tvRedeemedMsg)

        val ivQr: ImageView = view.findViewById(R.id.ivQr)
        val tvOnlineLabel: TextView = view.findViewById(R.id.tvOnlineLabel)
        val tvOnlineCode: TextView = view.findViewById(R.id.tvOnlineCode)

        val btnCopy: ImageButton = view.findViewById(R.id.btnCopy)

        val tvLocation: TextView = view.findViewById(R.id.tvLocation)

        btnCopy.setOnClickListener {
            val code = tvOnlineCode.text?.toString()?.trim().orEmpty()
            if (code.isBlank()) return@setOnClickListener

            val clipboard = requireContext()
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            clipboard.setPrimaryClip(ClipData.newPlainText("Online code", code))

            Toast.makeText(requireContext(), "Kod kopiran", Toast.LENGTH_SHORT).show()
        }

        val cardOnline: View = view.findViewById(R.id.cardOnline)

        cardOnline.isVisible = false
        tvOnlineLabel.isVisible = false
        tvOnlineCode.isVisible = false


        btnBack.setOnClickListener { findNavController().navigateUp() }

        // Statusvi message
        tvRedeemedMsg.text = getString(R.string.reward_redeemed_message)
        tvStatus.text = getString(R.string.reward_active_chip)

        if (redemptionId.isNotBlank()) {
            val qrPayload = "OTP_$redemptionId"
            ivQr.setImageBitmap(QrCodeUtils.generateQrBitmap(qrPayload))
        }

        // Online kod kao redemptionId

        if (rewardId.isBlank()) return

        // Učitaj reward iz baze
        db.collection("rewards").document(rewardId).get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener
                if (doc == null || !doc.exists()) return@addOnSuccessListener

                val title = doc.getString("title").orEmpty()
                val img = doc.getString("imageUrl")

                val type = doc.getString("type").orEmpty()
                val value = doc.getLong("value") ?: (doc.getDouble("value")?.toLong() ?: 0L)

                val channel = doc.getString("channel")?.trim()?.uppercase() ?: ""
                val onlineEnabled = channel == "ONLINE" || channel == "BOTH"


                cardOnline.isVisible = onlineEnabled
                btnCopy.isVisible = onlineEnabled
                tvOnlineLabel.isVisible = onlineEnabled
                tvOnlineCode.isVisible = onlineEnabled
                tvOnlineCode.text = if (onlineEnabled) redemptionId else ""

                tvTitle.text = title.ifBlank { getString(R.string.reward_default_store) }

                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnSuccessListener

                val validDays = doc.getLong("validDays") ?: 0L

                db.collection("users")
                    .document(uid)
                    .collection("redeemedRewards")
                    .whereEqualTo("redemptionId", redemptionId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { qs ->
                        val redeemedDoc = qs.documents.firstOrNull()
                        val redeemedAt = redeemedDoc?.getTimestamp("redeemedAt")?.toDate()

                        if (redeemedAt != null && validDays > 0) {
                            val cal = java.util.Calendar.getInstance().apply { time = redeemedAt }
                            cal.add(java.util.Calendar.DAY_OF_YEAR, validDays.toInt())

                            val expiryDate = cal.time
                            val fmt = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale("hr"))

                            tvLocation.text = "Istječe: ${fmt.format(expiryDate)}"
                        } else {
                            tvLocation.text = ""
                        }
                    }

                // badge popust
                if (type.equals("PERCENT", ignoreCase = true) && value > 0) {
                    badgeContainer.visibility = View.VISIBLE
                    tvBadgeValue.text = "$value%"
                    tvBadgeLabel.text = getString(R.string.reward_badge_discount)
                } else {
                    badgeContainer.visibility = View.GONE
                }

                // slika rewarda
                if (!img.isNullOrBlank() && img.startsWith("gs://")) {
                    val ref = FirebaseStorage.getInstance().getReferenceFromUrl(img)
                    ivReward.setImageResource(R.drawable.placeholder_reward)

                    ref.downloadUrl
                        .addOnSuccessListener { uri ->
                            if (!isAdded) return@addOnSuccessListener
                            Glide.with(this)
                                .load(uri)
                                .placeholder(R.drawable.placeholder_reward)
                                .error(R.drawable.placeholder_reward)
                                .into(ivReward)
                        }
                        .addOnFailureListener {
                            ivReward.setImageResource(R.drawable.placeholder_reward)
                        }
                } else {
                    Glide.with(this)
                        .load(img)
                        .placeholder(R.drawable.placeholder_reward)
                        .error(R.drawable.placeholder_reward)
                        .into(ivReward)
                }
            }
    }

    companion object {
        private const val ARG_REWARD_ID = "rewardId"
        private const val ARG_REDEMPTION_ID = "redemptionId"

        fun createArgs(rewardId: String, redemptionId: String) = bundleOf(
            ARG_REWARD_ID to rewardId,
            ARG_REDEMPTION_ID to redemptionId
        )
    }
}
