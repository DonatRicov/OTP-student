package hr.foi.air.otpstudent

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import hr.foi.air.auth.pin.PinVerifier

class PinUnlockActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_unlock)

        val etPin = findViewById<EditText>(R.id.etPin)
        val btnOk = findViewById<MaterialButton>(R.id.btnOk)
        val btnNotYou = findViewById<MaterialButton>(R.id.btnNotYou)

        btnOk.setOnClickListener {
            onPinEntered(etPin.text?.toString().orEmpty())
        }

        btnNotYou.setOnClickListener {
            // Obriši spremljene podatke
            SecureCreds.clear(this)

            // Prebaci na normalni login
            startActivity(
                Intent(this, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
            )
            finish()
        }
    }

    private fun onPinEntered(pin: String) {
        val email = SecureCreds.getEmail(this)
        val pass = SecureCreds.getPass(this)

        if (email.isNullOrBlank() || pass.isNullOrBlank()) {
            goToLogin()
            return
        }

        if (!PinVerifier.verify(this, pin)) {
            Toast.makeText(this, "Neispravan PIN", Toast.LENGTH_LONG).show()
            return
        }

        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { t ->
                if (t.isSuccessful) {
                    startActivity(
                        Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                    )
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

    private fun goToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        )
        finish()
    }
}
