package io.ngrok.kupping.kuppingmobile.services

import io.ngrok.kupping.kuppingmobile.Properties
import io.ngrok.kupping.kuppingmobile.models.ResponseModel
import io.ngrok.kupping.kuppingmobile.models.EventModel
import io.ngrok.kupping.kuppingmobile.models.EventWithStudentsModel
import io.ngrok.kupping.kuppingmobile.models.NewEventModel
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface EventApiService {

    @GET("private/event")
    fun events(@Header("Authorization") authorization: String):
            Observable<List<EventModel>>
    @POST("private/event")
    fun createEvent(@Header("Authorization") authorization: String, @Body newEvent: NewEventModel):
            Observable<ResponseModel>

    @GET("private/event/{id}")
    fun event(@Header("Authorization") token: String, @Path("id") id: String):
            Observable<EventWithStudentsModel>

    @POST("private/event/{id}/checkin/{studentId}")
    fun checkin(@Header("Authorization") token: String,@Path("id") id: String,@Path("studentId") studentId: String):
            Observable<ResponseModel>

    companion object {
        fun create(): EventApiService {

            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(
                    RxJava2CallAdapterFactory.create())
                .addConverterFactory(
                    GsonConverterFactory.create())
                .baseUrl(Properties.instance.url)
                .build()

            return retrofit.create(EventApiService::class.java)
        }
    }
}
