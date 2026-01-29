package hr.foi.air.otpstudent.ui.points

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.di.AppModule
import androidx.core.view.isVisible

class RedeemedRewardsFragment : Fragment(R.layout.fragment_redeemed_rewards) {

    private val vm: LoyaltyViewModel by activityViewModels {
        LoyaltyViewModelFactory(AppModule.loyaltyRepository)
    }

    private lateinit var adapter: RedeemedRewardsAdapter

    private fun setBottomNavVisible(visible: Boolean) {
        activity?.findViewById<View>(R.id.bottomNavigationView)?.isVisible = visible
    }

    override fun onStart() {
        super.onStart()
        setBottomNavVisible(false)
    }

    override fun onStop() {
        setBottomNavVisible(true)
        super.onStop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            findNavController().navigateUp()
        }

        val rv = view.findViewById<RecyclerView>(R.id.rvRedeemedRewards)

        adapter = RedeemedRewardsAdapter { rewardId, redemptionId ->
            findNavController().navigate(
                R.id.redeemedRewardDetailsFragment,
                RedeemedRewardDetailsFragment.createArgs(rewardId, redemptionId)
            )
        }

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        vm.redeemedRewardsState.observe(viewLifecycleOwner) { st ->
            when (st) {
                is RedeemedRewardsUiState.Loading -> { /* show loading */ }
                is RedeemedRewardsUiState.Success -> adapter.submit(st.items)
                is RedeemedRewardsUiState.Error -> { /* show error */ }
            }
        }

        vm.loadRedeemedRewards()
    }
}
