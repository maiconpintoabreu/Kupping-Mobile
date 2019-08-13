package io.ngrok.kupping.kuppingmobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

import io.ngrok.kupping.kuppingmobile.menu.NavigationViewAdaptor
import io.ngrok.kupping.kuppingmobile.models.EventModel
import io.ngrok.kupping.kuppingmobile.services.DanceClassApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.app_bar_event_list.*
import kotlinx.android.synthetic.main.event_list_content.view.*
import kotlinx.android.synthetic.main.event_list.*

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [EventDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class EventListActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,NavigationViewAdaptor {
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        getActivity(item.itemId,this)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false
    private lateinit var properties: Properties
    private lateinit var navView: NavigationView
    private var eventList: List<EventModel> =  ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_list)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = title

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        if (event_detail_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        properties = Properties.instance
        if(properties.token.isNotBlank()){
            navView.menu.clear()
            navView.inflateMenu(R.menu.activity_main_drawer_logged_in)
        }
        getEvents()
    }

    private val danceClassApiService by lazy {
        DanceClassApiService.create()
    }
    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }

    override fun onResume() {
        super.onResume()
        getEvents()
    }
    private var disposable: Disposable? = null
    private fun getEvents(){
        disposable =
            danceClassApiService.danceClasses("Bearer "+properties.token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result -> showResult(result as List<EventModel>) },
                    { error -> showError(error.message) }
                )
    }
    private fun showResult(eventList: List<EventModel>){

        this.eventList = eventList
        setupRecyclerView(event_list)
    }
    private fun showError(message: String?) {
        Log.e("Connection ERROR",message)
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, eventList, twoPane)
    }

    class SimpleItemRecyclerViewAdapter(
        private val parentActivity: EventListActivity,
        private val values: List<EventModel>,
        private val twoPane: Boolean
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as EventModel
                if (twoPane) {
                    val fragment = EventDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString(EventDetailFragment.ARG_ITEM_ID, item._id)
                        }
                    }
                    parentActivity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.event_detail_container, fragment)
                        .commit()
                } else {
                    val intent = Intent(v.context, EventDetailActivity::class.java).apply {
                        putExtra(EventDetailFragment.ARG_ITEM_ID, item._id)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.event_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.contentView.text = item.name

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }
        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val contentView: TextView = view.content
        }
    }
}
