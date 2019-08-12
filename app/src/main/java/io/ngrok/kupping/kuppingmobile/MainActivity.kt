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
import com.google.android.material.textfield.TextInputEditText
import io.ngrok.kupping.kuppingmobile.menu.NavigationViewAdaptor
import io.ngrok.kupping.kuppingmobile.models.LoginModel
import io.ngrok.kupping.kuppingmobile.services.IAMApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,NavigationViewAdaptor {
    private lateinit var properties: Properties
    private lateinit var navView: NavigationView
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
            login(usernameTextInputEditText.text.toString(),passwordTextInputEditText.text.toString())
        }
        properties = Properties.instance
        if(properties.token.isNotBlank()){
            navView.menu.clear()
            navView.inflateMenu(R.menu.activity_main_drawer_logged_in)
        }
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
    private fun login(username: String, password: String){
        val loginModel = LoginModel(username,password)
        disposable =
            loginApiService.login(loginModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result -> showResult(result.token) },
                    { error -> showError(error.message) }
                )
    }
    private fun showResult(token: String){
        Toast.makeText(applicationContext, "Log in success", Toast.LENGTH_LONG).show()
        properties.token = token
        navView.menu.clear()
        navView.inflateMenu(R.menu.activity_main_drawer_logged_in)
    }
    private fun showError(message: String?) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}
