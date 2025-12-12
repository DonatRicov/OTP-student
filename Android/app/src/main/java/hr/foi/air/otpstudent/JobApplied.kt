package hr.foi.air.otpstudent

import android.view.View
import android.widget.CheckBox
import com.google.android.material.button.MaterialButton
import hr.foi.air.otpstudent.model.Job

class JobAppliedUiBinder(
    private val onAppliedChanged: (jobId: String, applied: Boolean) -> Unit
) {
    fun bind(job: Job, cbJob: CheckBox, btnApplied: MaterialButton) {

        cbJob.setOnCheckedChangeListener(null)

        cbJob.isChecked = job.isApplied
        cbJob.visibility = if (job.isApplied) View.GONE else View.VISIBLE
        btnApplied.visibility = if (job.isApplied) View.VISIBLE else View.GONE

        cbJob.setOnCheckedChangeListener { _, checked ->
            onAppliedChanged(job.id, checked)
        }

        btnApplied.setOnClickListener {
            onAppliedChanged(job.id, false)
        }
    }
}
