package hr.foi.air.otpstudent

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        val user = FirebaseAuth.getInstance().currentUser
        Log.d("FirebaseTest", "Firebase connected. User: $user")
    }
}
