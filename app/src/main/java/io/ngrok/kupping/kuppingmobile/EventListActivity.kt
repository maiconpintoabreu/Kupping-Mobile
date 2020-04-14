package io.ngrok.kupping.kuppingmobile

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import com.google.android.material.navigation.NavigationView

import io.ngrok.kupping.kuppingmobile.menu.NavigationViewAdaptor
import io.ngrok.kupping.kuppingmobile.models.EventModel
import io.ngrok.kupping.kuppingmobile.models.EventWithStudentsModel
import io.ngrok.kupping.kuppingmobile.models.ResponseModel
import io.ngrok.kupping.kuppingmobile.services.EventApiService
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
class EventListActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,NavigationViewAdaptor,
    DeleteDialogFragment.NoticeDialogListener {
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
    private var item: EventModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_list)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = title

        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()

            if (twoPane) {
                val fragment = EventNewFragment().apply {
                    arguments = Bundle().apply {
//                        putString(EventNewFragment.ARG_ITEM_ID, item._id)
                    }
                }
                this.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.event_detail_container, fragment)
                    .commit()
            } else {
                val intent = Intent(view.context, EventNewActivity::class.java).apply {
//                    putExtra(EventDetailFragment.ARG_ITEM_ID, item._id)
                }
                view.context.startActivity(intent)
            }
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
        EventApiService.create()
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
        event_bar.visibility = ProgressBar.VISIBLE
        disposable =
            danceClassApiService.events("Bearer "+properties.token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result -> showResult(result as List<EventModel>) },
                    { error -> showError(error.message) }
                )
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    private fun showResult(eventList: List<EventModel>){
        this.eventList = eventList
        setupRecyclerView(event_list)
        event_bar.visibility = ProgressBar.GONE
    }
    private fun showError(message: String?) {
        if (message != null) {
            Log.e("Connection ERROR",message)
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
        event_bar.visibility = ProgressBar.GONE
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, eventList, twoPane)
    }
    private fun showDialog(item: EventModel) {
        selectLongClickItem(item)
        val fragmentManager = supportFragmentManager
        val newFragment = DeleteDialogFragment()
        newFragment.show(fragmentManager, "dialog")
    }
    private fun selectLongClickItem(item: EventModel?){
        this.item = item
    }

    class SimpleItemRecyclerViewAdapter(
        private val parentActivity: EventListActivity,
        private val values: List<EventModel>,
        private val twoPane: Boolean
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener
        private val onLongClickListener: View.OnLongClickListener

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
            onLongClickListener = View.OnLongClickListener { v ->
                val item = v.tag as EventModel
                parentActivity.showDialog(item)
                true
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
                setOnLongClickListener(onLongClickListener)
            }
        }
        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val contentView: TextView = view.content
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        if(this.item != null) {

            disposable =
                danceClassApiService.deleteEvent("Bearer "+properties.token, this.item!!._id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { result -> showDeleteResult(result as ResponseModel) },
                        { error -> showError(error.message) }
                    )
        }else{
            Toast.makeText(dialog.context, "ERROR Long Click: " + this.item, Toast.LENGTH_LONG)
                .show()
        }
        dialog.dismiss()
    }
    private fun showDeleteResult(result: ResponseModel){
        Toast.makeText(baseContext, "Event Deleted: " + this.item!!.name, Toast.LENGTH_LONG)
            .show()
        getEvents()
        this.item = null
    }
    override fun onDialogNegativeClick(dialog: DialogFragment) {
        this.item = null
        dialog.dismiss()
    }
}

class DeleteDialogFragment : DialogFragment() {

    private lateinit var listener: NoticeDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Build the dialog and set up the button click handlers
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Delete Event?")
                .setPositiveButton("Delete",
                    DialogInterface.OnClickListener { _, _ ->
                        // Send the positive button event back to the host activity
                        listener.onDialogPositiveClick(this)
                    })
                .setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { _, _ ->
                        // Send the negative button event back to the host activity
                        listener.onDialogNegativeClick(this)
                    })

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
    interface NoticeDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as NoticeDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }
}
