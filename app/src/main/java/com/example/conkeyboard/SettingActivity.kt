package com.example.conkeyboard

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.conkeyboard.databinding.ActivitySettingBinding
import java.io.File
import java.io.FileNotFoundException

class SettingActivity : AppCompatActivity() {
    private lateinit var helper: ItemTouchHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pm = PreferenceManager()
        val useConList: ArrayList<ConInfo>? = pm.getConList(applicationContext, "use")
        if(useConList != null) {
            val bitmapList: ArrayList<Bitmap?> = ArrayList()
            for(i in useConList.indices) {
                bitmapList.add(loadPhoto(getPath(useConList[i].conNum), "title.jpg"))
            }
            val callback = ItemMoveCallback(useConList, pm, applicationContext)
            helper = ItemTouchHelper(callback)
            helper.attachToRecyclerView(binding.recyclerSetting)
            val adapter = SettingAdapter(bitmapList, useConList, helper)
            binding.recyclerSetting.adapter = adapter
        }
        binding.btnBack.setOnClickListener {
            finish()
        }
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

    private fun getPath(conNum: String): File {
        val directory = ContextWrapper(applicationContext).getDir("imageDir", Context.MODE_PRIVATE)
        val file = File(directory, conNum)
        if(!file.exists()) {
            file.mkdir()
        }
        return file
    }
}