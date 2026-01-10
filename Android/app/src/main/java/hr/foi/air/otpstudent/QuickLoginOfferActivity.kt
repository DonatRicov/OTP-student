package hr.foi.air.otpstudent

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import hr.foi.air.core.auth.AuthRegistry
import hr.foi.air.core.auth.AuthResult

class QuickLoginOfferActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_login_offer)

        val container = findViewById<LinearLayout>(R.id.methodsContainer)
        val btnSkip = findViewById<MaterialButton>(R.id.btnSkip)

        val plugins = AuthRegistry.available()

        plugins.forEach { plugin ->
            val spec = plugin.uiSpec()

            val btn = MaterialButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    resources.getDimensionPixelSize(R.dimen.auth_btn_height)
                ).also {
                    it.topMargin = resources.getDimensionPixelSize(R.dimen.auth_btn_margin_top)
                }

                text = "Omogući: ${spec.title}"
                isAllCaps = false

                setOnClickListener {
                    plugin.configure(this@QuickLoginOfferActivity) { result ->
                        when (result) {
                            is AuthResult.Success -> {
                                plugin.setEnabled(this@QuickLoginOfferActivity, true)
                                goToHome()
                            }
                            is AuthResult.Error -> {
                            }
                            AuthResult.Cancelled -> Unit
                        }
                    }
                }
            }

            container.addView(btn)
        }

        btnSkip.setOnClickListener {
            goToHome()
        }
    }

    private fun goToHome() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        )
        finish()
    }
}
