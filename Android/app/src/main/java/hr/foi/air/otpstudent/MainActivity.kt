package hr.foi.air.otpstudent

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->

            bottomNav.visibility = if (destination.id == R.id.chatbotFragment) View.GONE else View.VISIBLE

            if (destination.id == R.id.nav_jobs_favorites || destination.id == R.id.nav_jobs_add_favorites) {
                bottomNav.menu.findItem(R.id.nav_poslovi)?.isChecked = true
            }


            if (destination.id == R.id.internshipDetailsFragment) {
                bottomNav.menu.findItem(R.id.nav_praksa)?.isChecked = true
            }
        }

        bottomNav.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.nav_poslovi) {
                navController.popBackStack(R.id.nav_poslovi, false)
            }
            if (item.itemId == R.id.nav_praksa) {
                navController.popBackStack(R.id.nav_praksa, false)
            }
        }
    }
}
