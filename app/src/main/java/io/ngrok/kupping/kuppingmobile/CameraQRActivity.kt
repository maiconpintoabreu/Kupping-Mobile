package io.ngrok.kupping.kuppingmobile

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.view.GravityCompat
import android.view.MenuItem
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import com.google.gson.JsonParser
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import io.ngrok.kupping.kuppingmobile.menu.NavigationViewAdaptor
import io.ngrok.kupping.kuppingmobile.models.ResponseModel
import io.ngrok.kupping.kuppingmobile.models.StudentModel
import io.ngrok.kupping.kuppingmobile.services.EventApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_camera_qr.*
import retrofit2.HttpException

class CameraQRActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,NavigationViewAdaptor {

    private var isTorch = false
    var found = false
    private lateinit var properties: Properties
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_qr)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.CAMERA),100)

        barcode_view.decodeContinuous(callback)
        properties = Properties.instance
        if(properties.token.isNotBlank()){
            navView.menu.clear()
            navView.inflateMenu(R.menu.activity_main_drawer_logged_in)
        }
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        getActivity(item.itemId,this)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onResume() {
        barcode_view.resume()
        super.onResume()
    }

    override fun onPause() {
        barcode_view.pause()
        disposable?.dispose()
        super.onPause()
    }

    private val callback = object : BarcodeCallback{
        override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {

        }

        override fun barcodeResult(result: BarcodeResult?) {
            if (result!!.text != null){
                // Initialize a new instance of
                val builder = AlertDialog.Builder(this@CameraQRActivity)

                val student:StudentModel? = Properties.instance.eventSelected.students.find { studentModel-> studentModel._id == result.text.toString() }
                            // Set the alert dialog title
                if (student != null) {

                    builder.setTitle("Student: " + student.name)

                    // Display a message on alert dialog
                    builder.setMessage("Event: " + Properties.instance.eventSelected.name)

                    // Set a positive button and its click listener on alert dialog
                    builder.setPositiveButton("Check in") { _, _ ->
                        // Do something when user press the positive button

                        disposable =
                            danceClassApiService.checkin("Bearer "+properties.token,Properties.instance.eventSelected._id,student._id)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    { result -> showResult(result as ResponseModel) },
                                    { error -> showError(error) }
                                )
                    }

                    // Display a neutral button on alert dialog
                    builder.setNeutralButton("Cancel") { _, _ ->
                        found = false
                    }
                    builder.setOnCancelListener{
                        found = false
                    }

                    // Finally, make the alert dialog using builder
                    val dialog: AlertDialog = builder.create()
                    if(!found) {
                        found = true
                        // Display the alert dialog on app interface
                        dialog.show()
                    }
                }else{
                    Toast.makeText(applicationContext, "Student not found", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun showResult(checkingResponseModel: ResponseModel){

        if (checkingResponseModel.status) {
            Toast.makeText(applicationContext, checkingResponseModel.message, Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, EventListActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            found = false
            startActivity(intent)
        }else{
            Toast.makeText(applicationContext, checkingResponseModel.message, Toast.LENGTH_SHORT).show()
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            found = false
            startActivity(intent)
        }
    }
    private fun showError(e: Throwable ) {
        val error = e as HttpException

        val errorJsonString = error.response().errorBody()?.string()
        val message = JsonParser().parse(errorJsonString)
            .asJsonObject["message"]
            .asString
        Log.e("Connection ERROR",message)
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        found = false
        startActivity(intent)
    }

    private val danceClassApiService by lazy {
        EventApiService.create()
    }
    private var disposable: Disposable? = null
}
