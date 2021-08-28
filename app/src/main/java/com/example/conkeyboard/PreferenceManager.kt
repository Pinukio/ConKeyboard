package com.example.conkeyboard

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class PreferenceManager {
    private val preferencesName: String = "preference_con"
    private val useConKey: String = "con_use"
    private val haveConKey: String = "con_have"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    }

    fun getConList(context: Context, flag: String): ArrayList<ConInfo>? {
        val preferences = getSharedPreferences(context)
        val gson = Gson()
        val json = when(flag) {
            "use" -> preferences.getString(useConKey, "")
            else -> preferences.getString(haveConKey, "")
        }
        val type: Type = object: TypeToken<ArrayList<ConInfo>>() {}.type
        return gson.fromJson(json, type)
    }

    fun setConList(context: Context, conList: ArrayList<ConInfo>, flag: String) {
        val preferences = getSharedPreferences(context)
        val editor = preferences.edit()
        val gson = Gson()
        val json = gson.toJson(conList)
        when(flag) {
            "use" -> editor.putString(useConKey, json)
            else -> editor.putString(haveConKey, json)
        }
        editor.apply()
    }
}