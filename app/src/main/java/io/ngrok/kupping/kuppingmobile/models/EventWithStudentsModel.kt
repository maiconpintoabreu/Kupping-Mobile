package io.ngrok.kupping.kuppingmobile.models


class EventWithStudentsModel(
    var _id: String,
    var style: StyleModel,
    var fromDate: Long,
    var toDate: Long,
    var about: String,
    var name: String,
    var students: List<StudentModel>,
    var place: PlaceModel
    )
