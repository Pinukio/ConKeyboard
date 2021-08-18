package com.example.conkeyboard

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView

class ConFieldAdapter(private val bitmapArrayList: ArrayList<Bitmap?>, private val listener: OnItemClick, private val w: Int): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var itemList: ArrayList<View> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder = MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_con, parent, false))
        itemList.add(viewHolder.getItemView())
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = (holder as MyViewHolder).conImg
        val index = position - 1
        item.setPadding(w)
        item.setOnClickListener {
            if(position == 0)
                listener.onClick(-2)
            else
                listener.onClick(position)
        }
        if(position == 0) {
            item.setImageResource(R.drawable.settings_black)
        }
        else {
            if(bitmapArrayList[index] != null) {
                item.setImageBitmap(bitmapArrayList[index])
            }
        }
    }

    override fun getItemCount(): Int {
        return bitmapArrayList.size + 1
    }
    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val conImg: ImageView = itemView.findViewById(R.id.img_con)

        fun getItemView(): View {
            return itemView
        }
    }
    fun setTitleConColor(position: Int, color: Int) {
        (itemList[position] as CardView).setCardBackgroundColor(color)
    }
}