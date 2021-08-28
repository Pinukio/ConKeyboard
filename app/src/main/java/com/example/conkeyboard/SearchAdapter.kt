package com.example.conkeyboard

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.ceil

class SearchAdapter(private val listener: OnItemClick): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val itemList: ArrayList<ConData> = ArrayList()
    private val bitmapList: ArrayList<Bitmap?> = ArrayList()
    private val TYPE_ITEM = 0
    private val TYPE_LOADING = 1
    private var itemRemained = true

    override fun getItemViewType(position: Int): Int {
        return if(ceil(itemList.size.toFloat() / 3).toInt() == position) TYPE_LOADING
        else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            TYPE_ITEM -> {
                ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_search, parent, false))
            }
            else -> {
                LoadingViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_loading, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is ItemViewHolder) {
            val index: Int = position * 3
            val num: Int = itemList.size - index

            if(!itemRemained && num < 3) {
                for(i in 0 until num) {
                    setItem(holder, i, bitmapList[index + i], itemList[index + i].title, itemList[index + i].artist, itemList[index + i].conNum)
                }
                for(i in num until 3) {
                    setItem(holder, i, null, "", "", "")
                }
            }
            else {
                for(i in 0..2) {
                    setItem(holder, i, bitmapList[index + i], itemList[index + i].title, itemList[index + i].artist, itemList[index + i].conNum)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return if(itemRemained) ceil(itemList.size.toFloat() / 3).toInt() + 1
        else ceil(itemList.size.toFloat() / 3).toInt()
    }

    inner class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val view1: View = itemView.findViewById(R.id.con_1)
        val view2: View = itemView.findViewById(R.id.con_2)
        val view3: View = itemView.findViewById(R.id.con_3)

        val img1: ImageView = view1.findViewById(R.id.img_con)
        val img2: ImageView = view2.findViewById(R.id.img_con)
        val img3: ImageView = view3.findViewById(R.id.img_con)

        val title1: TextView = view1.findViewById(R.id.text_title)
        val title2: TextView = view2.findViewById(R.id.text_title)
        val title3: TextView = view3.findViewById(R.id.text_title)

        val artist1: TextView = view1.findViewById(R.id.text_artist)
        val artist2: TextView = view2.findViewById(R.id.text_artist)
        val artist3: TextView = view3.findViewById(R.id.text_artist)

    }

    inner class LoadingViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {}

    fun addItem(items: List<ConData>, bitmaps: List<Bitmap?>) {
        itemList.addAll(items)
        bitmapList.addAll(bitmaps)

    }

    fun resetItem() {
        itemList.clear()
        bitmapList.clear()
    }

    fun setItemRemained(flag: Boolean) {
        itemRemained = flag
    }

    private fun setItem(holder: ItemViewHolder, position: Int, bitmap: Bitmap?, titleText: String, artistText: String, conNum: String) {
        val imageViewList: List<ImageView> = listOf(holder.img1, holder.img2, holder.img3)
        val titleList: List<TextView> = listOf(holder.title1, holder.title2, holder.title3)
        val artistList: List<TextView> = listOf(holder.artist1, holder.artist2, holder.artist3)
        val viewList: List<View> = listOf(holder.view1, holder.view2, holder.view3)

        if(bitmap != null) {
            viewList[position].visibility = View.VISIBLE
            imageViewList[position].setImageBitmap(bitmap)
            titleList[position].text = titleText
            artistList[position].text = artistText
            viewList[position].setOnClickListener {
                listener.onClick(conNum)
            }
        }
        else {
            if(titleText.isEmpty()) {
                viewList[position].visibility = View.GONE
            }
            /*imageViewList[position].setImageBitmap(null)
            titleList[position].text = ""
            artistList[position].text = ""
            */

        }
    }
}