package hr.foi.air.otpstudent

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import hr.foi.air.auth.bio.BioStore
import hr.foi.air.auth.pin.PinStore
import hr.foi.air.auth.pin.PinUnlockActivity
import hr.foi.air.auth.pin.PinUnlockContract
import hr.foi.air.core.auth.AuthRegistry
import hr.foi.air.core.auth.AuthRequest
import hr.foi.air.core.auth.AuthResult
import hr.foi.air.otpstudent.data.auth.AppLockStore
import hr.foi.air.otpstudent.data.auth.QuickLoginManager
import hr.foi.air.otpstudent.ui.auth.LoginActivity
import hr.foi.air.otpstudent.ui.auth.RegisterActivity
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class StartActivity : AppCompatActivity() {

    private val LOCK_THRESHOLD_MS = 30_000L

    private val pinUnlockLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode != RESULT_OK) {
            showStartScreen()
            return@registerForActivityResult
        }

        when (res.data?.getStringExtra(PinUnlockContract.EXTRA_RESULT)) {
            PinUnlockContract.RESULT_OK -> refreshSessionThenOpenMain()
            PinUnlockContract.RESULT_NOT_YOU -> {
                FirebaseAuth.getInstance().signOut()
                QuickLoginManager.resetQuickLogin(this)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            else -> showStartScreen()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        QuickLoginManager.enforceUserScope(this)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            showStartScreen()
            return
        }

        if (!isTaskRoot) {
            finish()
            return
        }

        launchUnlockFlow(user.uid)
    }

    private fun launchUnlockFlow(uid: String) {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            showStartScreen()
            return
        }

        val bioPlugin = AuthRegistry.available().firstOrNull { it.uiSpec().id == "bio" }
        val bioReady = BioStore.isEnabled(this) && (bioPlugin?.isConfigured(this) == true)

        val pinReady = PinStore.isEnabled(this, uid) && PinStore.hasPin(this, uid)

        when {
            pinReady -> {
                val i = Intent(this, PinUnlockActivity::class.java).apply {
                    putExtra(PinUnlockActivity.EXTRA_UID, uid)
                    putExtra(
                        PinUnlockActivity.EXTRA_USER_LABEL,
                        PinStore.getLastUserLabel(this@StartActivity)
                    )
                    putExtra(PinUnlockActivity.EXTRA_TRY_BIO_FIRST, bioReady)
                }
                pinUnlockLauncher.launch(i)
            }

            bioReady && bioPlugin != null -> {
                bioPlugin.authenticate(this, AuthRequest()) { result ->
                    runOnUiThread {
                        when (result) {
                            is AuthResult.Success -> refreshSessionThenOpenMain()
                            is AuthResult.Error -> {
                                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                                showStartScreen()
                            }
                            AuthResult.Cancelled -> showStartScreen()
                        }
                    }
                }
            }

            else -> refreshSessionThenOpenMain()
        }
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
            overridePendingTransition(0, 0)
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(0, 0)
        }
    }

    private fun refreshSessionThenOpenMain() {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
            return
        }

        user.getIdToken(true)
            .addOnSuccessListener {
                AppLockStore.clear(this)

                startActivity(
                    Intent(this@StartActivity, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                )
                overridePendingTransition(0, 0)
                finish()
            }
            .addOnFailureListener {
                auth.signOut()
                QuickLoginManager.resetQuickLogin(this)
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(0, 0)
                finish()
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
}