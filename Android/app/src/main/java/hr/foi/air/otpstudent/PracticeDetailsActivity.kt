package hr.foi.air.otpstudent

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Locale

class PracticeDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "practice_id"
        const val EXTRA_TITLE = "practice_title"
        const val EXTRA_COMPANY = "practice_company"
        const val EXTRA_COMPENSATION = "practice_compensation"
        const val EXTRA_LOCATION = "practice_location"
        const val EXTRA_DURATION = "practice_duration"
        const val EXTRA_DESCRIPTION = "practice_description"
        const val EXTRA_AREAS = "practice_areas"
        const val EXTRA_APPLY_URL = "practice_apply_url"

        fun newIntent(context: Context, p: Practice): Intent {
            val compensation = when {
                p.hourlyRateMax > 0.0 && p.hourlyRateMax != p.hourlyRate ->
                    "${p.hourlyRate}–${p.hourlyRateMax} €/h"
                p.hourlyRate > 0.0 ->
                    "${p.hourlyRate} €/h"
                else -> "-"
            }

            val durationOrExpires = p.expiresAt?.toDate()?.let { date ->
                "do " + SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault()).format(date)
            } ?: "-"

            val areas = if (p.requirements.isNotEmpty()) {
                p.requirements.joinToString(", ")
            } else {
                "-"
            }

            return Intent(context, PracticeDetailsActivity::class.java).apply {
                putExtra(EXTRA_ID, p.id)
                putExtra(EXTRA_TITLE, p.title)
                putExtra(EXTRA_COMPANY, p.company)
                putExtra(EXTRA_COMPENSATION, compensation)
                putExtra(EXTRA_LOCATION, p.location)
                putExtra(EXTRA_DURATION, durationOrExpires)
                putExtra(EXTRA_DESCRIPTION, p.description)
                putExtra(EXTRA_AREAS, areas)
                putExtra(EXTRA_APPLY_URL, p.applyUrl)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practice_details)

        // (možeš kasnije obrisati)
        Toast.makeText(this, "Otvoreni detalji prakse", Toast.LENGTH_SHORT).show()

        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }

        val tvTitle = findViewById<TextView>(R.id.tvPracticeTitle)
        val tvCompany = findViewById<TextView>(R.id.tvCompanyName)
        val tvComp = findViewById<TextView>(R.id.tvCompensationValue)
        val tvLoc = findViewById<TextView>(R.id.tvLocationValue)
        val tvDur = findViewById<TextView>(R.id.tvDurationValue)
        val tvDesc = findViewById<TextView>(R.id.tvDescription)
        val tvAreas = findViewById<TextView>(R.id.tvAreas)
        val btnApply = findViewById<MaterialButton>(R.id.btnApply)

        tvTitle.text = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "-" }
        tvCompany.text = intent.getStringExtra(EXTRA_COMPANY).orEmpty().ifBlank { "-" }
        tvComp.text = intent.getStringExtra(EXTRA_COMPENSATION).orEmpty().ifBlank { "-" }
        tvLoc.text = intent.getStringExtra(EXTRA_LOCATION).orEmpty().ifBlank { "-" }
        tvDur.text = intent.getStringExtra(EXTRA_DURATION).orEmpty().ifBlank { "-" }
        tvDesc.text = intent.getStringExtra(EXTRA_DESCRIPTION).orEmpty().ifBlank { "-" }
        tvAreas.text = intent.getStringExtra(EXTRA_AREAS).orEmpty().ifBlank { "-" }

        val url = intent.getStringExtra(EXTRA_APPLY_URL)

        btnApply.setOnClickListener {
            if (url.isNullOrBlank()) {
                Toast.makeText(this, "Link za prijavu nije dostupan.", Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
    }
}
