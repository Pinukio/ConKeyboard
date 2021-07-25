package com.example.conkeyboard

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import java.net.HttpURLConnection
import java.net.URL

class ImageDownloadTask: AsyncTask<URL, Void, Bitmap?>() {

    override fun doInBackground(vararg params: URL?): Bitmap? {
        var image: Bitmap?
        val connection = params[0]!!.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        val inputStream = connection.inputStream
        image = BitmapFactory.decodeStream(inputStream)

        return image
    }

}