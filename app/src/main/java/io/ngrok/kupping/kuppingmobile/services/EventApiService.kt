package io.ngrok.kupping.kuppingmobile.services

import io.ngrok.kupping.kuppingmobile.Properties
import io.ngrok.kupping.kuppingmobile.models.*
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
    @PUT("private/event/{id}")
    fun updateEvent(@Header("Authorization") authorization: String, @Path("id") id: String, @Body newEvent: EventWithStudentsModel):
            Observable<ResponseModel>

    @GET("private/event/{id}")
    fun event(@Header("Authorization") token: String, @Path("id") id: String):
            Observable<EventWithStudentsModel>

    @POST("private/event/{id}/checkin/{studentId}")
    fun checkin(@Header("Authorization") token: String,@Path("id") id: String,@Path("studentId") studentId: String):
            Observable<ResponseModel>


    @POST("public/event/{id}/booking")
    fun booking(@Header("Authorization") token: String,@Path("id") id: String, @Body newStudent: NewStudentModel):
            Observable<ResponseModel>

    @GET("private/event/{id}/ticket/qrcode/{studentid}")
    fun qrcode(@Header("Authorization") token: String, @Path("id") eventId: String,@Path("studentid") studentId: String):
            Observable<ImageModel>

    @DELETE("private/event/{id}/booking/{bookingid}")
    fun removeBooking(@Header("Authorization") token: String,@Path("id") id: String, @Path("bookingid") bookingid: String):
            Observable<ResponseModel>

    @DELETE("private/event/{id}")
    fun deleteEvent(@Header("Authorization") token: String,@Path("id") id: String):
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
