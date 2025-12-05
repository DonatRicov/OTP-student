package hr.foi.air.otpstudent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.model.Job
import java.text.SimpleDateFormat
import java.util.*

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
        private val tvMeta: TextView = itemView.findViewById(R.id.tvJobMeta)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatusBadge)
        private val imgLogo: ImageView = itemView.findViewById(R.id.imgCompanyLogo)

        private val dateFormat = SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault())

        fun bind(job: Job) {
            tvTitle.text = job.title
            tvLocation.text = job.location

            // bitne info
            val satnica = if (job.hourlyRateMax > 0) {
                String.format(Locale.getDefault(), "%.1f–%.1f €/h", job.hourlyRate, job.hourlyRateMax)
            } else {
                String.format(Locale.getDefault(), "%.1f €/h", job.hourlyRate)
            }
            tvMeta.text = "${job.applicantsCount} studenata • $satnica"

            // logo dodati (Lana)
            //imgLogo.setImageResource(R.drawable.ic_otp_logo_circle)

            // status
            val now = Date()
            val expiresDate = job.expiresAt?.toDate()
            val soonMillis = 3 * 24 * 60 * 60 * 1000L   // 3 dana za sad

            // Ovo zakomentirano dodati (Lana)
            when {
                job.isApplied -> {
                    tvStatus.visibility = View.VISIBLE
                    tvStatus.text = "Prijavljeno"
                    //tvStatus.setBackgroundResource(R.drawable.bg_badge_applied)
                }
                job.isClosed || (expiresDate != null && expiresDate.before(now)) -> {
                    tvStatus.visibility = View.VISIBLE
                    tvStatus.text = "Isteklo"
                    //tvStatus.setBackgroundResource(R.drawable.bg_badge_soon)
                }
                expiresDate != null && expiresDate.time - now.time < soonMillis -> {
                    tvStatus.visibility = View.VISIBLE
                    tvStatus.text = "Uskoro istječe"
                   // tvStatus.setBackgroundResource(R.drawable.bg_badge_soon)
                }
                else -> {
                    tvStatus.visibility = View.GONE
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
