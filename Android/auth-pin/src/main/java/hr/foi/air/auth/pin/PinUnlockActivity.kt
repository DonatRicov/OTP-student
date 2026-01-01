package hr.foi.air.auth.pin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PinUnlockActivity : AppCompatActivity() {

    private fun finishOk() {
        setResult(
            RESULT_OK,
            Intent().putExtra(PinUnlockContract.EXTRA_RESULT, PinUnlockContract.RESULT_OK)
        )
        finish()
    }

    private fun finishNotYou() {
        setResult(
            RESULT_OK,
            Intent().putExtra(PinUnlockContract.EXTRA_RESULT, PinUnlockContract.RESULT_NOT_YOU)
        )
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_unlock)

        val tvOtherLogin = findViewById<TextView>(R.id.tvOtherLogin)
        val tvForgotPin = findViewById<TextView>(R.id.tvForgotPin)

        val btnOk = findViewById<View>(R.id.keyOk)
        val btnDel = findViewById<View>(R.id.keyDel)

        val pinBoxes = listOf(
            findViewById<View>(R.id.pin1),
            findViewById<View>(R.id.pin2),
            findViewById<View>(R.id.pin3),
            findViewById<View>(R.id.pin4),
            findViewById<View>(R.id.pin5),
            findViewById<View>(R.id.pin6),
        )

        val pin = StringBuilder()

        fun renderPin() {
            pinBoxes.forEachIndexed { index, v ->
                v.setBackgroundResource(
                    if (index < pin.length) R.drawable.pin_box_filled
                    else R.drawable.pin_box_empty
                )
            }

            btnOk.isEnabled = pin.length == 6
            btnOk.alpha = if (btnOk.isEnabled) 1f else 0.4f
        }

        fun resetPin() {
            pin.clear()
            renderPin()
        }

        fun verifyAndFinishIfComplete() {
            if (pin.length != 6) return

            val ok = PinVerifier.verify(this@PinUnlockActivity, pin.toString())
            if (ok) {
                finishOk()
            } else {
                Toast.makeText(this@PinUnlockActivity, "Neispravan PIN", Toast.LENGTH_LONG).show()
                resetPin()
            }
        }

        fun appendDigit(d: String) {
            if (pin.length >= 6) return
            pin.append(d)
            renderPin()
            verifyAndFinishIfComplete()
        }

        fun deleteDigit() {
            if (pin.isEmpty()) return
            pin.deleteCharAt(pin.length - 1)
            renderPin()
        }

        val digitButtons = listOf(
            findViewById<Button>(R.id.key1),
            findViewById<Button>(R.id.key2),
            findViewById<Button>(R.id.key3),
            findViewById<Button>(R.id.key4),
            findViewById<Button>(R.id.key5),
            findViewById<Button>(R.id.key6),
            findViewById<Button>(R.id.key7),
            findViewById<Button>(R.id.key8),
            findViewById<Button>(R.id.key9),
        )
        val btn0 = findViewById<Button>(R.id.key0)

        fun setupRandomKeypad() {
            val digits = (1..9).map { it.toString() }.shuffled()

            digitButtons.forEachIndexed { i, btn ->
                val d = digits[i]
                btn.text = d
                btn.setOnClickListener { appendDigit(d) }
            }

            btn0.text = "0"
            btn0.setOnClickListener { appendDigit("0") }
        }

        setupRandomKeypad()

        btnDel.setOnClickListener { deleteDigit() }

        btnOk.setOnClickListener {
            verifyAndFinishIfComplete()
        }

        tvOtherLogin.setOnClickListener { finishNotYou() }
        tvForgotPin.setOnClickListener { finishNotYou() }

        renderPin()
    }
}
