package hr.foi.air.otpstudent.ui.internship

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.domain.model.Internship

class InternshipFulfilledFragment : Fragment(R.layout.fragment_internship_fullfiled) {

    private lateinit var db: FirebaseFirestore

    private lateinit var rvInternships: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var etSearch: TextInputEditText

    private lateinit var adapter: InternshipAdapter
    private var allInternships: List<Internship> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        rvInternships = view.findViewById(R.id.rvInternships)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        etSearch = view.findViewById(R.id.etSearch)

        // ✅ promjena: Activity -> Fragment navigation
        adapter = InternshipAdapter { internship ->
            findNavController().navigate(
                R.id.internshipDetailsFragment,
                bundleOf(InternshipDetailsFragment.ARG_INTERNSHIP_ID to internship.id)
            )
        }

        rvInternships.layoutManager = LinearLayoutManager(requireContext())
        rvInternships.adapter = adapter

        etSearch.addTextChangedListener { text ->
            applySearch(text?.toString().orEmpty())
        }

        loadAppliedInternshipsOnly()
    }

    private fun loadAppliedInternshipsOnly() {
        db.collection("internships")
            .orderBy("postedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val internships = snapshot.documents.map { doc ->
                    Internship(
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
                        isApplied = doc.getBoolean("isApplied") ?: false,
                        isFavorite = doc.getBoolean("isFavorite") ?: false,
                        description = doc.getString("description") ?: "",
                        applyUrl = doc.getString("applyUrl") ?: "",
                        requirements = (doc.get("requirements") as? List<String>) ?: emptyList()
                    )
                }

                allInternships = internships.filter { it.isApplied }
                applySearch(etSearch.text?.toString().orEmpty())
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Greška pri dohvaćanju praksi: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun applySearch(query: String) {
        val lower = query.lowercase().trim()

        val filtered = if (lower.isEmpty()) {
            allInternships
        } else {
            allInternships.filter { p ->
                p.title.lowercase().contains(lower) ||
                        p.location.lowercase().contains(lower) ||
                        p.company.lowercase().contains(lower)
            }
        }

        adapter.submitList(filtered)

        if (filtered.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvInternships.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvInternships.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        loadAppliedInternshipsOnly()
    }
}
