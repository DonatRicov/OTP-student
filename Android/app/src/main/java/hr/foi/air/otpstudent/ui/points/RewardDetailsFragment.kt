package hr.foi.air.otpstudent.ui.points

import android.os.Bundle
import android.view.View
//import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import hr.foi.air.otpstudent.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RewardDetailsFragment : Fragment(R.layout.fragment_reward_details) {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // hide/show bottom nav
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
        val userPoints = requireArguments().getLong(ARG_USER_POINTS, 0L)

        // Views
        val btnBack: View = view.findViewById(R.id.btnBack)
        //val ivReward: ImageView = view.findViewById(R.id.ivRewardImage)

        // Badge views
        val badgeContainer: View = view.findViewById(R.id.tvImageBadge)
        val tvBadgeValue: TextView = view.findViewById(R.id.tvBadgeValue)
        val tvBadgeLabel: TextView = view.findViewById(R.id.tvBadgeLabel)

        val tvStore: TextView = view.findViewById(R.id.tvStoreTitle)

        val tvChipType: TextView = view.findViewById(R.id.tvChipType)
        val tvChipValid: TextView = view.findViewById(R.id.tvChipValid)
        val tvChipUses: TextView = view.findViewById(R.id.tvChipUses)

        val tvCost: TextView = view.findViewById(R.id.tvCostPoints)
        val tvBalanceRight: TextView = view.findViewById(R.id.tvBalanceRight)
        val progress: ProgressBar = view.findViewById(R.id.progressPoints)
        val tvWarn: TextView = view.findViewById(R.id.tvPointsWarning)

        val tvDetailsBody: TextView = view.findViewById(R.id.tvDetailsBody)
        val tvInstructionsBody: TextView = view.findViewById(R.id.tvInstructionsBody)

        val btnRedeem: View = view.findViewById(R.id.btnRedeem)
        val tvRedeemText: TextView = view.findViewById(R.id.tvRedeemText)
        val tvRedeemCost: TextView = view.findViewById(R.id.tvRedeemCostBadge)

        btnBack.setOnClickListener { findNavController().navigateUp() }
        btnRedeem.setOnClickListener { /* TODO: redeem later */ }


        if (rewardId.isBlank()) return

        db.collection("rewards").document(rewardId).get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener
                if (doc == null || !doc.exists()) return@addOnSuccessListener

                val title = doc.getString("title").orEmpty()
                val costPoints = doc.getLong("costPoints") ?: 0L

                val type = doc.getString("type").orEmpty()
                val value = doc.getLong("value") ?: (doc.getDouble("value")?.toLong() ?: 0L)
                val rewardType = doc.getString("rewardType").orEmpty()

                val numberOfUses = doc.getString("numberOfUses").orEmpty()
                val validDays = doc.getLong("validDays")
                    ?: (doc.getDouble("validDays")?.toLong())
                    ?: 7L

                val validFromTs = doc.getTimestamp("validFrom")
                val (_, validToText) = formatValidFromTo(validFromTs, validDays)

                val detailsList =
                    (doc.get("details") as? List<*>)?.mapNotNull { it as? String }.orEmpty()
                val instructionsList =
                    (doc.get("instructions") as? List<*>)?.mapNotNull { it as? String }.orEmpty()

                // Header texts
                tvStore.text = title.takeIf { it.isNotBlank() }
                    ?: getString(R.string.reward_default_store)

                // Badge value iz baze
                if (type.equals("PERCENT", ignoreCase = true) && value > 0) {
                    badgeContainer.visibility = View.VISIBLE
                    tvBadgeValue.text = "$value%"
                    tvBadgeLabel.text = getString(R.string.reward_badge_discount)
                } else {
                    badgeContainer.visibility = View.GONE
                }

                // Chips
                tvChipType.text = rewardType.ifBlank { getString(R.string.reward_chip_default_type) }
                tvChipValid.text = getString(R.string.reward_chip_valid_to, validToText)
                tvChipUses.text = numberOfUses.ifBlank { getString(R.string.reward_chip_default_uses) }

                // Points panel
                tvCost.text = getString(R.string.reward_cost_points_value, costPoints)
                tvBalanceRight.text = getString(R.string.reward_points_ratio, userPoints, costPoints)

                progress.max = costPoints.toInt().coerceAtLeast(1)
                progress.progress = userPoints.coerceAtMost(costPoints).toInt()

                val missing = (costPoints - userPoints).coerceAtLeast(0L)
                val canRedeem = userPoints >= costPoints

                tvWarn.visibility = if (canRedeem) View.GONE else View.VISIBLE
                if (!canRedeem) {
                    tvWarn.text = getString(R.string.reward_points_missing, missing)
                }

                // Details i Instructions
                tvDetailsBody.text = if (detailsList.isNotEmpty()) {
                    detailsList.joinToString(separator = "\n") { "• $it" }
                } else {
                    getString(R.string.reward_details_empty)
                }

                tvInstructionsBody.text = if (instructionsList.isNotEmpty()) {
                    instructionsList.mapIndexed { idx, line -> "${idx + 1}. $line" }
                        .joinToString(separator = "\n")
                } else {
                    getString(R.string.reward_instructions_empty)
                }

                tvRedeemText.text = getString(R.string.reward_redeem_button)
                tvRedeemCost.text = costPoints.toString()

                btnRedeem.isEnabled = canRedeem
                btnRedeem.alpha = if (canRedeem) 1f else 0.55f
            }
            .addOnFailureListener {
                // opcionalno
            }
    }

    private fun formatValidFromTo(validFrom: Timestamp?, validDays: Long): Pair<String, String> {
        val locale = Locale("hr", "HR")
        val df = SimpleDateFormat("d.M.yyyy", locale)

        if (validFrom == null) {
            val fromFallback = "-"
            val toFallback = getString(R.string.reward_valid_days_fallback, validDays)
            return fromFallback to toFallback
        }

        val fromDate = validFrom.toDate()
        val cal = Calendar.getInstance(locale).apply { time = fromDate }
        cal.add(Calendar.DAY_OF_YEAR, validDays.toInt())

        val fromText = df.format(fromDate)
        val toText = df.format(cal.time)
        return fromText to toText
    }

    companion object {
        private const val ARG_REWARD_ID = "rewardId"
        private const val ARG_USER_POINTS = "userPoints"

        fun createArgs(rewardId: String, userPoints: Long) = bundleOf(
            ARG_REWARD_ID to rewardId,
            ARG_USER_POINTS to userPoints
        )
    }
}
