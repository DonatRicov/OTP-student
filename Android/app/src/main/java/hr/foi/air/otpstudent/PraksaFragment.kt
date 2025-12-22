package hr.foi.air.otpstudent

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PraksaFragment : Fragment(R.layout.fragment_praksa) {

    private lateinit var db: FirebaseFirestore

    private lateinit var rvJobs: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var etSearch: TextInputEditText
    private lateinit var tvAllPractices: TextView

    private lateinit var adapter: PracticeAdapter
    private var allPractices: List<Practice> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        rvJobs = view.findViewById(R.id.rvJobs)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        etSearch = view.findViewById(R.id.etSearch)
        tvAllPractices = view.findViewById(R.id.btnFavourites)

        tvAllPractices.setOnClickListener {
        }


        adapter = PracticeAdapter { practice ->
            val intent = PracticeDetailsActivity.newIntent(requireContext(), practice)
            startActivity(intent)
        }


        rvJobs.layoutManager = LinearLayoutManager(requireContext())
        rvJobs.adapter = adapter

        etSearch.addTextChangedListener { text ->
            applySearch(text?.toString().orEmpty())
        }

        loadPracticesWithUserStatus()
    }

    private fun applySearch(query: String) {
        val lower = query.lowercase().trim()

        val filtered = if (lower.isEmpty()) {
            allPractices
        } else {
            allPractices.filter { p ->
                p.title.lowercase().contains(lower) ||
                        p.location.lowercase().contains(lower) ||
                        p.company.lowercase().contains(lower)
            }
        }

        adapter.submitList(filtered)

        if (filtered.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvJobs.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvJobs.visibility = View.VISIBLE
        }
    }

    private fun loadPracticesWithUserStatus() {
        db.collection("practice")
            .orderBy("postedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val practices = snapshot.documents.map { doc ->
                    Practice(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        company = doc.getString("company") ?: "",
                        location = doc.getString("location") ?: "",
                        hourlyRate = doc.getDouble("hourlyRate") ?: 0.0,
                        hourlyRateMax = doc.getDouble("hourlyRateMax") ?: 0.0,
                        applicantsCount = (doc.getLong("applicantsCount") ?: 0L).toInt(),
                        postedAt = doc.getTimestamp("postedAt"),
                        expiresAt = doc.getTimestamp("expiresAt"),
                        isClosed = doc.getBoolean("isClosed") ?: false,
                        isApplied = false,
                        isFavorite = false,
                        description = doc.getString("description") ?: "",
                        applyUrl = doc.getString("applyUrl") ?: "",
                        requirements = (doc.get("requirements") as? List<String>) ?: emptyList()
                    )
                }

                allPractices = practices
                applySearch(etSearch.text?.toString().orEmpty())
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Greška pri dohvaćanju praksi: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                applySearch(etSearch.text?.toString().orEmpty())
            }
    }

    private fun markExpiredPracticesLocally() {
        val now = com.google.firebase.Timestamp.now()

        val updated = allPractices.map { practice ->
            val exp = practice.expiresAt
            if (exp != null && exp <= now && !practice.isClosed) {
                db.collection("practice")
                    .document(practice.id)
                    .update("isClosed", true)
                practice.copy(isClosed = true)
            } else {
                practice
            }
        }

        allPractices = updated
    }


    override fun onResume() {
        super.onResume()
        loadPracticesWithUserStatus()
    }
}
