package hr.foi.air.otpstudent

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import android.widget.TextView

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPass = findViewById<EditText>(R.id.etPass)
        val etPass2 = findViewById<EditText>(R.id.etPass2)
        val btn = findViewById<Button>(R.id.btnRegister)
        val progress = findViewById<ProgressBar>(R.id.progress)

        val tvLogin = findViewById<TextView>(R.id.tvGoLogin)

        tvLogin.setOnClickListener {

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString()
            val pass2 = etPass2.text.toString()

            // Validacija
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

            progress.visibility = View.VISIBLE
            btn.isEnabled = false

            // Firebase Auth - kreiranje korisnika
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    progress.visibility = View.GONE
                    btn.isEnabled = true

                    if (task.isSuccessful) {
                        val uid = auth.currentUser!!.uid
                        // Kreiraj osnovni profil u Firestore-u
                        val userDoc = mapOf(
                            "email" to email,
                            "createdAt" to com.google.firebase.Timestamp.now()
                        )
                        Firebase.firestore.collection("users").document(uid)
                            .set(userDoc)
                            .addOnCompleteListener {
                                Toast.makeText(
                                    this,
                                    "Registracija uspješna!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
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
}
