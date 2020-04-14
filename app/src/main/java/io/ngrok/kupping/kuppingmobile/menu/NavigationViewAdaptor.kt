package io.ngrok.kupping.kuppingmobile.menu

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.room.Room
import io.ngrok.kupping.kuppingmobile.*
import io.ngrok.kupping.kuppingmobile.models.AppDatabase


interface NavigationViewAdaptor {
    fun getActivity(itemId: Int,context: Context){
        var properties = Properties.instance
        when (itemId) {
            R.id.nav_logout -> {
                properties.token = ""
                var db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "kupping-local"
                ).allowMainThreadQueries().build()
                db.userLocalService().nukeTable()
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
            R.id.nav_login -> {
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }
            R.id.nav_sign_up -> {
                val intent = Intent(context, SignUpActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }
//            R.id.nav_camera -> {
//                if(properties.token.isNotBlank()) {
//                    val intent = Intent(context, CameraQRActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//                    context.startActivity(intent)
//                }else{
//                    Toast.makeText(context.applicationContext, "To open the camera you need to be log in", Toast.LENGTH_LONG).show()
//                }
//            }
//            R.id.nav_student -> {
//                if(properties.token.isNotBlank()) {
//                }else{
//                    Toast.makeText(context.applicationContext, "To open the camera you need to be log in", Toast.LENGTH_LONG).show()
//                }
//            }
            R.id.nav_dance_class -> {
                val intent = Intent(context, EventListActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }
        }
    }
}