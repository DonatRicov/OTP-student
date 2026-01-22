package hr.foi.air.otpstudent.ui.points

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.di.AppModule

class PointsFragment : Fragment(R.layout.fragment_points) {

    private lateinit var tabChallenges: TextView
    private lateinit var tabRewards: TextView
    private lateinit var tvPoints: TextView

    private val viewModel: LoyaltyViewModel by viewModels {
        LoyaltyViewModelFactory(AppModule.loyaltyRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabChallenges = view.findViewById(R.id.tabChallenges)
        tabRewards = view.findViewById(R.id.tabRewards)
        tvPoints = view.findViewById(R.id.tvPoints)

        viewModel.points.observe(viewLifecycleOwner) { points ->
            tvPoints.text = getString(R.string.points_total, points)
        }

        viewModel.load()

        if (savedInstanceState == null) {
            showChallenges()
            setSelectedTab(isChallenges = true)
        }

        tabChallenges.setOnClickListener {
            showChallenges()
            setSelectedTab(isChallenges = true)
        }

        tabRewards.setOnClickListener {
            showRewards()
            setSelectedTab(isChallenges = false)
        }

        // Filter button
        val btnFilter: View? = view.findViewById(R.id.btnFilter) ?: runCatching {
            requireActivity().findViewById<View>(R.id.btnFilter)
        }.getOrNull()

        btnFilter?.setOnClickListener {
            val currentChild = childFragmentManager.findFragmentById(R.id.pointsFragmentContainer)
            if (currentChild is RewardsFragment) {
                currentChild.openRewardsFilter()
            } else {
                //nema na izazovima
            }
        }
    }

    private fun showChallenges() {
        childFragmentManager.beginTransaction()
            .replace(R.id.pointsFragmentContainer, ChallengesFragment())
            .commit()
    }

    private fun showRewards() {
        childFragmentManager.beginTransaction()
            .replace(R.id.pointsFragmentContainer, RewardsFragment())
            .commit()
    }

    private fun setSelectedTab(isChallenges: Boolean) {
        tabChallenges.setBackgroundResource(
            if (isChallenges) R.drawable.bg_segment_selected else R.drawable.bg_segment_unselected
        )
        tabRewards.setBackgroundResource(
            if (isChallenges) R.drawable.bg_segment_unselected else R.drawable.bg_segment_selected
        )

        tabChallenges.isClickable = !isChallenges
        tabRewards.isClickable = isChallenges
    }
}
