package hr.foi.air.otpstudent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CvAdapter(
    private var cvList: List<CvDocument>,
    private val onItemClick: (CvDocument) -> Unit, // kliknuti red za otvaranje pdf
    private val onDeleteClick: (CvDocument) -> Unit // kliknuti smece za brisanje
) : RecyclerView.Adapter<CvAdapter.CvViewHolder>() {

    class CvViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvFileName)
        val tvDate: TextView = view.findViewById(R.id.tvUploaderName) // Reusing this ID for date/name
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
        val icon: ImageView = view.findViewById(R.id.imgPdfIcon) // Add ID to your XML icon
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CvViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cv, parent, false)
        return CvViewHolder(view)
    }

    override fun onBindViewHolder(holder: CvViewHolder, position: Int) {
        val cv = cvList[position]
        holder.tvName.text = cv.fileName

        // Format date nicely
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        holder.tvDate.text = sdf.format(Date(cv.timestamp))

        holder.itemView.setOnClickListener { onItemClick(cv) }
        holder.btnDelete.setOnClickListener { onDeleteClick(cv) }
    }

    override fun getItemCount() = cvList.size

    // Helper to update list efficiently
    fun updateData(newList: List<CvDocument>) {
        cvList = newList
        notifyDataSetChanged()
    }
}
