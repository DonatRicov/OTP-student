package hr.foi.air.otpstudent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView

data class NewsItem(
    @DrawableRes val imageRes: Int,
    val url: String
)

class NewsPagerAdapter(
    private val items: List<NewsItem>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<NewsPagerAdapter.NewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    override fun getItemCount(): Int = items.size

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgNews: ImageView = itemView.findViewById(R.id.imgNews)

        fun bind(item: NewsItem, onItemClick: (String) -> Unit) {
            imgNews.setImageResource(item.imageRes)
            itemView.setOnClickListener {
                onItemClick(item.url)
            }
        }
    }
}
