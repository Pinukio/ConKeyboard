package com.example.conkeyboard

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList

class ItemMoveCallback(private val useConList: ArrayList<ConInfo>, private val pm: PreferenceManager, private val context: Context): ItemTouchHelper.Callback() {
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition
        /*Collections.swap(useConNumList, fromPosition, toPosition)
        Collections.swap(useConNameList, fromPosition, toPosition)
        Collections.swap(useConTitleList, fromPosition, toPosition)
        Collections.swap(useConArtistList, fromPosition, toPosition)*/
        Collections.swap(useConList, fromPosition, toPosition)
        val adapter = recyclerView.adapter!! as SettingAdapter
        adapter.setItem(fromPosition, toPosition)
        adapter.notifyItemMoved(fromPosition, toPosition)
        /*pm.setConNumList(context, useConNumList, "use")
        pm.setConNameList(context, useConNameList, "use")
        pm.setConTitleList(context, useConTitleList, "use")
        pm.setConArtistList(context, useConArtistList, "use")*/
        pm.setConList(context, useConList, "use")
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }



}