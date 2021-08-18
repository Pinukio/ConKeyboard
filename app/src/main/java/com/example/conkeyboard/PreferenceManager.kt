package com.example.conkeyboard

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class PreferenceManager() {
    private val preferencesName: String = "preference_con"
    private val useConNumKey: String = "num_con_use" //사용하는 콘
    private val haveConNumKey: String = "num_con_have" //사용하지 않더라도 갖고 있는 콘
    private val useConNameKey: String = "name_con_use"
    private val haveConNameKey: String = "name_con_have"
    private val useConTitleKey: String = "title_con_use"
    private val haveConTitleKey: String = "title_con_have"
    private val useConArtistKey: String = "artist_con_use"
    private val haveConArtistKey: String = "artist_con_have"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    }

    fun getConNumList(context: Context, flag: String): ArrayList<String>? {
        val preferences = getSharedPreferences(context)
        val gson = Gson()
        val json = when(flag) {
            "use" -> preferences.getString(useConNumKey, "")
            else -> preferences.getString(haveConNumKey, "")
        }
        val type: Type = object: TypeToken<ArrayList<String>>() {}.type
        return gson.fromJson(json, type)
    }

    fun setConNumList(context: Context, conNumList: ArrayList<String>, flag: String) {
        val preferences = getSharedPreferences(context)
        val editor = preferences.edit()
        val gson = Gson()
        val json = gson.toJson(conNumList)
        when(flag) {
            "use" -> editor.putString(useConNumKey, json)
            else -> editor.putString(haveConNumKey, json)
        }
        editor.apply()
    }

    fun getConNameList(context: Context, flag: String): ArrayList<ArrayList<String>>? {
        val preferences = getSharedPreferences(context)
        val gson = Gson()
        val json = when(flag) {
            "use" -> preferences.getString(useConNameKey, "")
            else -> preferences.getString(haveConNameKey, "")
        }
        val type: Type = object: TypeToken<ArrayList<ArrayList<String>>>() {}.type
        return gson.fromJson(json, type)
    }

    fun setConNameList(context: Context, conNameList: ArrayList<ArrayList<String>>, flag: String) {
        val preferences = getSharedPreferences(context)
        val editor = preferences.edit()
        val gson = Gson()
        val json = gson.toJson(conNameList)
        when(flag) {
            "use" -> editor.putString(useConNameKey, json)
            else -> editor.putString(haveConNameKey, json)
        }
        editor.apply()
    }

    fun getConTitleList(context: Context, flag: String): ArrayList<String>? {
        val preferences = getSharedPreferences(context)
        val gson = Gson()
        val json = when(flag) {
            "use" -> preferences.getString(useConTitleKey, "")
            else -> preferences.getString(haveConTitleKey, "")
        }
        val type: Type = object: TypeToken<ArrayList<String>>() {}.type
        return gson.fromJson(json, type)
    }

    fun setConTitleList(context: Context, conTitleList: ArrayList<String>, flag: String) {
        val preferences = getSharedPreferences(context)
        val editor = preferences.edit()
        val gson = Gson()
        val json = gson.toJson(conTitleList)
        when(flag) {
            "use" -> editor.putString(useConTitleKey, json)
            else -> editor.putString(haveConTitleKey, json)
        }
        editor.apply()
    }

    fun getConArtistList(context: Context, flag: String): ArrayList<String>? {
        val preferences = getSharedPreferences(context)
        val gson = Gson()
        val json = when(flag) {
            "use" -> preferences.getString(useConArtistKey, "")
            else -> preferences.getString(haveConArtistKey, "")
        }
        val type: Type = object: TypeToken<ArrayList<String>>() {}.type
        return gson.fromJson(json, type)
    }

    fun setConArtistList(context: Context, conArtistList: ArrayList<String>, flag: String) {
        val preferences = getSharedPreferences(context)
        val editor = preferences.edit()
        val gson = Gson()
        val json = gson.toJson(conArtistList)
        when(flag) {
            "use" -> editor.putString(useConArtistKey, json)
            else -> editor.putString(haveConArtistKey, json)
        }
        editor.apply()
    }
}