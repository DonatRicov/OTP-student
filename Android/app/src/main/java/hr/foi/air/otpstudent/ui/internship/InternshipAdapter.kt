package hr.foi.air.otpstudent.ui.internship

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
import hr.foi.air.otpstudent.domain.model.Internship
import java.util.Date

class InternshipAdapter(
    private val onItemClick: (Internship) -> Unit
) : ListAdapter<Internship, InternshipAdapter.InternshipViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Internship>() {
        override fun areItemsTheSame(oldItem: Internship, newItem: Internship) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Internship, newItem: Internship) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InternshipViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_internship, parent, false)
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
        private val card: MaterialCardView = itemView.findViewById(R.id.cardJob)
        private val ivFavorite: ImageView = itemView.findViewById(R.id.ivFavorite)

        fun bind(internship: Internship) {
            tvTitle.text = internship.title
            tvLocation.text = internship.location

            tvApplicants.text = itemView.context.getString(
                R.string.internship_applicants_count,
                internship.applicantsCount
            )

            imgLogo.setImageResource(R.drawable.ic_otp_logo_circle)

            // zvjezdica se vidi samo kad je favorit
            ivFavorite.visibility = if (internship.isFavorite) View.VISIBLE else View.GONE

            val now = Date()
            val expiresDate = internship.expiresAt?.toDate()
            val soonMillis = 3 * 24 * 60 * 60 * 1000L
            val isSoonExpiring = expiresDate != null && (expiresDate.time - now.time) in 1..soonMillis

            when {
                internship.isApplied -> {
                    btnApplied.visibility = View.VISIBLE
                    btnApplied.setText(R.string.internship_status_applied)
                    btnApplied.backgroundTintList =
                        ColorStateList.valueOf(itemView.context.getColor(R.color.otp_green_dark))
                    card.setCardBackgroundColor(itemView.context.getColor(R.color.white))
                }

                isSoonExpiring -> {
                    btnApplied.visibility = View.VISIBLE
                    btnApplied.setText(R.string.internship_status_soon_expiring)


                    btnApplied.backgroundTintList =
                        ColorStateList.valueOf(itemView.context.getColor(R.color.otp_orange))

                    card.setCardBackgroundColor(itemView.context.getColor(R.color.white))
                }

                else -> {
                    btnApplied.visibility = View.GONE
                    card.setCardBackgroundColor(itemView.context.getColor(R.color.white))
                }
            }

            itemView.setOnClickListener { onItemClick(internship) }
        }
    }
}
