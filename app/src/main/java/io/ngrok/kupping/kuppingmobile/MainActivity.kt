package io.ngrok.kupping.kuppingmobile

import android.content.Intent
import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import android.view.MenuItem
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.widget.Button
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var properties: Properties
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val usernameTextInputEditText: TextInputEditText = findViewById(R.id.login_username_input_edit)
        val passwordTextInputEditText: TextInputEditText = findViewById(R.id.login_password_input_edit)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
        val btnLogin: Button = findViewById(R.id.btn_login)
        btnLogin.setOnClickListener {
            login(usernameTextInputEditText.text.toString(),passwordTextInputEditText.text.toString())
        }
        properties = Properties.instance
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
        when (item.itemId) {
            R.id.nav_login -> {
                // clear
            }
            R.id.nav_sign_up -> {
                val intent = Intent(this, SignUpActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
            R.id.nav_camera -> {
                if(properties.token.isNotBlank()) {
                    val intent = Intent(this, CameraQRActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                }else{
                    Toast.makeText(applicationContext, "To open the camera you need to be log in", Toast.LENGTH_LONG).show()
                }
            }
            R.id.nav_student -> {
                if(properties.token.isNotBlank()) {
                }else{
                    Toast.makeText(applicationContext, "To open the camera you need to be log in", Toast.LENGTH_LONG).show()
                }
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    private fun login(username: String, password: String){
        val params = HashMap<String,String>()
        params["username"] = username
        params["password"] = password
        val jsonObject = JSONObject(params as Map<String, String>)
        val queue = Volley.newRequestQueue(this)
        val stringRequest = JsonObjectRequest(
            Request.Method.POST, properties.url+"auth/login",jsonObject,
            Response.Listener { response ->
                // Display the first 500 characters of the response string.
                Toast.makeText(applicationContext, response.toString(), Toast.LENGTH_LONG).show()
                properties.token = response.toString()
            },
            Response.ErrorListener {
                Toast.makeText(applicationContext,"That didn't work!",Toast.LENGTH_LONG).show()
                properties.token = ""
            })
        queue.add(stringRequest)
    }
}
