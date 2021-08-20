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
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.net.URL

class MainActivity : AppCompatActivity(), OnItemClick {
    private lateinit var adapter: ShopAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pm = PreferenceManager()
        val haveConNumList = pm.getConNumList(applicationContext, "have")
        val haveConTitleList = pm.getConTitleList(applicationContext, "have")
        val haveConArtistList = pm.getConArtistList(applicationContext, "have")
        val bitmapArrayList: ArrayList<Bitmap?> = ArrayList()
        if(haveConNumList != null) {
            for(i in haveConNumList.indices) {
                bitmapArrayList.add(loadPNG(getPath(haveConNumList[i]), "title.jpg"))
            }
            adapter = ShopAdapter(bitmapArrayList, haveConTitleList!!, haveConArtistList!!, this, haveConNumList)
            binding.recyclerConsHave.adapter = adapter
        }

        binding.btnSearch.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        /*val retrofit = RetrofitConnection(applicationContext)
        val dailyHitConCall: Call<List<ConData>> = retrofit.server.getDailyHitCons()
        val weeklyHitConCall: Call<List<ConData>> = retrofit.server.getWeeklyHitCons()
        val newHitConCall: Call<List<ConData>> = retrofit.server.getNewCons()

        val listener: OnItemClick = this

        dailyHitConCall.enqueue(object: Callback<List<ConData>> {
            override fun onResponse(call: Call<List<ConData>>, response: Response<List<ConData>>) {
                val dataList: List<ConData>? = response.body()
                if(dataList != null) {
                    bitmapArrayList.clear()
                    val conNumList: ArrayList<String> = ArrayList()
                    for(i in dataList.indices) {
                        val byteArray = ConvertToByteArrayTask().execute(listOf(URL(dataList[i].photo[0]))).get()[0]
                        if (byteArray != null) {
                            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                            bitmapArrayList.add(bitmap)
                        } else {
                            bitmapArrayList.add(null)
                        }
                        conNumList.add(dataList[i].conNum)
                    }
                    val adapter = ShopAdapter(bitmapArrayList, listener, conNumList)
                    binding.recyclerConsDailyHit.adapter = adapter
                }
            }

            override fun onFailure(call: Call<List<ConData>>, t: Throwable) {
            }
        })

        weeklyHitConCall.enqueue(object: Callback<List<ConData>> {
            override fun onResponse(call: Call<List<ConData>>, response: Response<List<ConData>>) {
                val dataList: List<ConData>? = response.body()
                if(dataList != null) {
                    bitmapArrayList.clear()
                    val conNumList: ArrayList<String> = ArrayList()
                    for(i in dataList.indices) {
                        val byteArray =
                            ConvertToByteArrayTask().execute(listOf(URL(dataList[i].photo[0])))
                                .get()[0]
                        if (byteArray != null) {
                            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                            bitmapArrayList.add(bitmap)
                        } else {
                            bitmapArrayList.add(null)
                        }
                        conNumList.add(dataList[i].conNum)
                    }
                    val adapter = ShopAdapter(bitmapArrayList, listener, conNumList)
                    binding.recyclerConsWeeklyHit.adapter = adapter
                }
            }

            override fun onFailure(call: Call<List<ConData>>, t: Throwable) {
            }
        })

        newHitConCall.enqueue(object: Callback<List<ConData>> {
            override fun onResponse(call: Call<List<ConData>>, response: Response<List<ConData>>) {
                val dataList: List<ConData>? = response.body()
                if(dataList != null) {
                    bitmapArrayList.clear()
                    val conNumList: ArrayList<String> = ArrayList()
                    for(i in dataList.indices) {
                        val byteArray = ConvertToByteArrayTask().execute(listOf(URL(dataList[i].photo[0]))).get()[0]
                        if (byteArray != null) {
                            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                            bitmapArrayList.add(bitmap)
                        } else {
                            bitmapArrayList.add(null)
                        }
                        conNumList.add(dataList[i].conNum)
                    }
                    val adapter = ShopAdapter(bitmapArrayList, listener, conNumList)
                    binding.recyclerConsNew.adapter = adapter
                }
            }

            override fun onFailure(call: Call<List<ConData>>, t: Throwable) {
            }
        })*/
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
        val haveConNumList = pm.getConNumList(applicationContext, "have")
        val haveConTitleList = pm.getConTitleList(applicationContext, "have")
        val haveConArtistList = pm.getConArtistList(applicationContext, "have")
        val bitmapArrayList: ArrayList<Bitmap?> = ArrayList()
        if(haveConNumList != null) {
            for(i in haveConNumList.indices) {
                bitmapArrayList.add(loadPNG(getPath(haveConNumList[i]), "title.jpg"))
            }
            adapter.setData(bitmapArrayList, haveConTitleList!!, haveConArtistList!!)
            adapter.notifyDataSetChanged()
        }
    }
}