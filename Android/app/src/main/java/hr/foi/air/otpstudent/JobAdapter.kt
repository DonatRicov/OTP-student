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
import hr.foi.air.otpstudent.model.Job
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JobAdapter(
    private val onItemClick: (Job) -> Unit
) : ListAdapter<Job, JobAdapter.JobViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean =
            oldItem == newItem
    }


    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvJobLocation)
        private val imgLogo: ImageView = itemView.findViewById(R.id.imgCompanyLogo)
        private val tvApplicants: TextView = itemView.findViewById(R.id.tvApplicants)

        private val btnApplied: MaterialButton = itemView.findViewById(R.id.btnApplied)

        private val dateFormat = SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault())

        fun bind(job: Job) {
            tvTitle.text = job.title
            tvLocation.text = job.location
            tvApplicants.text = "${job.applicantsCount} studenata"
            imgLogo.setImageResource(R.drawable.ic_otp_logo_circle)
            btnApplied.visibility = if (job.isApplied) View.VISIBLE else View.GONE

            val now = Date()
            val expiresDate = job.expiresAt?.toDate()
            val soonMillis = 3 * 24 * 60 * 60 * 1000L

            when {
                job.isApplied -> {
                }
                job.isClosed || (expiresDate != null && expiresDate.before(now)) -> {
                }
                expiresDate != null && expiresDate.time - now.time < soonMillis -> {
                }
                else -> {
                }
            }

            itemView.setOnClickListener {
                onItemClick(job)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
