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

        val etPin = findViewById<EditText>(R.id.etPin)
        val btnOk = findViewById<MaterialButton>(R.id.btnOk)
        val btnNotYou = findViewById<MaterialButton>(R.id.btnNotYou)

        btnOk.setOnClickListener {
            viewModel.onPinEntered(etPin.text?.toString().orEmpty())
        }

        btnNotYou.setOnClickListener {
            viewModel.onNotYouClicked()
        }

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
