package hr.foi.air.otpstudent

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import hr.foi.air.otpstudent.data.auth.QuickLoginManager
import hr.foi.air.otpstudent.data.auth.AppLockStore

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

            val hideBottomNavDestinations = setOf(
                R.id.chatbotFragment,
                R.id.redeemedRewardsFragment,
                R.id.redeemedRewardDetailsFragment,
                R.id.rewardDetailsFragment,
                R.id.rewardRedeemedFragment,
                R.id.internshipListFragment,
                R.id.mentorshipDetailsFragment,
                R.id.internshipDetailsFragment
            )


            bottomNav.visibility =
                if (destination.id in hideBottomNavDestinations) View.GONE else View.VISIBLE

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

    private val LOCK_THRESHOLD_MS = 5_000L

    override fun onStart() {
        super.onStart()

        QuickLoginManager.enforceUserScope(this)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            startActivity(
                Intent(this, StartActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
            )
            finish()
            return
        }

        if (AppLockStore.shouldLock(this, LOCK_THRESHOLD_MS)) {
            startActivity(
                Intent(this, StartActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
            )
            finish()
            return
        }

        user.getIdToken(true)
            .addOnFailureListener {
                auth.signOut()
                QuickLoginManager.resetQuickLogin(this)
                startActivity(
                    Intent(this, StartActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                )
                finish()
            }
    }
}
