package io.ngrok.kupping.kuppingmobile.models

import androidx.room.Database
import androidx.room.RoomDatabase
import io.ngrok.kupping.kuppingmobile.services.UserLocalService

@Database(entities = [UserLocalModel::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userLocalService(): UserLocalService
}