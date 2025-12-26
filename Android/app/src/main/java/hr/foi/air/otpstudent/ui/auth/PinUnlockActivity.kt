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
import hr.foi.air.otpstudent.SecureCreds
import hr.foi.air.otpstudent.di.AppModule
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PinUnlockActivity : AppCompatActivity() {

    private val viewModel: PinUnlockViewModel by lazy {
        ViewModelProvider(this, PinVmFactory())[PinUnlockViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_unlock)

        // Klikabilni linkovi
        val tvOtherLogin = findViewById<android.widget.TextView>(R.id.tvOtherLogin)
        val tvForgotPin = findViewById<android.widget.TextView>(R.id.tvForgotPin)

// Keypad gumbi (moraš imati ove ID-eve u XML-u)
        val keys = listOf(
            findViewById<android.view.View>(R.id.key1) to "1",
            findViewById<android.view.View>(R.id.key2) to "2",
            findViewById<android.view.View>(R.id.key3) to "3",
            findViewById<android.view.View>(R.id.key4) to "4",
            findViewById<android.view.View>(R.id.key5) to "5",
            findViewById<android.view.View>(R.id.key6) to "6",
            findViewById<android.view.View>(R.id.key7) to "7",
            findViewById<android.view.View>(R.id.key8) to "8",
            findViewById<android.view.View>(R.id.key9) to "9",
            findViewById<android.view.View>(R.id.key0) to "0",
        )

        val btnOk = findViewById<android.view.View>(R.id.keyOk)
        val btnDel = findViewById<android.view.View>(R.id.keyDel)

// PIN kockice (6 kom)
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
            // popuni kockice (npr. promijeni alpha / background)
            pinBoxes.forEachIndexed { index, v ->
                v.alpha = if (index < pin.length) 1f else 0.25f
            }
            // po želji: OK enabled tek kad je unesen cijeli PIN
            btnOk.isEnabled = pin.length == 6
            btnOk.alpha = if (btnOk.isEnabled) 1f else 0.4f
        }

        fun appendDigit(d: String) {
            if (pin.length >= 6) return
            pin.append(d)
            renderPin()
            if (pin.length == 6) {
                // Auto-submit čim je unesen PIN
                viewModel.onPinEntered(pin.toString())
            }
        }

        fun deleteDigit() {
            if (pin.isEmpty()) return
            pin.deleteCharAt(pin.length - 1)
            renderPin()
        }

        keys.forEach { (view, digit) ->
            view.setOnClickListener { appendDigit(digit) }
        }

        btnDel.setOnClickListener { deleteDigit() }

// Ako želiš ručni OK (umjesto auto-submit)
        btnOk.setOnClickListener {
            if (pin.length == 6) viewModel.onPinEntered(pin.toString())
        }

// “Drugi načini prijave” (mapiramo na postojeći handler)
        tvOtherLogin.setOnClickListener { viewModel.onNotYouClicked() }

// Ako “Zaboravili ste pin?” vodi na isti flow:
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
