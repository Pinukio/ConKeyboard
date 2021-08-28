package com.example.conkeyboard

import android.os.AsyncTask
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL


class ConvertToByteArrayTask: AsyncTask<List<URL>, Void, ArrayList<ByteArray?>>() {

    override fun doInBackground(vararg params: List<URL>): ArrayList<ByteArray?> {
        val arrayList: ArrayList<ByteArray?> = ArrayList()
        for(i in params[0].indices) {
            val connection = params[0][i].openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val inputStream = connection.inputStream
            val bis = BufferedInputStream(inputStream)
            val CAPACITY: Int = 1024*1024
            val bof = ByteArrayOutputStream()
            val data = ByteArray(CAPACITY)
            var current: Int = bis.read(data, 0, data.size)

            while(current != -1) {
                bof.write(data, 0, current)
                current = bis.read(data, 0, data.size)
            }
            arrayList.add(bof.toByteArray())
            bof.close()
            bis.close()
            connection.disconnect()
        }

        return arrayList
    }

}