package io.ngrok.kupping.kuppingmobile

import android.util.Base64
import android.util.Log
import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.Claim
import io.ngrok.kupping.kuppingmobile.models.EventWithStudentsModel
import java.io.UnsupportedEncodingException
import java.util.*


class Properties {
    var token: String = ""
        get() = decoded(JWTEncoded = field)
    var url = "https://kuppingbackend.maicondev.com/"
    //var url = "https://5785cd6f.ngrok.io"

    lateinit var eventSelected: EventWithStudentsModel
    companion object {
        val instance = Properties()
    }

    private fun decoded(JWTEncoded: String): String {
        try {
            val parsedJWT = JWT.decode(JWTEncoded)
            val exp = parsedJWT.expiresAt
            if(exp.before(Date())){
                this.token = ""
                return ""
            }

        }catch (e: JWTDecodeException) {
            Log.e("TOKEN PARSED ERROR",e.message)
        }
        catch (e: UnsupportedEncodingException) {
            Log.e("TOKEN PARSED ERROR",e.message)
        }
        catch (e: Exception) {
            Log.e("TOKEN PARSED ERROR",e.message)
        }
        return JWTEncoded
    }
}