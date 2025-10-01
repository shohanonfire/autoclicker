package com.sonapakhi.autotapper

import android.content.Context
import org.json.JSONObject

object ConfigStore {
    private const val PREF = "cfg"
    private const val KEY_RAW = "raw"
    private const val KEY_CANON = "canon"

    fun save(ctx: Context, rawJson: String): Boolean {
        return try {
            val obj = JSONObject(rawJson)
            // Security block must exist
            val sec = obj.getJSONObject("security")
            // We keep two forms: raw (for loop) and canonical (with sig cleared) for verification
            val canon = JSONObject(obj.toString())
            canon.getJSONObject("security").put("sig", "")

            ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
                .putString(KEY_RAW, obj.toString())
                .putString(KEY_CANON, canon.toString())
                .apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun load(ctx: Context): Pair<org.json.JSONObject, org.json.JSONObject>? {
        val sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val raw = sp.getString(KEY_RAW, null) ?: return null
        val canon = sp.getString(KEY_CANON, null) ?: return null
        return try {
            Pair(org.json.JSONObject(raw), org.json.JSONObject(canon))
        } catch (e: Exception) {
            null
        }
    }
}
