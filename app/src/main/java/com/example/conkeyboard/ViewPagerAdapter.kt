package com.example.conkeyboard

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ViewPagerAdapter(private val useConNumList: List<String>, private val listener: OnItemClick, private val context: Context): RecyclerView.Adapter<ViewPagerAdapter.PagerViewHolder>(){
    private val pm: PreferenceManager = PreferenceManager()
    private val useConNameList: List<List<String>> = pm.getConNameList(context, "use")!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        return PagerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_viewpager, parent, false))
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        val adapter = UseConAdapter(useConNameList[position], useConNumList[position], listener, context)
        holder.recyclerView.adapter = adapter
    }

    override fun getItemCount(): Int {
        return useConNumList.size
    }

    inner class PagerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recycler_cons_use)
    }
}