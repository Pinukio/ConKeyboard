package com.example.conkeyboard

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView

class ConsAdapter(private val imgList: ArrayList<Bitmap>, private val listener: OnItemClick, private val w: Int): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_con, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = (holder as MyViewHolder).conImg
        item.setImageBitmap(imgList[position])
        item.setPadding(w)
        item.setOnClickListener {
            listener.onClick(position)
        }
    }

    override fun getItemCount(): Int {
        return imgList.size
    }
    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val conImg: ImageView = itemView.findViewById(R.id.img_con)
    }
}