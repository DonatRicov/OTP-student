package hr.foi.air.auth.pin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class PinUnlockActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_UID = "extra_uid"
        const val EXTRA_USER_LABEL = "extra_user_label"
    }

    private val pin = StringBuilder()

    private fun finishOk() {
        setResult(
            RESULT_OK,
            Intent().putExtra(PinUnlockContract.EXTRA_RESULT, PinUnlockContract.RESULT_OK)
        )
        finish()
    }

    private fun finishNotYou() {
        setResult(
            RESULT_OK,
            Intent().putExtra(PinUnlockContract.EXTRA_RESULT, PinUnlockContract.RESULT_NOT_YOU)
        )
        finish()
    }

    private fun resolveUid(): String? {
        //iz intenta
        intent.getStringExtra(EXTRA_UID)?.let { if (it.isNotBlank()) return it }

        //firebase user
        FirebaseAuth.getInstance().currentUser?.uid?.let { if (it.isNotBlank()) return it }

        //fallback
        return PinStore.getLastUid(this)
    }

    private fun emailPrefix(email: String): String {
        val at = email.indexOf('@')
        return if (at > 0) email.substring(0, at) else email
    }


    private fun formatUserLabel(raw: String?): String? {
        val s = raw?.trim()
        if (s.isNullOrBlank()) return null
        return if (s.contains("@")) emailPrefix(s) else s
    }

    private fun resolveUserLabelFormatted(): String {
        // app moze poslati ime ili email
        val fromIntent = formatUserLabel(intent.getStringExtra(EXTRA_USER_LABEL))
        if (!fromIntent.isNullOrBlank()) return fromIntent

        //iz firebase usera ime
        val u = FirebaseAuth.getInstance().currentUser
        val fromName = formatUserLabel(u?.displayName)
        if (!fromName.isNullOrBlank()) return fromName

        val fromEmail = formatUserLabel(u?.email)
        if (!fromEmail.isNullOrBlank()) return fromEmail

        // fallback iz stora moze biti ime ili email
        val fromStore = formatUserLabel(PinStore.getLastUserLabel(this))
        if (!fromStore.isNullOrBlank()) return fromStore

        return "korisnika"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_unlock)

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val label = resolveUserLabelFormatted()
        tvTitle.text = "Unesite PIN za $label"

        val tvOtherLogin = findViewById<TextView>(R.id.tvOtherLogin)
        val tvForgotPin = findViewById<TextView>(R.id.tvForgotPin)

        val btnOk = findViewById<View>(R.id.keyOk)
        val btnDel = findViewById<View>(R.id.keyDel)

        val pinBoxes = listOf(
            findViewById<View>(R.id.pin1),
            findViewById<View>(R.id.pin2),
            findViewById<View>(R.id.pin3),
            findViewById<View>(R.id.pin4),
            findViewById<View>(R.id.pin5),
            findViewById<View>(R.id.pin6),
        )

        fun renderPin() {
            pinBoxes.forEachIndexed { index, v ->
                v.setBackgroundResource(
                    if (index < pin.length) R.drawable.pin_box_filled
                    else R.drawable.pin_box_empty
                )
            }

            btnOk.isEnabled = pin.length == 6
            btnOk.alpha = if (btnOk.isEnabled) 1f else 0.4f
        }

        fun resetPin() {
            pin.clear()
            renderPin()
        }

        fun verifyAndFinishIfComplete() {
            if (pin.length != 6) return

            val uid = resolveUid() ?: run {
                Toast.makeText(this@PinUnlockActivity, "PIN prijava nije dostupna.", Toast.LENGTH_LONG).show()
                resetPin()
                return
            }

            val ok = PinVerifier.verify(this@PinUnlockActivity, uid, pin.toString())

            if (ok) {
                //spremi zadnjeg korisnika kao formatirani label
                PinStore.setLastUid(this@PinUnlockActivity, uid)
                PinStore.setLastUserLabel(this@PinUnlockActivity, resolveUserLabelFormatted())
                finishOk()
            } else {
                Toast.makeText(this@PinUnlockActivity, "Neispravan PIN", Toast.LENGTH_LONG).show()
                resetPin()
            }
        }

        fun appendDigit(d: String) {
            if (pin.length >= 6) return
            pin.append(d)
            renderPin()
        }

        fun deleteDigit() {
            if (pin.isEmpty()) return
            pin.deleteCharAt(pin.length - 1)
            renderPin()
        }

        val digitButtons = listOf(
            findViewById<Button>(R.id.key1),
            findViewById<Button>(R.id.key2),
            findViewById<Button>(R.id.key3),
            findViewById<Button>(R.id.key4),
            findViewById<Button>(R.id.key5),
            findViewById<Button>(R.id.key6),
            findViewById<Button>(R.id.key7),
            findViewById<Button>(R.id.key8),
            findViewById<Button>(R.id.key9),
        )
        val btn0 = findViewById<Button>(R.id.key0)

        fun setupRandomKeypad() {
            val digits = (1..9).map { it.toString() }.shuffled()

            digitButtons.forEachIndexed { i, btn ->
                val d = digits[i]
                btn.text = d
                btn.setOnClickListener { appendDigit(d) }
            }

            btn0.text = "0"
            btn0.setOnClickListener { appendDigit("0") }
        }

        setupRandomKeypad()

        btnDel.setOnClickListener { deleteDigit() }
        btnOk.setOnClickListener { verifyAndFinishIfComplete() }

        tvOtherLogin.setOnClickListener { finishNotYou() }
        tvForgotPin.setOnClickListener { finishNotYou() }

        renderPin()

        onBackPressedDispatcher.addCallback(this) {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        pin.clear()
    }
}
