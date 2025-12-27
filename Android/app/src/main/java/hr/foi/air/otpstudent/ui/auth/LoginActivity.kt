package hr.foi.air.otpstudent.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import hr.foi.air.core.auth.AuthRegistry
import hr.foi.air.core.auth.AuthRequest
import hr.foi.air.core.auth.AuthResult
import hr.foi.air.otpstudent.R
import hr.foi.air.core.auth.SecureCreds
import hr.foi.air.otpstudent.di.AppModule
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private var isPasswordVisible = false

    private val viewModel: LoginViewModel by lazy {
        ViewModelProvider(this, LoginVmFactory())[LoginViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Toast.makeText(this, "Auth plugins: ${AuthRegistry.available().size}", Toast.LENGTH_LONG).show()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPass = findViewById<EditText>(R.id.etPass)
        val tilPass = findViewById<TextInputLayout>(R.id.textInputLayoutPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvReg = findViewById<TextView>(R.id.tvGoRegister)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val authContainer = findViewById<LinearLayout>(R.id.authMethodsContainer)

        setupAuthPlugins(authContainer, etEmail)

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
                        startActivity(
                            Intent(this@LoginActivity, LoginSuccessActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                        )
                        finish()
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
                onSaveCreds = { e, p -> SecureCreds.save(this, e, p) }
            )
        }
    }

    private fun setupAuthPlugins(container: LinearLayout, etEmail: EditText) {
        container.removeAllViews()

        val enabledPlugins = AuthRegistry.available().filter { it.isEnabled(this) }

        enabledPlugins.forEach { plugin ->
            val spec = plugin.uiSpec()

            val btn = MaterialButton(
                this,
                null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle
            ).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    resources.getDimensionPixelSize(R.dimen.auth_btn_height)
                ).also { it.topMargin = resources.getDimensionPixelSize(R.dimen.auth_btn_margin_top) }

                text = spec.title
                isAllCaps = false

                spec.iconRes?.let {
                    setIconResource(it)
                    iconGravity = MaterialButton.ICON_GRAVITY_TEXT_END
                    iconPadding = resources.getDimensionPixelSize(R.dimen.auth_btn_icon_padding)
                }

                setOnClickListener {
                    val request = AuthRequest(email = etEmail.text.toString().trim())

                    plugin.authenticate(this@LoginActivity, request) { result ->
                        when (result) {
                            is AuthResult.Success -> {
                                startActivity(
                                    Intent(this@LoginActivity, LoginSuccessActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    }
                                )
                                finish()
                            }
                            is AuthResult.Error ->
                                Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_LONG).show()
                            AuthResult.Cancelled -> Unit
                        }
                    }
                }
            }

            container.addView(btn)
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

    private inner class LoginVmFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(AppModule.authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
