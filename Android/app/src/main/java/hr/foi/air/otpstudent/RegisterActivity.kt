package hr.foi.air.otpstudent

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private var passVisible  = false
    private var pass2Visible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val etEmail  = findViewById<EditText>(R.id.etEmail)
        val etPass   = findViewById<EditText>(R.id.etPass)
        val etPass2  = findViewById<EditText>(R.id.etPass2)
        val tilPass  = findViewById<TextInputLayout>(R.id.tilPass)
        val tilPass2 = findViewById<TextInputLayout>(R.id.tilPass2)
        val btn      = findViewById<Button>(R.id.btnRegister)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val tvLogin  = findViewById<TextView>(R.id.tvGoLogin)

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

        btn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass  = etPass.text.toString()
            val pass2 = etPass2.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.error = "Neispravan e-mail"; return@setOnClickListener }
            if (pass.length < 6) { etPass.error = "Minimalno 6 znakova"; return@setOnClickListener }
            if (pass != pass2) { etPass2.error = "Lozinke se ne podudaraju"; return@setOnClickListener }

            progress.visibility = View.VISIBLE
            btn.isEnabled = false

            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    progress.visibility = View.GONE
                    btn.isEnabled = true

                    if (task.isSuccessful) {
                        val home = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                        startActivity(home)

                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                        val userDoc = mapOf(
                            "email" to email,
                            "createdAt" to com.google.firebase.Timestamp.now()
                        )
                        Firebase.firestore.collection("users").document(uid)
                            .set(userDoc)
                            .addOnFailureListener { e ->

                            }
                    } else {
                        Toast.makeText(
                            this,
                            task.exception?.localizedMessage ?: "Greška pri registraciji",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

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
}