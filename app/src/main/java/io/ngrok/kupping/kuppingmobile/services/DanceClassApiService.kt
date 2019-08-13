package io.ngrok.kupping.kuppingmobile.services

import io.ngrok.kupping.kuppingmobile.Properties
import io.ngrok.kupping.kuppingmobile.models.EventModel
import io.ngrok.kupping.kuppingmobile.models.EventWithStudentsModel
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface DanceClassApiService {

    @GET("private/danceclass")
    fun danceClasses(@Header("Authorization") authorization: String):
            Observable<List<EventModel>>

    @GET("private/danceclass/{id}")
    fun danceClass(@Header("Authorization") token: String,@Path("id") id: String):
            Observable<EventWithStudentsModel>

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
