package com.example.conkeyboard

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView

class ShopAdapter(private val bitmapArrayList: ArrayList<Bitmap?>, private val titleArrayList: ArrayList<String>, private val artistArrayList: ArrayList<String>, private val listener: OnItemClick, private val conNumList: List<String>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
private val itemList: ArrayList<View> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder = MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_shop, parent, false))
        itemList.add(viewHolder.getItemView())
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemList[position]
        if(bitmapArrayList[position] != null) {
            (holder as MyViewHolder).conImg.setImageBitmap(bitmapArrayList[position])
            val titleText = titleArrayList[position]
            val artistText = artistArrayList[position]
            if(titleText.length < 10) holder.title.text = titleText
            else holder.title.text = titleText.substring(0, 9)

            if(artistText.length < 10) holder.artist.text = artistText
            else holder.artist.text = artistText.substring(0, 9)
        }
        item.setOnClickListener {
            listener.onClick(conNumList[position])
        }
    }

    override fun getItemCount(): Int {
        return bitmapArrayList.size
    }
    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val conImg: ImageView = itemView.findViewById(R.id.img_con)
        val title: TextView = itemView.findViewById(R.id.text_title)
        val artist: TextView = itemView.findViewById(R.id.text_artist)

        fun getItemView(): View {
            return itemView
        }
    }

    fun setData(bitmapList: ArrayList<Bitmap?>, titleList: ArrayList<String>, artistList: ArrayList<String>) {
        bitmapArrayList.clear()
        bitmapArrayList.addAll(bitmapList)
        titleArrayList.clear()
        titleArrayList.addAll(titleList)
        artistArrayList.clear()
        artistArrayList.addAll(artistList)
    }
}