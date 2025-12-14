package hr.foi.air.otpstudent

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hr.foi.air.otpstudent.model.Job
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.firebase.firestore.FieldValue

class JobDetailsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var applyUrl: String? = null

    private var isFavorite = false
    private lateinit var btnFavorite: ImageView
    private lateinit var userId: String

    private var jobId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_details)

        // nadi korisnika
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Morate biti prijavljeni.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        userId = currentUser.uid

        // id posla tj job
        jobId = intent.getStringExtra("JOB_ID")
        val id = jobId
        if (id.isNullOrEmpty()) {
            showError("Greška: ID posla nije pronađen.")
            return
        }

        // Back button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // Favorite button
        btnFavorite = findViewById(R.id.btnFavorite)
        checkIfFavorite(userId, id, btnFavorite)
        btnFavorite.setOnClickListener {
            toggleFavorite(userId, id, btnFavorite)
        }

        // Apply button
        val btnApply: AppCompatButton = findViewById(R.id.btn_apply)
        btnApply.isEnabled = false
        btnApply.setOnClickListener {
            val url = applyUrl
            if (url.isNullOrBlank()) {
                Toast.makeText(this, "Link za prijavu nije dostupan.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val appliedRef = db.collection("users")
                .document(userId)
                .collection("applied")
                .document(id) // <-- koristi provjereni jobId

            appliedRef.set(mapOf("createdAt" to FieldValue.serverTimestamp()))
                .addOnSuccessListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Greška pri spremanju prijave: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }




        // ucitaj info o poslu
        loadJobDetails(id)
    }

    private fun loadJobDetails(jobId: String) {
        db.collection("jobs").document(jobId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val job = document.toObject(Job::class.java)
                    if (job != null) {
                        applyUrl = job.applyUrl
                        val btnApply: AppCompatButton = findViewById(R.id.btn_apply)
                        btnApply.isEnabled = !applyUrl.isNullOrBlank()
                        populateUi(job)
                    } else {
                        showError("Greška pri čitanju podataka o poslu.")
                    }
                } else {
                    showError("Posao nije pronađen u bazi.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("JobDetailsActivity", "Greška pri dohvaćanju posla", exception)
                showError("Greška pri spajanju na bazu: ${exception.message}")
            }
    }

    private fun populateUi(job: Job) {
        val tvTitle: TextView = findViewById(R.id.tv_job_title_details)
        val tvDescription: TextView = findViewById(R.id.tv_description_details)
        val tvRequirements: TextView = findViewById(R.id.tv_job_requirements)

        val tvHourlyRate: TextView = findViewById(R.id.tvHourlyRate)
        val tvLocation: TextView = findViewById(R.id.tvLocation)
        val tvExpires: TextView = findViewById(R.id.tvExpires)
        val tvCompany: TextView = findViewById(R.id.tvCompany)

        tvTitle.text = job.title
        tvDescription.text = job.description

        if (job.requirements.isNotEmpty()) {
            tvRequirements.text = job.requirements.joinToString("\n") { "• $it" }
        } else {
            tvRequirements.text = "Nema specificiranih područja."
        }

        val rateText = if (job.hourlyRateMax > 0) {
            String.format(Locale.getDefault(), "%.1f–%.1f €/h", job.hourlyRate, job.hourlyRateMax)
        } else {
            String.format(Locale.getDefault(), "%.1f €/h", job.hourlyRate)
        }
        tvHourlyRate.text = rateText

        tvLocation.text = job.location.ifBlank { "—" }

        val expires = job.expiresAt?.toDate()
        tvExpires.text = if (expires != null) {
            SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault()).format(expires)
        } else {
            "—"
        }

        tvCompany.text = if (job.company.isNotBlank()) job.company else "OTP Banka"
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }

    // favoriti

    private fun checkIfFavorite(userId: String, jobId: String, icon: ImageView) {
        db.collection("users")
            .document(userId)
            .collection("favorites")
            .document(jobId)
            .get()
            .addOnSuccessListener {
                isFavorite = it.exists()
                updateFavoriteIcon(icon)
            }
            .addOnFailureListener { e ->
                Log.e("JobDetailsActivity", "Greška pri provjeri favorita", e)
            }
    }

    private fun toggleFavorite(userId: String, jobId: String, icon: ImageView) {
        val favRef = db.collection("users")
            .document(userId)
            .collection("favorites")
            .document(jobId)

        if (isFavorite) {
            favRef.delete()
                .addOnSuccessListener {
                    isFavorite = false
                    updateFavoriteIcon(icon)
                    Toast.makeText(this, "Uklonjeno iz favorita", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("JobDetailsActivity", "Greška pri brisanju favorita", e)
                    Toast.makeText(this, "Greška pri uklanjanju iz favorita", Toast.LENGTH_SHORT).show()
                }
        } else {
            favRef.set(mapOf("createdAt" to System.currentTimeMillis()))
                .addOnSuccessListener {
                    isFavorite = true
                    updateFavoriteIcon(icon)
                    Toast.makeText(this, "Dodano u favorite", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("JobDetailsActivity", "Greška pri spremanju favorita", e)
                    Toast.makeText(this, "Greška pri dodavanju u favorite", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateFavoriteIcon(icon: ImageView) {
        icon.setImageResource(
            if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline
        )
    }
}
