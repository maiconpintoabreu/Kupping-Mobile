package io.ngrok.kupping.kuppingmobile

import io.ngrok.kupping.kuppingmobile.models.EventWithStudentsModel

class Properties {
    var token: String = ""
    var url = "https://kuppingbackend.maicondev.com/"
    //var url = "https://5785cd6f.ngrok.io"

    lateinit var eventSelected: EventWithStudentsModel
    companion object {
        val instance = Properties()
    }

}