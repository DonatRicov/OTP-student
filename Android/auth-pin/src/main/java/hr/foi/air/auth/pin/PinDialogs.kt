package hr.foi.air.auth.pin

import android.text.InputFilter
import android.text.InputType
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

object PinDialogs {

    private const val PIN_LENGTH = 6

    fun showSetup(activity: FragmentActivity, onDone: (pin1: String, pin2: String) -> Unit) {
        val til1 = TextInputLayout(activity).apply { hint = "PIN ($PIN_LENGTH znamenki)" }
        val et1 = TextInputEditText(activity).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            filters = arrayOf(InputFilter.LengthFilter(PIN_LENGTH))
        }
        til1.addView(et1)

        val til2 = TextInputLayout(activity).apply { hint = "Ponovi PIN" }
        val et2 = TextInputEditText(activity).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            filters = arrayOf(InputFilter.LengthFilter(PIN_LENGTH))
        }
        til2.addView(et2)

        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 0)
            addView(til1)
            addView(til2)
        }

        AlertDialog.Builder(activity)
            .setTitle("Postavi PIN")
            .setView(container)
            .setPositiveButton("Spremi") { _, _ ->
                onDone(et1.text?.toString().orEmpty(), et2.text?.toString().orEmpty())
            }
            .setNegativeButton("Odustani") { d, _ -> d.dismiss() }
            .show()
    }

    fun showVerify(activity: FragmentActivity, onDone: (pin: String) -> Unit) {
        val til = TextInputLayout(activity).apply { hint = "Unesi PIN" }
        val et = TextInputEditText(activity).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            filters = arrayOf(InputFilter.LengthFilter(PIN_LENGTH))
        }
        til.addView(et)

        AlertDialog.Builder(activity)
            .setTitle("PIN prijava")
            .setView(til)
            .setPositiveButton("Potvrdi") { _, _ ->
                onDone(et.text?.toString().orEmpty())
            }
            .setNegativeButton("Odustani") { d, _ -> d.dismiss() }
            .show()
    }
}
