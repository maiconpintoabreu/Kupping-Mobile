package io.ngrok.kupping.kuppingmobile.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class UserLocalModel(
    @PrimaryKey val uuid: String,
    @ColumnInfo(name = "token") val token: String,
    @ColumnInfo(name = "expire_at") val expireAt: Int
)