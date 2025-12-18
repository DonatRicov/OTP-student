package hr.foi.air.otpstudent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class PracticeAdapter(
    private val onItemClick: (Practice) -> Unit
) : ListAdapter<Practice, PracticeAdapter.PracticeViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Practice>() {
        override fun areItemsTheSame(oldItem: Practice, newItem: Practice): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Practice, newItem: Practice): Boolean =
            oldItem == newItem
    }

    inner class PracticeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvJobLocation)
        private val tvApplicants: TextView = itemView.findViewById(R.id.tvApplicants)
        private val imgLogo: ImageView = itemView.findViewById(R.id.imgCompanyLogo)
        private val btnApplied: MaterialButton = itemView.findViewById(R.id.btnApplied)

        fun bind(practice: Practice) {
            tvTitle.text = practice.title
            tvLocation.text = practice.location
            tvApplicants.text = "${practice.applicantsCount} studenata"
            imgLogo.setImageResource(R.drawable.ic_otp_logo_circle)
            btnApplied.visibility = if (practice.isApplied) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                onItemClick(practice)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PracticeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job, parent, false) // ili item_practice ako ga napraviš
        return PracticeViewHolder(view)
    }

    override fun onBindViewHolder(holder: PracticeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
