package hr.foi.air.otpstudent.ui.points

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import hr.foi.air.otpstudent.R

class PointsFragment : Fragment(R.layout.fragment_points) {

    private lateinit var tabChallenges: TextView
    private lateinit var tabRewards: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabChallenges = view.findViewById(R.id.tabChallenges)
        tabRewards = view.findViewById(R.id.tabRewards)

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
