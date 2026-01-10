package hr.foi.air.otpstudent

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hr.foi.air.otpstudent.di.AppModule
import hr.foi.air.otpstudent.domain.model.Internship
import hr.foi.air.otpstudent.domain.model.Job
import hr.foi.air.otpstudent.ui.internship.InternshipDetailsActivity
import hr.foi.air.otpstudent.ui.jobs.JobDetailsActivity
import kotlinx.coroutines.launch
import android.widget.ImageButton
import androidx.navigation.fragment.findNavController
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var pager: ViewPager2
    private lateinit var indicatorLayout: LinearLayout
    private lateinit var items: List<NewsItem>

    private var autoScrollTimer: CountDownTimer? = null
    private val slideDuration = 5000L

    private val internshipRepo = AppModule.internshipRepository
    private val jobRepo = AppModule.jobRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.containerRandomJob).visibility = View.GONE
        view.findViewById<View>(R.id.containerRandomInternship).visibility = View.GONE

        val user = FirebaseAuth.getInstance().currentUser
        Log.d("FirebaseTest", "Firebase connected. User: $user")

        pager = view.findViewById(R.id.newsPager)
        indicatorLayout = view.findViewById(R.id.newsIndicatorLayout)

        setWelcomeTextFromDbOrEmail(view)
        setupNewsCarousel()
        loadRandomJobAndInternship(view)

        view.findViewById<ImageButton>(R.id.btnChatbot).setOnClickListener {
            findNavController().navigate(R.id.chatbotFragment)
        }
    }

    private fun setWelcomeTextFromDbOrEmail(view: View) {
        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
        val user = FirebaseAuth.getInstance().currentUser

        val emailFallback = user?.email
            ?.substringBefore("@")
            ?.replaceFirstChar { it.uppercase() }
            ?: ""

        val uid = user?.uid
        if (uid.isNullOrBlank()) {
            tvWelcome.text = if (emailFallback.isNotEmpty()) "Dobrodošli $emailFallback!" else "Dobrodošli!"
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val firstNameFromDb =
                    doc.getString("firstName")?.trim()?.takeIf { it.isNotBlank() }
                        ?: doc.getString("fullName")
                            ?.trim()
                            ?.split("\\s+".toRegex())
                            ?.firstOrNull()


                val displayName = when {
                    !firstNameFromDb.isNullOrEmpty() -> firstNameFromDb
                    emailFallback.isNotEmpty() -> emailFallback
                    else -> ""
                }

                tvWelcome.text =
                    if (displayName.isNotEmpty()) "Dobrodošli $displayName!"
                    else "Dobrodošli!"

            }

            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Failed to read users/$uid", e)
                tvWelcome.text = if (emailFallback.isNotEmpty()) "Dobrodošli $emailFallback!" else "Dobrodošli!"
            }

    }

    private fun setupNewsCarousel() {
        items = listOf(
            NewsItem(
                R.drawable.news_image1,
                "https://studentski.hr/studenti/financije/otp-e-indeks-najbolji-student-kad-su-financije-u-pitanju-42366"
            ),
            NewsItem(
                R.drawable.news_image2,
                "https://www.otpbanka.hr/gradani/studentski-kredit-za-obrazovanje"
            )
        )

        pager.adapter = NewsPagerAdapter(items) { url ->
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        setupIndicators(items.size)
        setCurrentIndicator(0)

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })
    }


    private fun loadRandomJobAndInternship(view: View) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        val jobContainer = view.findViewById<View>(R.id.containerRandomJob)
        val jobInclude = view.findViewById<View>(R.id.includeRandomJob)

        val internshipContainer = view.findViewById<View>(R.id.containerRandomInternship)
        val internshipInclude = view.findViewById<View>(R.id.includeRandomInternship)

        lifecycleScope.launch {
            try {
                val jobs = jobRepo.getJobsForUser(uid)
                    .filter { !it.isClosed && !it.isApplied }

                val randomJob = jobs.randomOrNull()

                if (randomJob == null) {
                    jobContainer.visibility = View.GONE
                } else {
                    jobContainer.visibility = View.VISIBLE
                    bindJob(jobInclude, randomJob)

                    jobInclude.setOnClickListener {
                        val intent = Intent(requireContext(), JobDetailsActivity::class.java)
                        intent.putExtra("JOB_ID", randomJob.id)
                        startActivity(intent)
                    }


                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Job load error", e)
                jobContainer.visibility = View.GONE
            }
        }

        lifecycleScope.launch {
            try {
                val internships = internshipRepo.getInternshipsForUser(uid)
                    .filter { !it.isClosed && !it.isApplied }

                val randomInternship = internships.randomOrNull()

                if (randomInternship == null) {
                    internshipContainer.visibility = View.GONE
                } else {
                    internshipContainer.visibility = View.VISIBLE
                    bindInternship(internshipInclude, randomInternship)

                    internshipInclude.setOnClickListener {
                        startActivity(
                            InternshipDetailsActivity.newIntent(requireContext(), randomInternship.id)
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Internship load error", e)
                internshipContainer.visibility = View.GONE
            }
        }
    }

    private fun bindInternship(root: View, item: Internship) {
        root.findViewById<TextView>(R.id.tvJobTitle).text = item.title
        root.findViewById<TextView>(R.id.tvJobLocation).text = item.location
        root.findViewById<TextView>(R.id.tvApplicants).text = "${item.applicantsCount} studenata"

        root.findViewById<MaterialButton>(R.id.btnApplied).visibility = View.GONE

        root.findViewById<View>(R.id.metaRow)?.visibility = View.VISIBLE
    }

    private fun bindJob(root: View, item: Job) {
        root.findViewById<TextView>(R.id.tvJobTitle).text = item.title
        root.findViewById<TextView>(R.id.tvJobLocation).text = item.location

        root.findViewById<View>(R.id.metaRow)?.visibility = View.VISIBLE
        root.findViewById<TextView>(R.id.tvApplicants).text = "${item.applicantsCount} studenata"

        root.findViewById<MaterialButton>(R.id.btnApplied).visibility = View.GONE
    }


    private fun setupIndicators(count: Int) {
        indicatorLayout.removeAllViews()

        val barWidth = resources.getDimensionPixelSize(R.dimen.news_indicator_width)
        val barHeight = resources.getDimensionPixelSize(R.dimen.news_indicator_height)
        val margin = resources.getDimensionPixelSize(R.dimen.news_indicator_margin)

        repeat(count) { index ->
            val progressBar = ProgressBar(
                requireContext(),
                null,
                android.R.attr.progressBarStyleHorizontal
            ).apply {
                layoutParams = LinearLayout.LayoutParams(barWidth, barHeight).also { lp ->
                    if (index > 0) lp.marginStart = margin
                    lp.marginEnd = margin
                }
                max = slideDuration.toInt()
                progress = 0
                progressDrawable = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.news_indicator_progress
                )
                background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.news_indicator_background
                )
            }
            indicatorLayout.addView(progressBar)
        }
    }

    private fun setCurrentIndicator(position: Int) {
        for (i in 0 until indicatorLayout.childCount) {
            val bar = indicatorLayout.getChildAt(i) as ProgressBar
            bar.progress = 0
            bar.alpha = if (i == position) 1f else 0.4f
        }
        startAutoScrollTimer(position)
    }

    private fun startAutoScrollTimer(position: Int) {
        autoScrollTimer?.cancel()

        val indicator = indicatorLayout.getChildAt(position) as ProgressBar

        autoScrollTimer = object : CountDownTimer(slideDuration, 50) {
            override fun onTick(millisUntilFinished: Long) {
                val elapsed = (slideDuration - millisUntilFinished).toInt()
                indicator.progress = elapsed
            }

            override fun onFinish() {
                val next = if (pager.currentItem + 1 < items.size) pager.currentItem + 1 else 0
                pager.setCurrentItem(next, true)
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        autoScrollTimer?.cancel()
    }
}
