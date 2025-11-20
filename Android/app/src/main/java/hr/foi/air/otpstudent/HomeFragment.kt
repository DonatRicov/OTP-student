package hr.foi.air.otpstudent

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        Log.d("FirebaseTest", "Firebase connected. User: $user")

        // NOTICE: We use 'view.findViewById' now!
        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)

        val nameFromEmail = user?.email
            ?.substringBefore("@")
            ?.replaceFirstChar { it.uppercase() }
            ?: ""

        tvWelcome.text = if (nameFromEmail.isNotEmpty())
            "Dobrodošli $nameFromEmail!"
        else
            "Dobrodošli!"

        setupNewsCarousel(view)
    }

    private fun setupNewsCarousel(view: View) {
        val pager = view.findViewById<ViewPager2>(R.id.newsPager)

        val items = listOf(
            NewsItem(R.drawable.news_image1, "https://studentski.hr/studenti/financije/otp-e-indeks-najbolji-student-kad-su-financije-u-pitanju-42366"),
            NewsItem(R.drawable.news_image2, "https://www.otpbanka.hr/gradani/studentski-kredit-za-obrazovanje")
        )

        pager.adapter = NewsPagerAdapter(items) { url ->
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
}
