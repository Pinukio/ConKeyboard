package com.example.conkeyboard

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import pl.droidsonroids.gif.GifDrawable
import java.io.File
import java.io.FileNotFoundException
import kotlin.math.ceil

class UseConAdapter(useConNameList: List<String>, private val conNum: String, private val listener: OnItemClick, private val context: Context): RecyclerView.Adapter<UseConAdapter.MyViewHolder>() {
    private val conName: List<String> = useConNameList.subList(1, useConNameList.size)
    private val imgList: ArrayList<Any?> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_con_use, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val imageViewList: List<ImageView> = listOf(holder.conImg1, holder.conImg2, holder.conImg3, holder.conImg4)
        val index = position * 4
        val num = conName.size - index
        if(num < 4) {
            for(i in 0 until num) {
                val name = conName[index + i]
                when(name.substring(name.length - 3, name.length)) {
                    "png" -> {
                        val bitmap = loadPhoto(getPath(conNum, context), name)
                        imgList.add(bitmap)
                        if(bitmap != null)
                            imageViewList[i].setImageBitmap(bitmap)
                    }
                    "gif" -> {
                        val gif = loadGIF(getPath(conNum, context), conName[index + i])
                        imgList.add(gif)
                        if(gif != null)
                            imageViewList[i].setImageDrawable(gif)
                    }
                }
                imageViewList[i].setOnClickListener {
                    val img = imgList[index + i]
                    if(img != null)
                        listener.onConClick(conNum, conName[index + i])
                }
            }
        }
        else {
            for(i in 0..3) {
                val name = conName[index + i]
                when(name.substring(name.length - 3, name.length)) {
                    "png" -> {
                        val bitmap = loadPhoto(getPath(conNum, context), name)
                        imgList.add(bitmap)
                        if(bitmap != null)
                            imageViewList[i].setImageBitmap(bitmap)
                    }
                    "gif" -> {
                        val gif = loadGIF(getPath(conNum, context), conName[index + i])
                        imgList.add(gif)
                        if(gif != null)
                            imageViewList[i].setImageDrawable(gif)
                    }
                }
                imageViewList[i].setOnClickListener {
                    val img = imgList[index + i]
                    if(img != null)
                        listener.onConClick(conNum, conName[index + i])
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return ceil(conName.size.toFloat() / 4).toInt()
    }

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val conImg1: ImageView = itemView.findViewById(R.id.img_con_1)
        val conImg2: ImageView = itemView.findViewById(R.id.img_con_2)
        val conImg3: ImageView = itemView.findViewById(R.id.img_con_3)
        val conImg4: ImageView = itemView.findViewById(R.id.img_con_4)
    }

    private fun loadPhoto(path: File, name: String): Bitmap? {
        return try {
            val f = File(path, name)
            val b = f.readBytes()
            BitmapFactory.decodeByteArray(b, 0, b.size)

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    private fun loadGIF(path: File, name: String): GifDrawable? {
        return try {
            val f = File(path, name)
            val b = f.readBytes()
            GifDrawable(b)
        }
        catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    private fun getPath(conNum: String, context: Context): File {
        val directory = ContextWrapper(context).getDir("imageDir", Context.MODE_PRIVATE)
        val file = File(directory, conNum)
        if(!file.exists()) {
            file.mkdir()
        }
        return file
    }
}