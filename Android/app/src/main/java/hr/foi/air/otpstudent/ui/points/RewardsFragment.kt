package hr.foi.air.otpstudent.ui.points

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.di.AppModule
import hr.foi.air.otpstudent.domain.model.RewardsFilter

class RewardsFragment : Fragment(R.layout.fragment_rewards) {

    // Shared VM sa svima koji koriste bodove
    private val vm: LoyaltyViewModel by activityViewModels {
        LoyaltyViewModelFactory(AppModule.loyaltyRepository)
    }

    private lateinit var adapter: RewardsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvRewards)

        adapter = RewardsAdapter(
            onOpenDetails = { rewardId ->
                findNavController().navigate(
                    R.id.rewardDetailsFragment,
                    RewardDetailsFragment.createArgs(
                        rewardId = rewardId,
                        userPoints = vm.points.value ?: 0L
                    )
                )
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
            .setNegativeButton("Odustani") { _, _ -> }
            .setNeutralButton("Očisti") { _, _ ->
                vm.setRewardFilters(emptySet())
                vm.loadRewards()
            }
            .setPositiveButton("Primijeni") { _, _ ->
                vm.setRewardFilters(temp.toSet())
                vm.applyRewardFilters()
            }
            .show()
    }
}
