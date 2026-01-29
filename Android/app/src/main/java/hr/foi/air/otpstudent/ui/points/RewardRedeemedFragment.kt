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
import kotlin.random.Random

class RewardRedeemedFragment : Fragment(R.layout.fragment_reward_redeemed) {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private fun setBottomNavVisible(visible: Boolean) {
        activity?.findViewById<View>(R.id.bottomNavigationView)?.isVisible = visible
    }

    override fun onResume() {
        super.onResume()
        setBottomNavVisible(false)
    }

    override fun onDestroyView() {
        setBottomNavVisible(true)
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rewardId = requireArguments().getString(ARG_REWARD_ID).orEmpty()

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

        btnBack.setOnClickListener {
            val nav = findNavController()

            nav.getBackStackEntry(R.id.nav_points)
                .savedStateHandle["openTab"] = "rewards"

            nav.popBackStack(R.id.nav_points, false)
        }




        val qrPayload = generateOtpToken()
        ivQr.setImageBitmap(QrCodeUtils.generateQrBitmap(qrPayload))

        tvRedeemedMsg.text = getString(R.string.reward_redeemed_message)
        tvStatus.text = getString(R.string.reward_active_chip)

        if (rewardId.isBlank()) return

        db.collection("rewards").document(rewardId).get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener
                if (doc == null || !doc.exists()) return@addOnSuccessListener

                val title = doc.getString("title").orEmpty()
                val img = doc.getString("imageUrl")

                val type = doc.getString("type").orEmpty()
                val value = doc.getLong("value") ?: (doc.getDouble("value")?.toLong() ?: 0L)

                val channel = doc.getString("channel").orEmpty()
                val onlineEnabled = channel.equals("ONLINE", true) || channel.equals("BOTH", true)

                val barcodeFormat = doc.getString("barcodeFormat").orEmpty()

                tvTitle.text = title.ifBlank { getString(R.string.reward_default_store) }

                if (type.equals("PERCENT", ignoreCase = true) && value > 0) {
                    badgeContainer.visibility = View.VISIBLE
                    tvBadgeValue.text = "$value%"
                    tvBadgeLabel.text = getString(R.string.reward_badge_discount)
                } else {
                    badgeContainer.visibility = View.GONE
                }

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

                tvOnlineLabel.isVisible = onlineEnabled
                tvOnlineCode.isVisible = onlineEnabled
                if (onlineEnabled) tvOnlineCode.text = barcodeFormat.ifBlank { "-" }
            }
    }

    private fun generateOtpToken(): String {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val sb = StringBuilder("OTP_")
        repeat(8) { sb.append(alphabet[Random.nextInt(alphabet.length)]) }
        return sb.toString()
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
