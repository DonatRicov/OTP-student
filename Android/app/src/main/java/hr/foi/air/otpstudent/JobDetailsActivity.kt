package hr.foi.air.otpstudent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import hr.foi.air.otpstudent.model.Job // !! PROVJERITE JE LI OVO ISPRAVAN IMPORT !!



class JobDetailsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_details)


        val jobId = intent.getStringExtra("JOB_ID")


        if (jobId.isNullOrEmpty()) {
            Toast.makeText(this, "Greška: ID posla nije pronađen.", Toast.LENGTH_LONG).show()
            finish()
            return
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
        val tvRequirements: TextView = findViewById(R.id.tv_job_requirements) // ID iz novog XML-a
        val backArrow: ImageView = findViewById(R.id.iv_back_arrow) // ID iz novog XML-a


        tvTitle.text = job.title
        tvDescription.text = job.description


        if (job.requirements.isNotEmpty()) {
            val requirementsText = job.requirements.joinToString(separator = "\n") { "• $it" }
            tvRequirements.text = requirementsText
        } else {
            tvRequirements.text = "Nema specificiranih područja." // Ili sakrijte TextView
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
