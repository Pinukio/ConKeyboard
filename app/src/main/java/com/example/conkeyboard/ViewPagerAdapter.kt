package com.example.conkeyboard

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ViewPagerAdapter(private val useConList: List<ConInfo>, private val listener: OnItemClick, private val context: Context, private val isDarkMode: Boolean): RecyclerView.Adapter<ViewPagerAdapter.PagerViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        return PagerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_viewpager, parent, false))
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        val adapter = UseConAdapter(useConList[position].conName, useConList[position].conNum, listener, context, isDarkMode)
        holder.recyclerView.adapter = adapter
    }

    override fun getItemCount(): Int {
        return useConList.size
    }

    inner class PagerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recycler_cons_use)
    }
}