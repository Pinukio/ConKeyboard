 package com.example.conkeyboard

import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.conkeyboard.databinding.ActivityInfoConBinding
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.lang.Exception
import java.net.URL
import java.net.URLConnection

class ConInfoActivity: AppCompatActivity() {
    private val baList: ArrayList<ByteArray?> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityInfoConBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnClose.setOnClickListener {
            finish()
        }

        val intent = intent
        val conNum: String? = intent.getStringExtra("conNum")
        val url: String? = intent.getStringExtra("url")
        val pm = PreferenceManager()
        //val haveConNumList: ArrayList<String>? = pm.getConNumList(applicationContext, "have")
        val haveConList: ArrayList<ConInfo>? = pm.getConList(applicationContext, "have")
        var isConDownloaded = false
        var conNumIndex: Int = 0
        setThumbnailSize(binding.thumbnail)

        if(conNum != null && haveConList != null) {
            //isConDownloaded = conNum in haveConList
            haveConList.forEachIndexed { index, conInfo ->
                if(conInfo.conNum == conNum) {
                    isConDownloaded = true
                    conNumIndex = index
                }
            }
        }
        if(isConDownloaded) {
            /*val haveConNameList: ArrayList<ArrayList<String>> = pm.getConNameList(applicationContext, "have")!!
            val haveConTitleList: ArrayList<String> = pm.getConTitleList(applicationContext, "have")!!
            val haveConArtistList: ArrayList<String> = pm.getConArtistList(applicationContext, "have")!!*/

            binding.thumbnail.setImageBitmap(loadPhoto(getPath(conNum!!), "title.jpg"))
            //binding.textTitle.text = haveConTitleList[conNumIndex]
            binding.textTitle.text = haveConList!![conNumIndex].title
            //binding.textArtist.text = haveConArtistList[conNumIndex]
            binding.textArtist.text = haveConList[conNumIndex].artist

            val imgList: ArrayList<Any?> = ArrayList()
            //val conName = haveConNameList[(conNumIndex)]
            val conName = haveConList[conNumIndex].conName
            for(i in 1 until conName.size) {
                when(conName[i].substring(conName[i].length - 3, conName[i].length)) {
                    "png", "jpg" -> {
                        imgList.add(loadPhoto(getPath(conNum), conName[i]))
                    }
                    "gif" -> {
                        imgList.add(loadGIF(getPath(conNum), conName[i]))
                    }
                }
            }

            val adapter = ConInfoAdapter(imgList, conName)
            binding.recyclerConContent.adapter = adapter

            val alertDialog: AlertDialog = this.let {
                val builder = AlertDialog.Builder(it, R.style.AlertDialogStyle)
                builder.apply {
                    setPositiveButton("확인", DialogInterface.OnClickListener { dialog, _ ->
                        for(i in conName.indices) {
                            removeFile(getPath(conNum), conName[i])
                        }
                        //removeFile(getPath(conNum), "title.jpg")
                        removeFile(ContextWrapper(applicationContext).getDir("imageDir", Context.MODE_PRIVATE), conNum)
                        removeManager(pm, conNumIndex, conNum)
                        binding.fieldHaveCon.visibility = View.GONE
                        binding.btnDownload.visibility = View.VISIBLE
                        Toast.makeText(applicationContext, "제거되었습니다.", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    })
                    setNegativeButton("취소", DialogInterface.OnClickListener { dialog, _ ->
                        dialog.cancel()
                    })
                }
                builder.setMessage("디시콘을 제거하시겠습니까?")
                builder.create()
            }
            binding.btnRemove.setOnClickListener {
                alertDialog.show()
            }
            binding.btnDownload.setOnClickListener {
                it.visibility = View.GONE
                binding.fieldProgressDownload.visibility = View.VISIBLE

                val photoName: ArrayList<String> = ArrayList()
                for(i in baList.indices) {
                    if(baList[i] != null) {
                        saveImage(baList[i]!!, getPath(conNum), conName[i])
                        photoName.add(conName[i])
                    }
                    binding.progressbar.progress = (i + 1) / baList.size
                    binding.textProgress.text = "${i + 1}/${baList.size}"
                }
                downloadManager(pm, haveConList[conNumIndex].title, conNum, photoName, haveConList[conNumIndex].artist)
                binding.fieldProgressDownload.visibility = View.GONE
                binding.fieldHaveCon.visibility = View.VISIBLE
                //baList.clear()
                Toast.makeText(applicationContext, "다운로드가 완료되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        else if(conNum != null) {
            binding.fieldHaveCon.visibility = View.GONE
            binding.btnDownload.visibility = View.VISIBLE

            val retrofit = RetrofitConnection(applicationContext, url!!).server
            val json = JsonForOneCon(conNum)
            val oneConCall: Call<ConData> = retrofit.getOneCon(json)
            oneConCall.enqueue(object : Callback<ConData> {
                override fun onResponse(call: Call<ConData>, response: Response<ConData>) {
                    val data = response.body()
                    if(data != null) {
                        val urlList: ArrayList<URL> = ArrayList()
                        for(i in data.photo.indices) {
                            urlList.add(URL(data.photo[i]))
                        }
                        val byteArrayList: ArrayList<ByteArray?> = ConvertToByteArrayTask().execute(urlList).get()
                        val imgList: ArrayList<Any?> = ArrayList()
                        val photoName: ArrayList<String> = ArrayList()
                        for(i in byteArrayList.indices) {
                            try {
                                val ba = byteArrayList[i]!!
                                val inputStream = ByteArrayInputStream(ba)
                                val extension: String = URLConnection.guessContentTypeFromStream(inputStream).split("/")[1] //image/png, image/gif
                                val conName = when(i) {
                                    0 -> "title.jpg"
                                    else -> "${i}.${extension}"
                                }
                                inputStream.close()
                                when(conName.substring(conName.length - 3, conName.length)) {
                                    "png", "jpg" -> {
                                        val bitmap = BitmapFactory.decodeByteArray(ba, 0, ba.size)
                                        imgList.add(bitmap)
                                    }
                                    "gif" -> {
                                        val drawable = GifDrawable(ba)
                                        imgList.add(drawable)
                                    }
                                }
                                photoName.add(conName)
                            }
                            catch (e: Exception) {
                                if(imgList.size == i)
                                    imgList.add(null)
                            }
                        }
                        val thumb = imgList[0]
                        if(thumb != null)
                            binding.thumbnail.setImageBitmap(thumb as Bitmap)
                        binding.textTitle.text = data.title
                        binding.textArtist.text = data.artist
                        val adapter = ConInfoAdapter(imgList, photoName)
                        binding.recyclerConContent.adapter = adapter

                        binding.btnDownload.setOnClickListener {
                            it.visibility = View.GONE
                            binding.fieldProgressDownload.visibility = View.VISIBLE
                            for(i in data.photo.indices) {
                                val ba = byteArrayList[i]
                                if(ba != null) {
                                    saveImage(ba, getPath(conNum), photoName[i])
                                    //photoName.add(photoName[i])
                                }
                                binding.progressbar.progress = (i + 1) / data.photo.size
                                binding.textProgress.text = "${i + 1}/${data.photo.size}"
                            }
                            downloadManager(pm, data.title, data.conNum, photoName, data.artist)
                            binding.fieldProgressDownload.visibility = View.GONE
                            binding.fieldHaveCon.visibility = View.VISIBLE
                            Toast.makeText(applicationContext, "다운로드가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        }

                        val alertDialog: AlertDialog = this.let {
                            val builder = AlertDialog.Builder(this@ConInfoActivity  , R.style.AlertDialogStyle)
                            builder.apply {
                                setPositiveButton("확인", DialogInterface.OnClickListener { dialog, _ ->
                                    for(i in photoName.indices) {
                                        removeFile(getPath(conNum), photoName[i])
                                    }
                                    //removeFile(getPath(conNum), "title.jpg")
                                    removeFile(ContextWrapper(applicationContext).getDir("imageDir", Context.MODE_PRIVATE), conNum)
                                    removeManager(pm, 0, conNum)
                                    binding.fieldHaveCon.visibility = View.GONE
                                    binding.btnDownload.visibility = View.VISIBLE
                                    Toast.makeText(applicationContext, "제거되었습니다.", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                })
                                setNegativeButton("취소", DialogInterface.OnClickListener { dialog, _ ->
                                    dialog.cancel()
                                })
                            }
                            builder.setMessage("디시콘을 제거하시겠습니까?")
                            builder.create()
                        }

                        binding.btnRemove.setOnClickListener {
                            /*for(i in photoName.indices) {
                                removeFile(getPath(data.conNum), photoName[i])
                            }
                            removeFile(ContextWrapper(applicationContext).getDir("imageDir", Context.MODE_PRIVATE), conNum)
                            removeManager(pm, 0, conNum)
                            binding.fieldHaveCon.visibility = View.GONE
                            binding.btnDownload.visibility = View.VISIBLE*/
                            alertDialog.show()
                        }
                    }
                }
                override fun onFailure(call: Call<ConData>, t: Throwable) {}
            })
        }
    }

    private fun saveImage(ba: ByteArray, path: File, name: String) {
        val myPath = File(path, name)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(myPath)
            fos.write(ba)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        finally {
            try {
                fos!!.close()
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun loadPhoto(path: File, name: String): Bitmap? {
        return try {
            val f = File(path, name)
            val b = f.readBytes()
            baList.add(b)
            BitmapFactory.decodeByteArray(b, 0, b.size)

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            baList.add(null)
            null
        }
    }

    private fun loadGIF(path: File, name: String): GifDrawable? {
        return try {
            val f = File(path, name)
            val b = f.readBytes()
            baList.add(b)
            GifDrawable(b)
        }
        catch (e: FileNotFoundException) {
            e.printStackTrace()
            baList.add(null)
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

    private fun removeFile(path: File, name: String) {
        val f = File(path, name)
        if(f.exists()) {
            try {
                f.delete()
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun removeManager(pm: PreferenceManager, index: Int, conNum: String) {
        /*val haveConNumList = pm.getConNumList(applicationContext, "have")!!
        val haveConNameList = pm.getConNameList(applicationContext, "have")!!
        val haveConTitleList = pm.getConTitleList(applicationContext, "have")!!
        val haveConArtistList = pm.getConArtistList(applicationContext, "have")!!
        haveConNumList.removeAt(index)
        haveConNameList.removeAt(index)
        haveConTitleList.removeAt(index)
        haveConArtistList.removeAt(index)*/
        val haveConList = pm.getConList(applicationContext, "have")!!
        haveConList.removeAt(index)

        /*pm.setConNumList(applicationContext, haveConNumList, "have")
        pm.setConNameList(applicationContext, haveConNameList, "have")
        pm.setConTitleList(applicationContext, haveConTitleList, "have")
        pm.setConArtistList(applicationContext, haveConArtistList, "have")*/
        pm.setConList(applicationContext, haveConList, "have")

        //val useConNumList: ArrayList<String>? = pm.getConNumList(applicationContext, "use")
        val useConList: ArrayList<ConInfo>? = pm.getConList(applicationContext, "use")
        if(useConList != null) {
            var isUseCon = false
            var useConIndex = 0
            useConList.forEachIndexed { index, conInfo ->
                if(conInfo.conNum == conNum) {
                    isUseCon = true
                    useConIndex = index
                }
            }
            if(isUseCon) {
                /*val useConNameList: ArrayList<ArrayList<String>> = pm.getConNameList(applicationContext, "use")!!
                val useConTitleList: ArrayList<String> = pm.getConTitleList(applicationContext, "use")!!
                val useConArtistList: ArrayList<String> = pm.getConArtistList(applicationContext, "use")!!*/
                /*useConNumList.removeAt(useConNumIndex)
                useConNameList.removeAt(useConNumIndex)
                useConTitleList.removeAt(useConNumIndex)
                useConArtistList.removeAt(useConNumIndex)*/
                    useConList.removeAt(useConIndex)
                /*pm.setConNumList(applicationContext, useConNumList, "use")
                pm.setConNameList(applicationContext, useConNameList, "use")
                pm.setConTitleList(applicationContext, useConTitleList, "use")
                pm.setConArtistList(applicationContext, useConArtistList, "use")*/
                pm.setConList(applicationContext, useConList, "use")
            }
        }
    }

    private fun downloadManager(pm: PreferenceManager, title: String, conNum: String, conName: ArrayList<String>, artist: String) {
        //val haveConNumList: ArrayList<String>? = pm.getConNumList(applicationContext, "have")
        val haveConList: ArrayList<ConInfo>? = pm.getConList(applicationContext, "have")
        val con = ConInfo(title, artist, conNum, conName)

        if(haveConList != null) {
            /*val haveConNameList = pm.getConNameList(applicationContext, "have")!!
            val haveConTitleList = pm.getConTitleList(applicationContext, "have")!!
            val haveConArtistList = pm.getConArtistList(applicationContext, "have")!!*/

            /*haveConNumList.add(conNum)
            haveConNameList.add(conName)
            haveConTitleList.add(title)
            haveConArtistList.add(artist)*/
            /*haveConNumList.add(0, conNum)
            haveConNameList.add(0, conName)
            haveConTitleList.add(0, title)
            haveConArtistList.add(0, artist)*/

                haveConList.add(0, con)

            /*pm.setConNumList(applicationContext, haveConNumList, "have")
            pm.setConNameList(applicationContext, haveConNameList, "have")
            pm.setConTitleList(applicationContext, haveConTitleList, "have")
            pm.setConArtistList(applicationContext, haveConArtistList, "have")*/
            pm.setConList(applicationContext, haveConList, "have")

            //val useConNumList = pm.getConNumList(applicationContext, "use")
            val useConList = pm.getConList(applicationContext, "use")
            if(useConList != null) {
                /*val useConNameList = pm.getConNameList(applicationContext, "use")!!
                val useConTitleList = pm.getConTitleList(applicationContext, "use")!!
                val useConArtistList = pm.getConArtistList(applicationContext, "use")!!*/
                /*useConNumList.add(conNum)
                useConNameList.add(conName)
                useConTitleList.add(title)
                useConArtistList.add(artist)*/

                /*useConNumList.add(0, conNum)
                useConNameList.add(0, conName)
                useConTitleList.add(0, title)
                useConArtistList.add(0, artist)*/
                    useConList.add(0, con)

                /*pm.setConNumList(applicationContext, useConNumList, "use")
                pm.setConNameList(applicationContext, useConNameList, "use")
                pm.setConTitleList(applicationContext, useConTitleList, "use")
                pm.setConArtistList(applicationContext, useConArtistList, "use")*/
                pm.setConList(applicationContext, useConList, "use")
            }
            else {
                /*val conNumList = ArrayList<String>()
                conNumList.add(conNum)
                val conNameList = ArrayList<ArrayList<String>>()
                conNameList.add(conName)
                val conTitleList = ArrayList<String>()
                conTitleList.add(title)
                val conArtistList = ArrayList<String>()
                conArtistList.add(artist)*/
                    val conList = ArrayList<ConInfo>()
                conList.add(con)

                /*pm.setConNumList(applicationContext, conNumList, "use")
                pm.setConNameList(applicationContext, conNameList, "use")
                pm.setConTitleList(applicationContext, conTitleList, "use")
                pm.setConArtistList(applicationContext, conArtistList, "use")*/
                pm.setConList(applicationContext, conList, "use")
            }
        }
        else {
            /*val conNumList = ArrayList<String>()
            conNumList.add(conNum)
            val conNameList = ArrayList<ArrayList<String>>()
            conNameList.add(conName)
            val conTitleList = ArrayList<String>()
            conTitleList.add(title)
            val conArtistList = ArrayList<String>()
            conArtistList.add(artist)*/
            val conList = ArrayList<ConInfo>()
            conList.add(con)

            /*pm.setConNumList(applicationContext, conNumList, "have")
            pm.setConNameList(applicationContext, conNameList, "have")
            pm.setConTitleList(applicationContext, conTitleList, "have")
            pm.setConArtistList(applicationContext, conArtistList, "have")*/
            pm.setConList(applicationContext, conList, "have")

            /*pm.setConNumList(applicationContext, conNumList, "use")
            pm.setConNameList(applicationContext, conNameList, "use")
            pm.setConTitleList(applicationContext, conTitleList, "use")
            pm.setConArtistList(applicationContext, conArtistList, "use")*/
            pm.setConList(applicationContext, conList, "use")
        }
    }

    private fun setThumbnailSize(thumbnail: ImageView) {
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels

        val layoutParams: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams((width/3.5f).toInt(), (width/3.5f).toInt())
        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        layoutParams.topToBottom = R.id.btn_close
        layoutParams.topMargin = height/20
        thumbnail.layoutParams = layoutParams
    }
}