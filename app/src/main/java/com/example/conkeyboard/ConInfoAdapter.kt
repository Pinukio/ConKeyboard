package com.example.conkeyboard

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import pl.droidsonroids.gif.GifDrawable
import java.lang.Exception
import kotlin.math.ceil

class ConInfoAdapter(private val imgList: ArrayList<Any?>, private val conNameList_: ArrayList<String>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
private val conNameList: List<String> = conNameList_.subList(1, conNameList_.size)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_info_con, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item1 = (holder as MyViewHolder).conImg1
        val item2 = holder.conImg2
        val item3 = holder.conImg3
        val itemList: List<ImageView> = listOf(item1, item2, item3)
        val index: Int = position * 3
        val num: Int = imgList.size - index
        if(num < 3) {
            for(i in 0 until num) {
                val img = imgList[index + i]
                val conName = conNameList[index + i]
                if(img != null) {
                    try {
                        when(conName.substring(conName.length - 3, conName.length)) {
                            "png", "jpg" -> itemList[i].setImageBitmap(img as Bitmap)
                            "gif" -> itemList[i].setImageDrawable(img as GifDrawable)
                        }
                    }
                    catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        else {
            for(i in 0..2) {
                val img = imgList[index + i]
                val conName = conNameList[index + i]
                if(img != null) {
                    try {
                        when(conName.substring(conName.length - 3, conName.length)) {
                            "png", "jpg" -> itemList[i].setImageBitmap(img as Bitmap)
                            "gif" -> itemList[i].setImageDrawable(img as GifDrawable)
                        }
                    }
                    catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return ceil(imgList.size.toFloat() / 3).toInt()
    }

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val conImg1: ImageView = itemView.findViewById(R.id.img_con_1)
        val conImg2: ImageView = itemView.findViewById(R.id.img_con_2)
        val conImg3: ImageView = itemView.findViewById(R.id.img_con_3)
    }
}