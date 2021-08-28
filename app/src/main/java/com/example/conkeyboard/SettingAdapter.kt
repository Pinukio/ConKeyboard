package com.example.conkeyboard

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList

class SettingAdapter(private val bitmapList: ArrayList<Bitmap?>, private val useConList: ArrayList<ConInfo>, private val helper: ItemTouchHelper): RecyclerView.Adapter<SettingAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_setting, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val bitmap = bitmapList[position]
        if(bitmap != null) {
            holder.imageView.setImageBitmap(bitmap)
            holder.title.text = useConList[position].title
            holder.btn.setOnTouchListener { v, event ->
                if(event.actionMasked == MotionEvent.ACTION_DOWN) {
                    helper.startDrag(holder)
                }
                true
            }

        }
    }

    override fun getItemCount(): Int {
        return bitmapList.size
    }

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById<ImageView>(R.id.img_con)
        val title = itemView.findViewById<TextView>(R.id.text_title)
        val btn = itemView.findViewById<View>(R.id.btn_hold)
    }

    fun setItem(fromPosition: Int, toPosition: Int) {
        Collections.swap(bitmapList, fromPosition, toPosition)
    }


}