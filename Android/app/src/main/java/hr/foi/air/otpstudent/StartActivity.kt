package hr.foi.air.otpstudent

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import hr.foi.air.auth.pin.PinUnlockActivity
import hr.foi.air.auth.pin.PinUnlockContract
import hr.foi.air.core.auth.AuthRegistry
import hr.foi.air.core.auth.AuthRequest
import hr.foi.air.core.auth.AuthResult
import hr.foi.air.core.auth.SecureCreds
import hr.foi.air.otpstudent.di.AppModule
import hr.foi.air.otpstudent.ui.auth.LoginActivity
import hr.foi.air.otpstudent.ui.auth.RegisterActivity
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import android.util.Log
import com.google.firebase.FirebaseApp

class StartActivity : AppCompatActivity() {

    private val pinUnlockLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode != RESULT_OK) {
            showStartScreen()
            return@registerForActivityResult
        }

        when (res.data?.getStringExtra(PinUnlockContract.EXTRA_RESULT)) {
            PinUnlockContract.RESULT_OK -> autoLoginWithSavedCreds()
            PinUnlockContract.RESULT_NOT_YOU -> {
                SecureCreds.clear(this)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            else -> showStartScreen()
        }
    }

    private fun launchUnlockFlow() {
        val hasCreds =
            !SecureCreds.getEmail(this).isNullOrBlank() &&
                    !SecureCreds.getPass(this).isNullOrBlank()
        if (!hasCreds) {
            showStartScreen()
            return
        }

        val bioPlugin = AuthRegistry.available().firstOrNull { it.uiSpec().id == "bio" }
        val bioReady = bioPlugin?.isEnabled(this) == true && bioPlugin.isConfigured(this)

        val pinPlugin = AuthRegistry.available().firstOrNull { it.uiSpec().id == "pin" }
        val pinReady = pinPlugin?.isEnabled(this) == true && pinPlugin.isConfigured(this)

        when {
            bioReady -> {
                bioPlugin.authenticate(this, AuthRequest()) { result ->
                    runOnUiThread {
                        when (result) {
                            is AuthResult.Success -> {
                                autoLoginWithSavedCreds()
                            }

                            is AuthResult.Error -> {
                                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                                fallbackToPinOrStart()
                            }

                            AuthResult.Cancelled -> {
                                fallbackToPinOrStart()
                            }
                        }
                    }
                }

            }

            pinReady -> {
                pinUnlockLauncher.launch(Intent(this, PinUnlockActivity::class.java))
            }

            else -> showStartScreen()
        }
    }
    private fun fallbackToPinOrStart() {
        val pinPlugin = AuthRegistry.available().firstOrNull { it.uiSpec().id == "pin" }
        val pinReady = pinPlugin?.isEnabled(this) == true && pinPlugin.isConfigured(this)

        if (pinReady) {
            pinUnlockLauncher.launch(Intent(this, PinUnlockActivity::class.java))
        } else {
            showStartScreen()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        launchUnlockFlow()
    }


    private fun showStartScreen() {
        setContentView(R.layout.activity_start)

        val btnRegister = findViewById<MaterialButton>(R.id.btnStartRegister)
        val tvLogin = findViewById<TextView>(R.id.tvStartLogin)
        val tvTagline = findViewById<TextView>(R.id.tvTagline)

        val html = """
            <font color="#005F3A">OTP</font><font color="#F7941D">akiraj</font> svoju karijeru!
        """.trimIndent()

        tvTagline.text = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun autoLoginWithSavedCreds() {
        val email = SecureCreds.getEmail(this).orEmpty()
        val pass = SecureCreds.getPass(this).orEmpty()

        if (email.isBlank() || pass.isBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                AppModule.authRepository.login(email, pass)
                logDailyLoginEvent()

                startActivity(
                    Intent(this@StartActivity, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                )
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@StartActivity,
                    e.message ?: "Prijava nije uspjela",
                    Toast.LENGTH_LONG
                ).show()
                startActivity(Intent(this@StartActivity, LoginActivity::class.java))
                finish()
            }
        }
    }

    private suspend fun logDailyLoginEvent() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val dateId = LocalDate.now().toString()
        val ref = db.collection("users")
            .document(uid)
            .collection("loginEvents")
            .document(dateId)

        try {
            val snap = ref.get().await()
            if (!snap.exists()) {
                ref.set(mapOf("createdAt" to FieldValue.serverTimestamp())).await()
            }
        } catch (e: Exception) {
            Log.e("StartActivity", "Failed to log loginEvent", e)
        }
    }

    private fun shouldUsePinUnlock(): Boolean {
        val hasCreds =
            !SecureCreds.getEmail(this).isNullOrBlank() &&
                    !SecureCreds.getPass(this).isNullOrBlank()

        if (!hasCreds) return false

        val pinPlugin = AuthRegistry.available().firstOrNull { it.uiSpec().id == "pin" }
        val pinReady = pinPlugin?.isEnabled(this) == true && pinPlugin.isConfigured(this)

        val bioPlugin = AuthRegistry.available().firstOrNull { it.uiSpec().id == "bio" }
        val bioReady = bioPlugin?.isEnabled(this) == true && bioPlugin.isConfigured(this)

        return pinReady || bioReady
    }
}
