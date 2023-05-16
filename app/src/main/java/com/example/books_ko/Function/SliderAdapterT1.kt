package com.example.books_ko.Function



import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.books_ko.Data.SliderItem
import com.example.books_ko.R
import com.smarteist.autoimageslider.SliderViewAdapter

class SliderAdapterT1(private val context: Context) :
    SliderViewAdapter<SliderAdapterT1.SliderAdapterVH>() {

    private var mSliderItems: MutableList<SliderItem> = ArrayList()

    fun renewItems(sliderItems: List<SliderItem>) {
        mSliderItems = ArrayList(sliderItems)
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int) {
        mSliderItems.removeAt(position)
        notifyDataSetChanged()
    }

    fun addItem(sliderItem: SliderItem) {
        mSliderItems.add(sliderItem)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup): SliderAdapterVH {
        val inflate =
            LayoutInflater.from(parent.context).inflate(R.layout.image_slider_layout_item, null)
        return SliderAdapterVH(inflate)
    }

    override fun onBindViewHolder(viewHolder: SliderAdapterVH, position: Int) {
        val sliderItem = mSliderItems[position]

        viewHolder.textViewDescription.text = sliderItem.description
        viewHolder.textViewDescription.setTextSize(16F)
        viewHolder.textViewDescription.setTextColor(Color.WHITE)
        Glide.with(viewHolder.itemView)
            .load(sliderItem.imageUrl)
            .fitCenter()
            .into(viewHolder.imageViewBackground)

        viewHolder.itemView.setOnClickListener {
            Toast.makeText(context, "This is item in position $position", Toast.LENGTH_SHORT)
                .show()
            Log.i("정보태그","imgUrl->${sliderItem.imageUrl}")
        }
    }

    override fun getCount(): Int {
        // slider view count could be dynamic size
        return mSliderItems.size
    }

    inner class SliderAdapterVH(itemView: View) : ViewHolder(itemView) {
        val imageViewBackground: ImageView = itemView.findViewById(R.id.iv_auto_image_slider)
        val imageGifContainer: ImageView = itemView.findViewById(R.id.iv_gif_container)
        val textViewDescription: TextView = itemView.findViewById(R.id.tv_auto_image_slider)
    }
}