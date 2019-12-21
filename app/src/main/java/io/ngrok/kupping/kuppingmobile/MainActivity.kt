package io.ngrok.kupping.kuppingmobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import android.view.MenuItem
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.JsonParser
import io.ngrok.kupping.kuppingmobile.menu.NavigationViewAdaptor
import io.ngrok.kupping.kuppingmobile.models.LoginModel
import io.ngrok.kupping.kuppingmobile.services.IAMApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.event_list_content.*
import retrofit2.HttpException

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,NavigationViewAdaptor {
    private lateinit var properties: Properties
    private lateinit var navView: NavigationView
    private lateinit var loginBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
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
            login(it,usernameTextInputEditText.text.toString(),passwordTextInputEditText.text.toString())
        }
        properties = Properties.instance
        if(properties.token.isNotBlank()){
            navView.menu.clear()
            navView.inflateMenu(R.menu.activity_main_drawer_logged_in)
            val intent = Intent(this, EventListActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        loginBar = findViewById(R.id.login_bar)
        loginBar.visibility = ProgressBar.GONE

    }
    private val loginApiService by lazy {
        IAMApiService.create()
    }
    private var disposable: Disposable? = null
    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    override fun onPause() {
        super.onPause()
        disposable?.dispose()
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
    private fun login(view: View, username: String, password: String){
        val loginModel = LoginModel(username,password)
        loginBar.visibility = ProgressBar.VISIBLE
        disposable =
            loginApiService.login(loginModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result -> showResult(view,result.token) },
                    { error -> showError(view,error) }
                )
    }
    private fun showResult(view: View,token: String){
        loginBar.visibility = ProgressBar.GONE
        Snackbar.make(view, "Log in success", Snackbar.LENGTH_LONG)
            .setAction("Login-Success", null).show()
        properties.token = token
        navView.menu.clear()
        navView.inflateMenu(R.menu.activity_main_drawer_logged_in)
        val intent = Intent(this, EventListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
    private fun showError(view: View,e: Throwable) {
        loginBar.visibility = ProgressBar.GONE

        val error = e as HttpException

        val errorJsonString = error.response().errorBody()?.string()
//        val message = JsonParser().parse(errorJsonString)
//            .asJsonObject["message"]
//            .asString
        Log.e("LOGIN-ERROR",errorJsonString)
        Snackbar.make(view, "ERROR $errorJsonString", Snackbar.LENGTH_LONG)
            .setAction("Login-Error", null).show()
    }
}
