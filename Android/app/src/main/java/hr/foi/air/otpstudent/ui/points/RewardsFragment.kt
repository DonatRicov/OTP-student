package hr.foi.air.otpstudent.ui.points

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.di.AppModule

class RewardsFragment : Fragment(R.layout.fragment_rewards) {

    private lateinit var vm: LoyaltyViewModel
    private lateinit var adapter: RewardsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvRewards)

        adapter = RewardsAdapter(
            onRedeem = { rewardId ->
                vm.redeemReward(rewardId)
            }
        )

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        val repo = AppModule.loyaltyRepository
        vm = ViewModelProvider(
            this,
            LoyaltyViewModelFactory(repo)
        )[LoyaltyViewModel::class.java]

        // rewards list
        vm.rewardsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RewardsUiState.Loading -> {
                    //za progress ak bu trebalo
                }
                is RewardsUiState.Success -> {
                    val points = vm.points.value ?: 0L
                    adapter.submit(state.rewards, points)
                }
                is RewardsUiState.Error -> {
                    // ak bu trebalo
                }
            }
        }

        vm.loadRewards()
    }
}
