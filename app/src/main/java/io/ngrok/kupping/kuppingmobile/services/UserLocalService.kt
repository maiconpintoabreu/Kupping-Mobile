package io.ngrok.kupping.kuppingmobile.services

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.ngrok.kupping.kuppingmobile.models.UserLocalModel

@Dao
    interface UserLocalService {

    @Query("SELECT * FROM userlocalmodel WHERE uuid = :uuid LIMIT 1")
    fun findById(uuid: String): UserLocalModel?
    @Query("SELECT * FROM userlocalmodel LIMIT 1")
    fun findLast(): UserLocalModel?
    @Insert
    fun insertAll(vararg users: UserLocalModel)

    @Delete
    fun delete(user: UserLocalModel)

    @Query("DELETE FROM userlocalmodel")
    fun nukeTable()
}