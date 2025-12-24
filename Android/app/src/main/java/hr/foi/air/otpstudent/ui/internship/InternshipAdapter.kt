package hr.foi.air.otpstudent.ui.internship

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.domain.model.Internship

class InternshipAdapter(
    private val onItemClick: (Internship) -> Unit
) : ListAdapter<Internship, InternshipAdapter.InternshipViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Internship>() {
        override fun areItemsTheSame(oldItem: Internship, newItem: Internship) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Internship, newItem: Internship) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InternshipViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_job, parent, false)
        return InternshipViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: InternshipViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class InternshipViewHolder(
        itemView: View,
        private val onItemClick: (Internship) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvJobLocation)
        private val tvApplicants: TextView = itemView.findViewById(R.id.tvApplicants)
        private val imgLogo: ImageView = itemView.findViewById(R.id.imgCompanyLogo)
        private val btnApplied: MaterialButton = itemView.findViewById(R.id.btnApplied)

        fun bind(internship: Internship) {
            tvTitle.text = internship.title
            tvLocation.text = internship.location
            tvApplicants.text = "${internship.applicantsCount} studenata"
            imgLogo.setImageResource(R.drawable.ic_otp_logo_circle)
            btnApplied.visibility = if (internship.isApplied) View.VISIBLE else View.GONE
            itemView.setOnClickListener { onItemClick(internship) }
        }
    }
}
