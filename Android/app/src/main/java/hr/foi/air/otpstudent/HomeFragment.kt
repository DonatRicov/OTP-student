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
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var pager: ViewPager2
    private lateinit var indicatorLayout: LinearLayout
    private lateinit var items: List<NewsItem>

    private var autoScrollTimer: CountDownTimer? = null
    private val slideDuration = 5000L   // 5 sekundi

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        Log.d("FirebaseTest", "Firebase connected. User: $user")

        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)

        val nameFromEmail = user?.email
            ?.substringBefore("@")
            ?.replaceFirstChar { it.uppercase() }
            ?: ""

        tvWelcome.text = if (nameFromEmail.isNotEmpty())
            "Dobrodošli $nameFromEmail!"
        else
            "Dobrodošli!"

        pager = view.findViewById(R.id.newsPager)
        indicatorLayout = view.findViewById(R.id.newsIndicatorLayout)

        setupNewsCarousel()
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
                    if (index > 0) {
                        lp.marginStart = margin
                    }
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
                val next = if (pager.currentItem + 1 < items.size) {
                    pager.currentItem + 1
                } else {
                    0
                }
                pager.setCurrentItem(next, true)
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        autoScrollTimer?.cancel()
    }
}
