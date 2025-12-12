package hr.foi.air.otpstudent

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.firestore.FirebaseFirestore
import hr.foi.air.otpstudent.model.Job

class JobDetailsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var applyUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_details)

        val jobId = intent.getStringExtra("JOB_ID")

        if (jobId.isNullOrEmpty()) {
            Toast.makeText(this, "Greška: ID posla nije pronađen.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val btnApply: AppCompatButton = findViewById(R.id.btn_apply)
        btnApply.isEnabled = false

        btnApply.setOnClickListener {
            val url = applyUrl
            if (url.isNullOrBlank()) {
                Toast.makeText(this, "Link za prijavu nije dostupan.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        loadJobDetails(jobId)
    }

    private fun loadJobDetails(jobId: String) {
        db.collection("jobs").document(jobId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val job = document.toObject(Job::class.java)
                    if (job != null) {
                        applyUrl = job.applyUrl
                        findViewById<AppCompatButton>(R.id.btn_apply).isEnabled = !applyUrl.isNullOrBlank()
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
        val backArrow: ImageView = findViewById(R.id.iv_back_arrow)

        tvTitle.text = job.title
        tvDescription.text = job.description

        if (job.requirements.isNotEmpty()) {
            val requirementsText = job.requirements.joinToString(separator = "\n") { "• $it" }
            tvRequirements.text = requirementsText
        } else {
            tvRequirements.text = "Nema specificiranih područja."
        }

        backArrow.setOnClickListener {
            finish()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }
}
