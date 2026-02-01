package hr.foi.air.otpstudent.ui.jobs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.di.AppModule
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class JobDetailsActivity : AppCompatActivity() {

    private lateinit var btnFavoriteToggle: MaterialButton
    private lateinit var btnApply: MaterialButton

    private val viewModel: JobDetailsViewModel by lazy {
        ViewModelProvider(this, JobDetailsVmFactory())[JobDetailsViewModel::class.java]
    }

    private val df = SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_details)

        // secondary header
        val headerContainer = findViewById<FrameLayout>(R.id.headerContainer)
        val headerView = LayoutInflater.from(this)
            .inflate(R.layout.view_header_secondary, headerContainer, true)

        val jobId = intent.getStringExtra("JOB_ID")
        if (jobId.isNullOrBlank()) {
            Toast.makeText(this, "Greška: ID posla nije pronađen.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // btnBack i btnChatbot su u secondary headeru
        headerView.findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }

        headerView.findViewById<View>(R.id.btnChatbot)?.visibility = View.VISIBLE

        btnFavoriteToggle = findViewById(R.id.btn_favorite_toggle)
        btnApply = findViewById(R.id.btn_apply)

        btnFavoriteToggle.setOnClickListener { viewModel.toggleFavorite() }
        btnApply.setOnClickListener { viewModel.onApplyClicked() }

        lifecycleScope.launch {
            viewModel.state.collectLatest { state -> render(state) }
        }

        lifecycleScope.launch {
            viewModel.effects.collectLatest { eff ->
                when (eff) {
                    is JobDetailsEffect.OpenUrl ->
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(eff.url)))

                    is JobDetailsEffect.ShowMessage ->
                        Toast.makeText(this@JobDetailsActivity, eff.message, Toast.LENGTH_LONG).show()

                    JobDetailsEffect.Close -> finish()
                }
            }
        }

        viewModel.load(jobId)
    }

    private fun render(state: JobDetailsUiState) {
        val job = state.job

        state.error?.let {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }

        btnApply.isEnabled = job?.applyUrl?.isNotBlank() == true
        if (job == null) return

        findViewById<TextView>(R.id.tv_job_title_details).text = job.title
        findViewById<TextView>(R.id.tvCompany).text = job.company.ifBlank { "OTP Banka" }
        findViewById<TextView>(R.id.tv_description_details).text = job.description

        val tvReq: TextView = findViewById(R.id.tv_job_requirements)
        tvReq.text =
            if (job.requirements.isNotEmpty()) job.requirements.joinToString("\n") { "• $it" }
            else "SSS, bacc, mag"

        val rateText = if (job.hourlyRateMax > 0) {
            String.format(Locale.getDefault(), "%.1f–%.1f €/h", job.hourlyRate, job.hourlyRateMax)
        } else {
            String.format(Locale.getDefault(), "%.1f €/h", job.hourlyRate)
        }
        findViewById<TextView>(R.id.tvHourlyRate).text = rateText
        findViewById<TextView>(R.id.tvLocation).text = job.location.ifBlank { "—" }

        val expires = job.expiresAt?.toDate()
        findViewById<TextView>(R.id.tvExpires).text =
            if (expires != null) df.format(expires) else "—"

        renderFavoriteButton(job.isFavorite)
    }

    private fun renderFavoriteButton(isFavorite: Boolean) {
        if (isFavorite) {
            btnFavoriteToggle.setText(R.string.job_details_remove_favorite)
            btnFavoriteToggle.backgroundTintList =
                android.content.res.ColorStateList.valueOf(getColor(R.color.otp_green_dark))
            btnFavoriteToggle.setTextColor(getColor(android.R.color.white))
            btnFavoriteToggle.strokeWidth = 0
        } else {
            btnFavoriteToggle.setText(R.string.job_details_add_favorite)
            btnFavoriteToggle.backgroundTintList =
                android.content.res.ColorStateList.valueOf(getColor(android.R.color.transparent))
            btnFavoriteToggle.setTextColor(getColor(R.color.otp_green_dark))
            btnFavoriteToggle.strokeWidth = 1
            btnFavoriteToggle.strokeColor =
                android.content.res.ColorStateList.valueOf(getColor(R.color.otp_green_dark))
        }
    }

    private class JobDetailsVmFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(JobDetailsViewModel::class.java)) {
                val repo = AppModule.jobRepository
                val userIdProvider = { FirebaseAuth.getInstance().currentUser?.uid }
                @Suppress("UNCHECKED_CAST")
                return JobDetailsViewModel(repo, userIdProvider) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
