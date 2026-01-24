package hr.foi.air.otpstudent.ui.jobs

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.domain.model.Job
import java.util.Date

class JobAdapter(
    private val onItemClick: (Job) -> Unit
) : ListAdapter<Job, JobAdapter.JobViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean = oldItem == newItem
    }

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvJobLocation)
        private val imgLogo: ImageView = itemView.findViewById(R.id.imgCompanyLogo)
        private val tvApplicants: TextView = itemView.findViewById(R.id.tvApplicants)
        private val btnApplied: MaterialButton = itemView.findViewById(R.id.btnApplied)

        private val card: MaterialCardView = itemView.findViewById(R.id.cardJob)
        private val ivFavorite: ImageView = itemView.findViewById(R.id.ivFavorite)

        fun bind(job: Job) {
            tvTitle.text = job.title
            tvLocation.text = job.location

            tvApplicants.text = itemView.context.getString(
                R.string.jobs_applicants_count,
                job.applicantsCount
            )

            imgLogo.setImageResource(R.drawable.ic_otp_logo_circle)


            ivFavorite.setImageResource(R.drawable.ic_favorite_filled)
            ivFavorite.visibility = if (job.isFavorite) View.VISIBLE else View.GONE

            val now = Date()
            val expiresDate = job.expiresAt?.toDate()
            val soonMillis = 3L * 24 * 60 * 60 * 1000
            val isSoonExpiring = expiresDate != null && (expiresDate.time - now.time) in 1..soonMillis

            when {
                job.isApplied -> {
                    btnApplied.visibility = View.VISIBLE
                    btnApplied.setText(R.string.jobs_status_applied)
                    btnApplied.backgroundTintList =
                        ColorStateList.valueOf(itemView.context.getColor(R.color.otp_green_dark))


                    card.setCardBackgroundColor(itemView.context.getColor(R.color.jobs_card_applied_bg))
                }

                isSoonExpiring -> {
                    btnApplied.visibility = View.VISIBLE
                    btnApplied.setText(R.string.jobs_status_soon_expiring)


                    btnApplied.backgroundTintList =
                        ColorStateList.valueOf(itemView.context.getColor(R.color.jobs_status_soon_bg))

                    // TODO: boja pozadine kartice za "uskoro ističe"
                    card.setCardBackgroundColor(itemView.context.getColor(R.color.jobs_status_soon_bg))
                }

                else -> {
                    btnApplied.visibility = View.GONE
                    // default bg (može ostati bijelo)
                    card.setCardBackgroundColor(itemView.context.getColor(R.color.white))
                }
            }

            itemView.setOnClickListener { onItemClick(job) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_job, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
