package io.ngrok.kupping.kuppingmobile

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.core.view.GravityCompat
import android.view.MenuItem
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import io.ngrok.kupping.kuppingmobile.menu.NavigationViewAdaptor
import kotlinx.android.synthetic.main.content_camera_qr.*

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

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
        super.onPause()
    }

    private val callback = object : BarcodeCallback{
        override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {

        }

        override fun barcodeResult(result: BarcodeResult?) {
            if (result!!.text != null){
                val resultTextView: TextView = findViewById(R.id.result)
                resultTextView.text = result.text.toString()
                // Initialize a new instance of
                val builder = AlertDialog.Builder(this@CameraQRActivity)

                // Set the alert dialog title
                builder.setTitle("Student: " + result.text.toString())

                // Display a message on alert dialog
                builder.setMessage("Class: " + " Test")

                // Set a positive button and its click listener on alert dialog
                builder.setPositiveButton("Check in") { _, _ ->
                    // Do something when user press the positive button
                    Toast.makeText(applicationContext, "Ok.", Toast.LENGTH_SHORT).show()
                    found = false
                }

                // Display a neutral button on alert dialog
                builder.setNeutralButton("Cancel") { _, _ ->
                    Toast.makeText(applicationContext, "You cancelled the dialog.", Toast.LENGTH_SHORT).show()
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
            }
        }

    }
}