package hr.foi.air.otpstudent

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import hr.foi.air.core.auth.AuthRegistry
import hr.foi.air.core.auth.SecureCreds
import hr.foi.air.otpstudent.ui.auth.PinUnlockActivity
import hr.foi.air.otpstudent.ui.auth.RegisterActivity
import hr.foi.air.otpstudent.ui.auth.LoginActivity

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ako je PIN omogućen stavi pin unlock
        if (shouldUsePinUnlock()) {
            startActivity(
                Intent(this, PinUnlockActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
            )
            finish()
            return
        }

        // Baci korisnika na start screen
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
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun shouldUsePinUnlock(): Boolean {
        val hasCreds =
            !SecureCreds.getEmail(this).isNullOrBlank() &&
                    !SecureCreds.getPass(this).isNullOrBlank()

        if (!hasCreds) return false

        return AuthRegistry.available().any {
            it.uiSpec().id in listOf("pin", "bio")
        }
    }
}
