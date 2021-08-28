package com.example.conkeyboard

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.conkeyboard.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.lang.Exception
import java.net.URL
import java.net.URLConnection

class MainActivity : AppCompatActivity(), OnItemClick {
    private lateinit var haveConAdapter: ShopAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pm = PreferenceManager()
        val haveConList = pm.getConList(applicationContext, "have")
        if(haveConList != null) {
            val bitmapList: ArrayList<Bitmap?> = ArrayList()
            for(i in haveConList.indices) {
                bitmapList.add(loadPNG(getPath(haveConList[i].conNum), "title.jpg"))
            }
            haveConAdapter = ShopAdapter(bitmapList, this, haveConList)
            binding.recyclerConsHave.adapter = haveConAdapter
        }

        binding.btnSearch.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        val retrofit = RetrofitConnection(applicationContext)
        val newConCall: Call<List<ConData>> = retrofit.server.getNewCons()

        val listener: OnItemClick = this

        newConCall.enqueue(object: Callback<List<ConData>> {
            override fun onResponse(call: Call<List<ConData>>, response: Response<List<ConData>>) {
                val dataList: List<ConData>? = response.body()
                if(dataList != null) {
                    val bitmapList: ArrayList<Bitmap?> = ArrayList()
                    val conList: ArrayList<ConInfo> = ArrayList()
                    val conName: ArrayList<String> = ArrayList()
                    val urlList = ArrayList<URL>()
                    for(i in dataList.indices) {
                        urlList.add(URL(dataList[i].photo[0]))
                    }
                    val baList = ConvertToByteArrayTask().execute(urlList).get()
                    if(baList != null) {
                        for(i in baList.indices) {
                            val byteArray = baList[i]
                            if (byteArray != null) {
                                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                                bitmapList.add(bitmap)
                            } else {
                                bitmapList.add(null)
                            }
                            val con = ConInfo(dataList[i].title, dataList[i].artist, dataList[i].conNum, conName)
                            conList.add(con)
                        }
                    }
                    val adapter = ShopAdapter(bitmapList, listener, conList)
                    binding.recyclerConsNew.adapter = adapter
                }
            }

            override fun onFailure(call: Call<List<ConData>>, t: Throwable) {
            }
        })
    }

    private fun loadPNG(path: File, name: String): Bitmap? {
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

    override fun onClick(conNum: String) {
        val intent = Intent(this, ConInfoActivity::class.java)
        intent.putExtra("conNum", conNum)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        val pm = PreferenceManager()
        val haveConList = pm.getConList(applicationContext, "have")
        if(haveConList != null) {
            val bitmapArrayList: ArrayList<Bitmap?> = ArrayList()
            for(i in haveConList.indices) {
                bitmapArrayList.add(loadPNG(getPath(haveConList[i].conNum), "title.jpg"))
            }
            haveConAdapter.setData(bitmapArrayList, haveConList)
            haveConAdapter.notifyDataSetChanged()
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
}