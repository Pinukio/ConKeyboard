package com.example.conkeyboard

import android.graphics.Bitmap

interface OnItemClick {
    fun onClick(position: Int) {}
    fun onClick(conNum: String) {}
    fun onConClick(conNum: String, conName: String) {}
}