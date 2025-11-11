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
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val etEmail  = findViewById<EditText>(R.id.etEmail)
        val etPass   = findViewById<EditText>(R.id.etPass)
        val tilPass  = findViewById<TextInputLayout>(R.id.textInputLayoutPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnPin   = findViewById<Button>(R.id.btnPin)
        val btnBio   = findViewById<Button>(R.id.btnBiometric)
        val tvReg    = findViewById<TextView>(R.id.tvGoRegister)
        val progress = findViewById<ProgressBar>(R.id.progress)

        setPasswordHidden(etPass, tilPass)

        tilPass.setEndIconOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) setPasswordVisible(etPass, tilPass)
            else setPasswordHidden(etPass, tilPass)
        }

        tvReg.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
        btnPin.setOnClickListener { Toast.makeText(this, "PIN login uskoro", Toast.LENGTH_SHORT).show() }
        btnBio.setOnClickListener { Toast.makeText(this, "Biometrija uskoro", Toast.LENGTH_SHORT).show() }

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
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
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