package hr.foi.air.otpstudent

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.textfield.TextInputLayout
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import com.google.android.material.button.MaterialButton
import hr.foi.air.core.auth.AuthRegistry
import hr.foi.air.core.auth.AuthRequest
import hr.foi.air.core.auth.AuthResult

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Toast.makeText(this, "Auth plugins: ${AuthRegistry.available().size}", Toast.LENGTH_LONG).show()

        auth = FirebaseAuth.getInstance()

        val etEmail  = findViewById<EditText>(R.id.etEmail)
        val etPass   = findViewById<EditText>(R.id.etPass)
        val tilPass  = findViewById<TextInputLayout>(R.id.textInputLayoutPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvReg    = findViewById<TextView>(R.id.tvGoRegister)
        val progress = findViewById<ProgressBar>(R.id.progress)

        val authContainer = findViewById<LinearLayout>(R.id.authMethodsContainer)

        AuthRegistry.available().forEach { plugin ->
            val spec = plugin.uiSpec()

            val btn = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
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
                    val request = AuthRequest(
                        email = etEmail.text.toString().trim()
                    )

                    plugin.authenticate(this@LoginActivity, request) { result ->
                        when (result) {
                            is AuthResult.Success -> {
                                startActivity(Intent(this@LoginActivity, LoginSuccessActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                })
                            }
                            is AuthResult.Error -> Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_LONG).show()
                            AuthResult.Cancelled -> Unit
                        }
                    }
                }
            }

            authContainer.addView(btn)
        }


        setPasswordHidden(etPass, tilPass)

        tilPass.setEndIconOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) setPasswordVisible(etPass, tilPass)
            else setPasswordHidden(etPass, tilPass)
        }

        tvReg.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }


        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass  = etPass.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Neispravan e-mail"; return@setOnClickListener
            }
            if (pass.isEmpty()) { etPass.error = "Unesi lozinku"; return@setOnClickListener }

            progress.visibility = View.VISIBLE
            btnLogin.isEnabled = false

            auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { t ->
                progress.visibility = View.GONE
                btnLogin.isEnabled = true
                if (t.isSuccessful) {
                    val intent = Intent(this, LoginSuccessActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    startActivity(intent)
                }
                else {
                    Toast.makeText(this, t.exception?.localizedMessage ?: "Prijava nije uspjela", Toast.LENGTH_LONG).show()
                }
            }
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

}