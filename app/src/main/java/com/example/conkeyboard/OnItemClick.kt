package com.example.conkeyboard

interface OnItemClick {
    fun onClick(position: Int) {}
    fun onClick(conNum: String) {}
}