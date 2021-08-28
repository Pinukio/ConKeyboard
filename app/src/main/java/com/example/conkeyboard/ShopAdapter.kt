package com.example.conkeyboard

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ShopAdapter(private val bitmapList: ArrayList<Bitmap?>,  private val listener: OnItemClick, private val conList: ArrayList<ConInfo>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder = MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_shop, parent, false))
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(bitmapList[position] != null) {
            (holder as MyViewHolder).conImg.setImageBitmap(bitmapList[position])
            val titleText = conList[position].title
            val artistText = conList[position].artist
            if(titleText.length < 10) holder.title.text = titleText
            else holder.title.text = titleText.substring(0, 9)
            holder.title.text = titleText

            if(artistText.length < 10) holder.artist.text = artistText
            else holder.artist.text = artistText.substring(0, 9)
            holder.artist.text = artistText
        }
        (holder as MyViewHolder).view.setOnClickListener {
            listener.onClick(conList[position].conNum)
        }
    }

    override fun getItemCount(): Int {
        return bitmapList.size
    }
    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val conImg: ImageView = itemView.findViewById(R.id.img_con)
        val title: TextView = itemView.findViewById(R.id.text_title)
        val artist: TextView = itemView.findViewById(R.id.text_artist)
        val view: View = itemView.findViewById(R.id.view)
    }

    fun setData(bitmapList: ArrayList<Bitmap?>, conList_: ArrayList<ConInfo>) {
        bitmapList.clear()
        bitmapList.addAll(bitmapList)
        conList.clear()
        conList.addAll(conList_)
    }
}