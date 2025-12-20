package hr.foi.air.otpstudent

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton

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
        const val EXTRA_LOGO_URL = "practice_logo_url"

        private const val PREFS = "practice_favorites"

        fun newIntent(
            context: Context,
            id: String?,
            title: String?,
            company: String?,
            compensation: String?,
            location: String?,
            duration: String?,
            description: String?,
            areas: String?,
            applyUrl: String?,
            logoUrl: String? = null
        ): Intent {
            return Intent(context, PracticeDetailsActivity::class.java).apply {
                putExtra(EXTRA_ID, id)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_COMPANY, company)
                putExtra(EXTRA_COMPENSATION, compensation)
                putExtra(EXTRA_LOCATION, location)
                putExtra(EXTRA_DURATION, duration)
                putExtra(EXTRA_DESCRIPTION, description)
                putExtra(EXTRA_AREAS, areas)
                putExtra(EXTRA_APPLY_URL, applyUrl)
                putExtra(EXTRA_LOGO_URL, logoUrl)
            }
        }
    }

    private lateinit var ivFavorite: ImageView
    private var favKey: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practice_details)

        val ivBack = findViewById<ImageView>(R.id.ivBack)
        ivFavorite = findViewById(R.id.ivFavorite)
        val ivLogo = findViewById<ImageView>(R.id.ivCompanyLogo)

        val tvTitle = findViewById<TextView>(R.id.tvPracticeTitle)
        val tvCompany = findViewById<TextView>(R.id.tvCompanyName)

        val tvComp = findViewById<TextView>(R.id.tvCompensationValue)
        val tvLoc = findViewById<TextView>(R.id.tvLocationValue)
        val tvDur = findViewById<TextView>(R.id.tvDurationValue)

        val tvDesc = findViewById<TextView>(R.id.tvDescription)
        val tvAreas = findViewById<TextView>(R.id.tvAreas)

        val btnApply = findViewById<MaterialButton>(R.id.btnApply)

        ivBack.setOnClickListener { finish() }

        val id = intent.getStringExtra(EXTRA_ID)
        val title = intent.getStringExtra(EXTRA_TITLE)
        val company = intent.getStringExtra(EXTRA_COMPANY)
        val compensation = intent.getStringExtra(EXTRA_COMPENSATION)
        val location = intent.getStringExtra(EXTRA_LOCATION)
        val duration = intent.getStringExtra(EXTRA_DURATION)
        val description = intent.getStringExtra(EXTRA_DESCRIPTION)
        val areas = intent.getStringExtra(EXTRA_AREAS)
        val applyUrl = intent.getStringExtra(EXTRA_APPLY_URL)
        val logoUrl = intent.getStringExtra(EXTRA_LOGO_URL)

        // ključ za favorite (ako nema id, fallback)
        favKey = (id?.takeIf { it.isNotBlank() }
            ?: "${title.orEmpty()}_${company.orEmpty()}_${applyUrl.orEmpty()}").ifBlank { "unknown_practice" }

        tvTitle.text = title.orEmpty().ifBlank { "-" }
        tvCompany.text = company.orEmpty().ifBlank { "-" }

        tvComp.text = compensation.orEmpty().ifBlank { "-" }
        tvLoc.text = location.orEmpty().ifBlank { "-" }
        tvDur.text = duration.orEmpty().ifBlank { "-" }

        tvDesc.text = description.orEmpty().ifBlank { "-" }
        tvAreas.text = areas.orEmpty().ifBlank { "-" }

        if (!logoUrl.isNullOrBlank()) {
            Glide.with(this).load(logoUrl).into(ivLogo)
        }

        // Favorite toggle (lokalno)
        renderFavorite()
        ivFavorite.setOnClickListener {
            toggleFavorite()
            renderFavorite()
        }

        btnApply.setOnClickListener {
            if (applyUrl.isNullOrBlank()) {
                Toast.makeText(this, "Link za prijavu nije dostupan.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            openUrl(applyUrl)
        }
    }

    private fun prefs() = getSharedPreferences(PREFS, MODE_PRIVATE)

    private fun isFavorite(): Boolean = prefs().getBoolean(favKey, false)

    private fun toggleFavorite() {
        prefs().edit().putBoolean(favKey, !isFavorite()).apply()
    }

    private fun renderFavorite() {
        ivFavorite.setImageResource(
            if (isFavorite()) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
        ivFavorite.imageTintList = getColorStateList(R.color.otp_green).takeIf { runCatching { true }.getOrDefault(false) }
        // Ako nemaš otp_green u projektu, gore možeš obrisati ovu liniju (tint je već u XML-u).
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) {
            Toast.makeText(this, "Ne mogu otvoriti link.", Toast.LENGTH_SHORT).show()
        }
    }
}
