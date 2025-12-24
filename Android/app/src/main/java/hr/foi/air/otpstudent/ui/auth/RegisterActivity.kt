package hr.foi.air.otpstudent.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.di.AppModule
import hr.foi.air.otpstudent.ui.profile.ProfilePersonalActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private var passVisible = false
    private var pass2Visible = false

    private val viewModel: RegisterViewModel by lazy {
        ViewModelProvider(this, RegisterVmFactory())[RegisterViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPass = findViewById<EditText>(R.id.etPass)
        val etPass2 = findViewById<EditText>(R.id.etPass2)
        val tilPass = findViewById<TextInputLayout>(R.id.tilPass)
        val tilPass2 = findViewById<TextInputLayout>(R.id.tilPass2)
        val btn = findViewById<Button>(R.id.btnRegister)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val tvLogin = findViewById<TextView>(R.id.tvGoLogin)

        setHidden(etPass, tilPass)
        setHidden(etPass2, tilPass2)

        tilPass.setEndIconOnClickListener {
            passVisible = !passVisible
            if (passVisible) setVisible(etPass, tilPass) else setHidden(etPass, tilPass)
        }
        tilPass2.setEndIconOnClickListener {
            pass2Visible = !pass2Visible
            if (pass2Visible) setVisible(etPass2, tilPass2) else setHidden(etPass2, tilPass2)
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                progress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                btn.isEnabled = !state.isLoading

                if (state.error != null) {
                    Toast.makeText(this@RegisterActivity, state.error, Toast.LENGTH_LONG).show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.effects.collectLatest { eff ->
                when (eff) {
                    RegisterEffect.GoToProfilePersonal -> {
                        startActivity(
                            Intent(this@RegisterActivity, ProfilePersonalActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                        )
                        finish()
                    }
                    is RegisterEffect.ShowMessage -> {
                        Toast.makeText(this@RegisterActivity, eff.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // register click -> VM
        btn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString()
            val pass2 = etPass2.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Neispravan e-mail"
                return@setOnClickListener
            }
            if (pass.length < 6) {
                etPass.error = "Minimalno 6 znakova"
                return@setOnClickListener
            }
            if (pass != pass2) {
                etPass2.error = "Lozinke se ne podudaraju"
                return@setOnClickListener
            }

            viewModel.register(email, pass)
        }
    }

    private fun setVisible(et: EditText, til: TextInputLayout) {
        et.transformationMethod = HideReturnsTransformationMethod.getInstance()
        et.setSelection(et.text?.length ?: 0)
        til.endIconDrawable = getDrawable(R.drawable.ic_visibility)
        til.endIconContentDescription = getString(R.string.hide_password)
    }

    private fun setHidden(et: EditText, til: TextInputLayout) {
        et.transformationMethod = PasswordTransformationMethod.getInstance()
        et.setSelection(et.text?.length ?: 0)
        til.endIconDrawable = getDrawable(R.drawable.ic_visibility_off)
        til.endIconContentDescription = getString(R.string.show_password)
    }

    private inner class RegisterVmFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RegisterViewModel(AppModule.authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
