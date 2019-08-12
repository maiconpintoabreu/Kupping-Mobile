package io.ngrok.kupping.kuppingmobile.services

import io.ngrok.kupping.kuppingmobile.Properties
import io.ngrok.kupping.kuppingmobile.models.LoginModel
import io.ngrok.kupping.kuppingmobile.models.SignUpModel
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
object ResponseDanceClassModel {
    data class Result(val token: String)
}
interface DanceClassApiService {

    @GET("private/danceclass")
    fun danceClasses(@Body body:  LoginModel,@Header("Authorization") token: String):
            Observable<ResponseDanceClassModel.Result>

    companion object {
        fun create(): DanceClassApiService {

            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(
                    RxJava2CallAdapterFactory.create())
                .addConverterFactory(
                    GsonConverterFactory.create())
                .baseUrl(Properties.instance.url)
                .build()

            return retrofit.create(DanceClassApiService::class.java)
        }
    }
}
