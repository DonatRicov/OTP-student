package hr.foi.air.otpstudent.ui.points

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import hr.foi.air.otpstudent.domain.model.RewardsFilter


class RewardsFilterCheckboxDialogFragment(
    private val initialSelected: Set<RewardsFilter> = emptySet(),
    private val onApply: (Set<RewardsFilter>) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val items = arrayOf(
            "Mogu preuzeti",
            "Hrana i piće",
            "Sve za dom",
            "Ljepota i zdravlje",
            "Zabava i sport",
            "Bonovi i popusti",
            "OTP nagrade"
        )

        val values = arrayOf(
            RewardsFilter.CAN_GET,
            RewardsFilter.FOOD,
            RewardsFilter.HOME,
            RewardsFilter.HEALTH,
            RewardsFilter.FUN_AND_SPORT,
            RewardsFilter.BON_AND_DISCOUNT,
            RewardsFilter.OPT_REWARDS
        )

        val checked = BooleanArray(values.size) { idx ->
            initialSelected.contains(values[idx])
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("Filtriraj nagrade")
            .setMultiChoiceItems(items, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setNegativeButton("Odustani", null)
            .setNeutralButton("Očisti") { _, _ ->
                //bez filtera
                onApply(emptySet())
            }
            .setPositiveButton("Primijeni") { _, _ ->
                val selected = buildSet {
                    for (i in values.indices) {
                        if (checked[i]) add(values[i])
                    }
                }
                onApply(selected)
            }
            .create()
    }
}
