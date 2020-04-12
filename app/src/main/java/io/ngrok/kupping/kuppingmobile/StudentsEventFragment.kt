package io.ngrok.kupping.kuppingmobile

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import io.ngrok.kupping.kuppingmobile.models.EventModel
import io.ngrok.kupping.kuppingmobile.models.EventWithStudentsModel
import io.ngrok.kupping.kuppingmobile.models.StudentModel
import io.ngrok.kupping.kuppingmobile.models.StyleModel
import io.ngrok.kupping.kuppingmobile.services.EventApiService
import io.ngrok.kupping.kuppingmobile.services.StyleApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_event_detail.*
import kotlinx.android.synthetic.main.activity_event_detail.event_detail_container
import kotlinx.android.synthetic.main.event_detail.*
import kotlinx.android.synthetic.main.event_list.*
import kotlinx.android.synthetic.main.event_list_content.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger

class StudentsEventFragment : Fragment() {

    private var item: EventWithStudentsModel? = null
    private lateinit var bar: ProgressBar
    private lateinit var peopleList: RecyclerView
    private var twoPane: Boolean = false
    private lateinit var properties: Properties

    private var studentList: List<StudentModel> = listOf()

    private val danceClassApiService by lazy {
        EventApiService.create()
    }
    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }

    override fun onResume() {
        super.onResume()
        if(item != null) {
            getEvent(item!!._id)
        }
    }
    private var disposable: Disposable? = null
    private fun getEvent(id: String?){
        bar.visibility = ProgressBar.VISIBLE
        if(id!!.isNotBlank())
        disposable =
            danceClassApiService.event("Bearer "+Properties.instance.token,id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result -> showResult(result as EventWithStudentsModel) },
                    { error -> showError(error.message) }
                )
    }
    private fun showResult(event: EventWithStudentsModel){
        this.item = event
        activity?.toolbar_layout?.title = this.item!!.name + " Students"
        this.studentList = item!!.students
        setupRecyclerView(this.peopleList)
        this.item!!.let {
            Properties.instance.eventSelected = it
        }
        bar.visibility = ProgressBar.GONE
    }
    private fun showError(message: String?) {
        bar.visibility = ProgressBar.GONE
        Toast.makeText(this.context, message, Toast.LENGTH_LONG).show()
    }
    private var id:String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                id = it.getString(ARG_ITEM_ID)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.students_event, container, false)
        properties = Properties.instance
        bar = view.findViewById(R.id.event_detail_bar)
        peopleList = view.findViewById(R.id.people_list)
        getEvent(this.id)

        if (event_detail_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }

        return view
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this.requireActivity(), studentList,this.item!! , twoPane)
    }
    companion object {
        const val ARG_ITEM_ID = "item_id"
    }

    class SimpleItemRecyclerViewAdapter(
        private val parentActivity: FragmentActivity,
        private val values: List<StudentModel>,
        private var event: EventWithStudentsModel,
        private val twoPane: Boolean
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as StudentModel
                val intent = Intent(v.context, QRCodeActivity::class.java).apply {
                    putExtra(QRCodeActivity.ARG_STUDENT_ID, item._id)
                    putExtra(QRCodeActivity.ARG_EVENT_ID,event._id)
                }
                parentActivity.startActivity(intent)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.people_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.contentView.text = item!!.name

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
