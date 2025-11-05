package hr.foi.air.otpstudent

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPass  = findViewById<EditText>(R.id.etPass)
        val btn     = findViewById<Button>(R.id.btnLogin)
        val btnPin  = findViewById<Button>(R.id.btnPin)
        val btnBio  = findViewById<Button>(R.id.btnBiometric)
        val tvReg   = findViewById<TextView>(R.id.tvGoRegister)
        val progress= findViewById<ProgressBar>(R.id.progress)

        tvReg.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnPin.setOnClickListener {
            Toast.makeText(this, "PIN login uskoro", Toast.LENGTH_SHORT).show()
        }
        btnBio.setOnClickListener {
            Toast.makeText(this, "Biometrija uskoro", Toast.LENGTH_SHORT).show()
        }

        btn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass  = etPass.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Neispravan e-mail"; return@setOnClickListener
            }
            if (pass.isEmpty()) {
                etPass.error = "Unesi lozinku"; return@setOnClickListener
            }

            progress.visibility = View.VISIBLE
            btn.isEnabled = false

            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { t ->
                    progress.visibility = View.GONE
                    btn.isEnabled = true

                    if (t.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            t.exception?.localizedMessage ?: "Prijava nije uspjela",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}
