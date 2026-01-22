package hr.foi.air.otpstudent.ui.points

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.di.AppModule
import hr.foi.air.otpstudent.domain.model.RewardsFilter

class RewardsFragment : Fragment(R.layout.fragment_rewards) {

    // Shared VM s PointsFragment
    private val vm: LoyaltyViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    ) {
        LoyaltyViewModelFactory(AppModule.loyaltyRepository)
    }

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

        vm.rewardsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RewardsUiState.Loading -> { /* opcionalno loading */ }
                is RewardsUiState.Success -> {
                    val points = vm.points.value ?: 0L
                    adapter.submit(state.rewards, points)
                }
                is RewardsUiState.Error -> { /* opcionalno error */ }
            }
        }

        vm.loadRewards()
    }

    fun openRewardsFilter() {
        if (!isAdded) return
        showRewardsFilterDialog()
    }

    //dok ode s Rewards taba resetira filtere

    override fun onStop() {
        super.onStop()
        vm.clearRewardFiltersSilently()
    }

    private fun showRewardsFilterDialog() {
        val labels = arrayOf(
            "Mogu preuzeti",
            "Hrana i piće",
            "Sve za dom",
            "Ljepota i zdravlje",
            "Zabava i sport",
            "Bonovi i popusti",
            "OTP nagrade"
        )

        val map = arrayOf(
            RewardsFilter.CAN_GET,
            RewardsFilter.FOOD,
            RewardsFilter.HOME,
            RewardsFilter.HEALTH,
            RewardsFilter.FUN_AND_SPORT,
            RewardsFilter.BON_AND_DISCOUNT,
            RewardsFilter.OPT_REWARDS
        )

        val original = vm.getRewardFiltersSnapshot()

        // privremeni izbor
        val temp = original.toMutableSet()

        val checked = BooleanArray(labels.size) { idx ->
            temp.contains(map[idx])
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtriraj nagrade")
            .setMultiChoiceItems(labels, checked) { _, which, isChecked ->
                val f = map[which]
                if (isChecked) temp.add(f) else temp.remove(f)
            }
            .setNegativeButton("Odustani") { _, _ ->
                // nista
            }
            .setNeutralButton("Očisti") { _, _ ->
                vm.setRewardFilters(emptySet())
                vm.loadRewards() // vrati sve
            }
            .setPositiveButton("Primijeni") { _, _ ->
                vm.setRewardFilters(temp.toSet())
                vm.applyRewardFilters()
            }
            .show()
    }
}
