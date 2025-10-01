package com.sonapakhi.autotapper

import android.content.Context
import android.util.Base64
import org.json.JSONObject
import java.nio.charset.Charset
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Security {

    fun verify(ctx: Context, raw: JSONObject, canon: JSONObject): Boolean {
        return try {
            val sec = raw.getJSONObject("security")
            val exp = sec.getLong("exp")
            val sig = sec.getString("sig")
            val now = System.currentTimeMillis() / 1000
            if (now > exp) return false

            val b64 = Base64.encodeToString(canon.toString().toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            val toSign = "$exp.$b64"
            val secret = ctx.getString(R.string.signing_key)
            val calc = hmacSha256Hex(secret.toByteArray(Charsets.UTF_8), toSign.toByteArray(Charsets.UTF_8))
            calc.equals(sig, ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }

    private fun hmacSha256Hex(key: ByteArray, msg: ByteArray): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        val out = mac.doFinal(msg)
        return out.joinToString("") { "%02x".format(it) }
    }
}
