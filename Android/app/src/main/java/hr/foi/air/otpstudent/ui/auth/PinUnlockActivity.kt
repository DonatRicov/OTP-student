package hr.foi.air.otpstudent.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import hr.foi.air.auth.pin.PinVerifier
import hr.foi.air.otpstudent.MainActivity
import hr.foi.air.otpstudent.R
import hr.foi.air.core.auth.SecureCreds
import hr.foi.air.otpstudent.di.AppModule
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.widget.Button


class PinUnlockActivity : AppCompatActivity() {

    private val viewModel: PinUnlockViewModel by lazy {
        ViewModelProvider(this, PinVmFactory())[PinUnlockViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_unlock)

        val tvOtherLogin = findViewById<android.widget.TextView>(R.id.tvOtherLogin)
        val tvForgotPin = findViewById<android.widget.TextView>(R.id.tvForgotPin)

        val btnOk = findViewById<android.view.View>(R.id.keyOk)
        val btnDel = findViewById<android.view.View>(R.id.keyDel)

        val pinBoxes = listOf(
            findViewById<android.view.View>(R.id.pin1),
            findViewById<android.view.View>(R.id.pin2),
            findViewById<android.view.View>(R.id.pin3),
            findViewById<android.view.View>(R.id.pin4),
            findViewById<android.view.View>(R.id.pin5),
            findViewById<android.view.View>(R.id.pin6),
        )

        var pin = StringBuilder()

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


        fun appendDigit(d: String) {
            if (pin.length >= 6) return
            pin.append(d)
            renderPin()
            if (pin.length == 6) {
                viewModel.onPinEntered(pin.toString())
            }
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
            if (pin.length == 6) viewModel.onPinEntered(pin.toString())
        }

        tvOtherLogin.setOnClickListener { viewModel.onNotYouClicked() }
        tvForgotPin.setOnClickListener { viewModel.onNotYouClicked() }

        renderPin()

        lifecycleScope.launch {
            viewModel.effects.collectLatest { eff ->
                when (eff) {
                    PinUnlockEffect.GoToMain -> {
                        startActivity(
                            Intent(this@PinUnlockActivity, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                        )
                        finish()
                    }
                    PinUnlockEffect.GoToLogin -> {
                        startActivity(
                            Intent(this@PinUnlockActivity, LoginActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                        )
                        finish()
                    }
                    is PinUnlockEffect.ShowMessage -> {
                        Toast.makeText(this@PinUnlockActivity, eff.message, Toast.LENGTH_LONG).show()

                    }
                }
            }
        }
    }


    private inner class PinVmFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PinUnlockViewModel::class.java)) {
                val authRepo = AppModule.authRepository

                val getCreds = {
                    val email = SecureCreds.getEmail(this@PinUnlockActivity).orEmpty()
                    val pass = SecureCreds.getPass(this@PinUnlockActivity).orEmpty()
                    if (email.isBlank() || pass.isBlank()) null else SavedCreds(email, pass)
                }

                val clearCreds = { SecureCreds.clear(this@PinUnlockActivity) }

                val verify = { pin: String -> PinVerifier.verify(this@PinUnlockActivity, pin) }

                @Suppress("UNCHECKED_CAST")
                return PinUnlockViewModel(authRepo, getCreds, clearCreds, verify) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
