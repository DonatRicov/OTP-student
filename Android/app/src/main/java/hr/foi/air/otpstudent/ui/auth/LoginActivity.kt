package hr.foi.air.otpstudent.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import hr.foi.air.auth.pin.PinStore
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.di.AppModule
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class LoginActivity : AppCompatActivity() {

    private var isPasswordVisible = false

    private val viewModel: LoginViewModel by lazy {
        ViewModelProvider(this, LoginVmFactory())[LoginViewModel::class.java]
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            startActivity(
                Intent(this, hr.foi.air.otpstudent.StartActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
            )
            finish()
            return
        }

        val root = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = bars.top)
            insets
        }

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPass = findViewById<EditText>(R.id.etPass)
        val tilPass = findViewById<TextInputLayout>(R.id.textInputLayoutPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvReg = findViewById<TextView>(R.id.tvGoRegister)
        val progress = findViewById<ProgressBar>(R.id.progress)


        setPasswordHidden(etPass, tilPass)
        tilPass.setEndIconOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) setPasswordVisible(etPass, tilPass)
            else setPasswordHidden(etPass, tilPass)
        }

        tvReg.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                progress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                btnLogin.isEnabled = !state.isLoading
            }
        }

        lifecycleScope.launch {
            viewModel.effects.collectLatest { eff ->
                when (eff) {
                    LoginEffect.GoToSuccess -> {
                        lifecycleScope.launch {
                            logDailyLoginEvent()
                            goToSuccess()
                        }
                    }

                    is LoginEffect.ShowMessage -> {
                        Toast.makeText(this@LoginActivity, eff.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Neispravan e-mail"
                return@setOnClickListener
            }
            if (pass.isEmpty()) {
                etPass.error = "Unesi lozinku"
                return@setOnClickListener
            }

            viewModel.login(
                email = email,
                pass = pass,
                onSaveCreds = { e, _ ->
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (!uid.isNullOrBlank()) {
                        PinStore.setLastUid(this, uid)
                        PinStore.setLastUserLabel(this, e)
                        
                    }
                }
            )
        }
    }


    private fun goToSuccess() {
        startActivity(
            Intent(this@LoginActivity, LoginSuccessActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        )
        finish()
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
            Log.e("LoginActivity", "Failed to log loginEvent", e)
        }
    }

    private fun setPasswordVisible(et: EditText, til: TextInputLayout) {
        et.transformationMethod = HideReturnsTransformationMethod.getInstance()
        et.setSelection(et.text?.length ?: 0)
        til.endIconDrawable = getDrawable(R.drawable.ic_visibility)
        til.endIconContentDescription = getString(R.string.hide_password)
    }

    private fun setPasswordHidden(et: EditText, til: TextInputLayout) {
        et.transformationMethod = PasswordTransformationMethod.getInstance()
        et.setSelection(et.text?.length ?: 0)
        til.endIconDrawable = getDrawable(R.drawable.ic_visibility_off)
        til.endIconContentDescription = getString(R.string.show_password)
    }

    private class LoginVmFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(AppModule.authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
