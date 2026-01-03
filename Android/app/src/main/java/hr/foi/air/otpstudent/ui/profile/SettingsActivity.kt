package hr.foi.air.otpstudent.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import hr.foi.air.core.auth.AuthRegistry
import hr.foi.air.core.auth.AuthResult
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.ui.profile.ProfileSetupActivity

class SettingsActivity : AppCompatActivity() {

    private fun pinPlugin() =
        AuthRegistry.available().firstOrNull { it.uiSpec().id == "pin" }

    private fun bioPlugin() =
        AuthRegistry.available().firstOrNull { it.uiSpec().id == "bio" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<android.view.View>(R.id.rowEditProfile).setOnClickListener {
            startActivity(Intent(this, ProfileSetupActivity::class.java))
        }

        findViewById<SwitchMaterial>(R.id.switchPush).setOnCheckedChangeListener { _, _ ->
            // TODO: spremi u SharedPreferences / Firestore
        }

        // Privacy policy
        findViewById<android.view.View>(R.id.rowPrivacyPolicy).setOnClickListener {
            // TODO: otvori WebView ili ekran s tekstom
        }

        // Delete account
        findViewById<android.view.View>(R.id.rowDeleteAccount).setOnClickListener {
            // TODO: dialog + delete acc flow
        }

        val switchBiometric = findViewById<SwitchMaterial>(R.id.switchBiometric)
        val bio = bioPlugin()

        switchBiometric.isChecked = bio?.isEnabled(this) == true

        switchBiometric.setOnCheckedChangeListener { _, checked ->
            val plugin = bioPlugin()
            if (plugin == null) {
                Toast.makeText(this, "Biometrija nije dostupna.", Toast.LENGTH_SHORT).show()
                switchBiometric.isChecked = false
                return@setOnCheckedChangeListener
            }

            if (checked) {
                plugin.configure(this) { result ->
                    runOnUiThread {
                        when (result) {
                            is AuthResult.Success -> {
                                plugin.setEnabled(this, true)
                                Toast.makeText(this, "Biometrija uključena", Toast.LENGTH_SHORT).show()
                            }
                            is AuthResult.Error -> {
                                plugin.setEnabled(this, false)
                                switchBiometric.isChecked = false
                                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                            }
                            AuthResult.Cancelled -> {
                                plugin.setEnabled(this, false)
                                switchBiometric.isChecked = false
                            }
                        }
                    }
                }

            } else {
                plugin.setEnabled(this, false)
                Toast.makeText(this, "Biometrija isključena", Toast.LENGTH_SHORT).show()
            }
        }

        val rowPin = findViewById<android.view.View>(R.id.rowPin)
        val tvPinTitle = findViewById<TextView>(R.id.tvPinTitle)
        val ivPinChevron = findViewById<android.view.View>(R.id.ivPinChevron)
        val switchPin = findViewById<SwitchMaterial>(R.id.switchPin)

        var ignorePinSwitchCallback = false

        fun renderPinRow() {
            val plugin = pinPlugin()
            if (plugin == null) {
                tvPinTitle.text = "PIN nije dostupan"
                rowPin.isEnabled = false
                rowPin.alpha = 0.5f
                ivPinChevron.visibility = android.view.View.VISIBLE
                switchPin.visibility = android.view.View.GONE
                return
            }

            val configured = plugin.isConfigured(this)
            val enabled = plugin.isEnabled(this)

            if (!configured) {
                tvPinTitle.text = "Postavi PIN"
                ivPinChevron.visibility = android.view.View.VISIBLE
                switchPin.visibility = android.view.View.GONE
            } else {
                tvPinTitle.text = "PIN"
                ivPinChevron.visibility = android.view.View.GONE
                switchPin.visibility = android.view.View.VISIBLE

                ignorePinSwitchCallback = true
                switchPin.isChecked = enabled
                ignorePinSwitchCallback = false
            }

            rowPin.isEnabled = true
            rowPin.alpha = 1f
        }

        renderPinRow()

        rowPin.setOnClickListener {
            val plugin = pinPlugin()
            if (plugin == null) {
                Toast.makeText(this, "PIN nije dostupan.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val configured = plugin.isConfigured(this)
            if (!configured) {
                plugin.configure(this) { result ->
                    runOnUiThread {
                        when (result) {
                            is AuthResult.Success -> {
                                plugin.setEnabled(this, true)
                                Toast.makeText(this, "PIN postavljen i uključen", Toast.LENGTH_SHORT).show()
                            }
                            is AuthResult.Error -> {
                                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                            }
                            AuthResult.Cancelled -> Unit
                        }
                        renderPinRow()
                    }
                }
            } else {
            }
        }

        switchPin.setOnCheckedChangeListener { _, checked ->
            if (ignorePinSwitchCallback) return@setOnCheckedChangeListener

            val plugin = pinPlugin() ?: run {
                Toast.makeText(this, "PIN nije dostupan.", Toast.LENGTH_SHORT).show()
                return@setOnCheckedChangeListener
            }

            plugin.setEnabled(this, checked)
            Toast.makeText(
                this,
                if (checked) "PIN uključen" else "PIN isključen",
                Toast.LENGTH_SHORT
            ).show()

            renderPinRow()
        }

    }
}
