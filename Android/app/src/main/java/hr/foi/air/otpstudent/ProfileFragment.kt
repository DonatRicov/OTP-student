package hr.foi.air.otpstudent

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        val tvName     = view.findViewById<TextView>(R.id.tvProfileName)
        val tvSubtitle = view.findViewById<TextView>(R.id.tvProfileSubtitle)

        // Ime iz emaila (za sada; kasnije možeš iz Firestore-a)
        val nameFromEmail = user?.email
            ?.substringBefore("@")
            ?.replaceFirstChar { it.uppercase() }
            ?: "Korisnik"

        tvName.text = nameFromEmail
        tvSubtitle.text = "FOI student"

        // Odjava
        view.findViewById<LinearLayout>(R.id.rowLogout).setOnClickListener {
            auth.signOut()

            val ctx = requireActivity()
            val i = Intent(ctx, StartActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(i)
        }

        // Ostali redovi – za sada samo stubovi
        view.findViewById<LinearLayout>(R.id.rowCv).setOnClickListener {
            // TODO: otvori ekran za životopis
        }

        view.findViewById<LinearLayout>(R.id.rowPractice).setOnClickListener {
            // TODO: odrađene prakse
        }

        view.findViewById<LinearLayout>(R.id.rowJobs).setOnClickListener {
            // TODO: moji poslovi
        }

        view.findViewById<LinearLayout>(R.id.rowProfileSettings).setOnClickListener {
            // TODO: postavke profila
        }
    }
}
