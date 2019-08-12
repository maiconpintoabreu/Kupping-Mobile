package io.ngrok.kupping.kuppingmobile.services

import io.ngrok.kupping.kuppingmobile.Properties
import io.ngrok.kupping.kuppingmobile.models.LoginModel
import io.ngrok.kupping.kuppingmobile.models.SignUpModel
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
object ResponseLoginModel {
    data class Result(val token: String)
}
interface IAMApiService {

    @POST("auth/login")
    fun login(@Body body:  LoginModel):
            Observable<ResponseLoginModel.Result>

    @POST("auth/signup")
    fun signUp(@Body body:  SignUpModel):
            Observable<ResponseLoginModel.Result>

    companion object {
        fun create(): IAMApiService {

            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(
                    RxJava2CallAdapterFactory.create())
                .addConverterFactory(
                    GsonConverterFactory.create())
                .baseUrl(Properties.instance.url)
                .build()

            return retrofit.create(IAMApiService::class.java)
        }
    }
}
