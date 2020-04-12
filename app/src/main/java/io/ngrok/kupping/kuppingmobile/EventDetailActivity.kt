package io.ngrok.kupping.kuppingmobile

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.fragment.app.DialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.ngrok.kupping.kuppingmobile.models.NewStudentModel
import io.ngrok.kupping.kuppingmobile.models.ResponseModel
import io.ngrok.kupping.kuppingmobile.services.EventApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_event_detail.*

class EventDetailActivity : AppCompatActivity(),
    CustomDialogFragment.NoticeDialogListener{

    private lateinit var properties: Properties
    private lateinit var eventid: String
    private lateinit var mode: Number
    /*
    * 1 - Event Detail
    * 2 - People List
     */

    private val eventApiService by lazy {
        EventApiService.create()
    }
    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }

    override fun onResume() {
        super.onResume()
    }
    private fun showResult(response: ResponseModel){
        if(this.mode == 1){
            setStudentFragment(null)
        }else{
            setEventFragment(null)
        }
        Toast.makeText(applicationContext, response.message, Toast.LENGTH_LONG).show()
    }
    private fun addPerson(name: String, email: String){
        disposable =
            eventApiService.booking("Bearer "+properties.token, this.eventid,NewStudentModel(name,email))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result -> showResult(result as ResponseModel) },
                    { error -> showError(error.message) }
                )
    }
    private fun showError(message: String?) {
        if (message != null) {
            Log.e("Connection ERROR",message)
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }
    private var disposable: Disposable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)
        setSupportActionBar(detail_toolbar)
        properties = Properties.instance
        this.mode = 1
        this.eventid = intent.getStringExtra(EventDetailFragment.ARG_ITEM_ID).orEmpty()
        fab.setOnClickListener {
            run {
                val intent = Intent(this, CameraQRActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                this.startActivity(intent)
            }
        }
        edit_students_event_btn.setOnClickListener {
            setStudentFragment(it)
        }
        edit_event_btn.setOnClickListener {
            setEventFragment(it)
        }
        add_new_student.setOnClickListener {
            run {
                showDialog()
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (savedInstanceState == null) {
            val fragment = EventDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(
                        EventDetailFragment.ARG_ITEM_ID,
                        intent.getStringExtra(EventDetailFragment.ARG_ITEM_ID)
                    )
                }
            }
            edit_event_btn.visibility = FloatingActionButton.GONE
            add_new_student.visibility = FloatingActionButton.GONE

            supportFragmentManager.beginTransaction()
                .add(R.id.event_detail_container, fragment)
                .commit()
        }
    }
    private fun setStudentFragment(it: View?){
        run {
            val fragment = StudentsEventFragment().apply {
                arguments = Bundle().apply {
                    putString(
                        StudentsEventFragment.ARG_ITEM_ID,
                        intent.getStringExtra(StudentsEventFragment.ARG_ITEM_ID)
                    )
                }
            }
            edit_event_btn.visibility = FloatingActionButton.VISIBLE
            add_new_student.visibility = FloatingActionButton.VISIBLE
            edit_students_event_btn.visibility = FloatingActionButton.GONE
            fab.visibility = FloatingActionButton.GONE
            supportFragmentManager.beginTransaction().replace(R.id.event_detail_container, fragment)
                .commit()
        }
    }
    private fun setEventFragment(it: View?){
        run {
            val fragment = EventDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(
                        EventDetailFragment.ARG_ITEM_ID,
                        intent.getStringExtra(EventDetailFragment.ARG_ITEM_ID)
                    )
                }
            }
            edit_event_btn.visibility = FloatingActionButton.GONE
            add_new_student.visibility = FloatingActionButton.GONE
            edit_students_event_btn.visibility = FloatingActionButton.VISIBLE
            fab.visibility = FloatingActionButton.VISIBLE
            supportFragmentManager.beginTransaction().replace(R.id.event_detail_container, fragment)
                .commit()
        }

    }
    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpTo(this, Intent(this, EventListActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    private fun showDialog() {
        val fragmentManager = supportFragmentManager
        val newFragment = CustomDialogFragment()
        newFragment.show(fragmentManager, "dialog")
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, name: String, email: String) {
        if(name.isNotEmpty() && email.isNotEmpty()){
            addPerson(name,email)
            dialog.dismiss()
        }else{
            Toast.makeText(applicationContext, "Not Saved because it is missing information", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }
}
class CustomDialogFragment : DialogFragment() {

    private lateinit var listener: NoticeDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Build the dialog and set up the button click handlers
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            var mView = inflater.inflate(R.layout.new_people_dialog, null)
            var name = mView.findViewById<EditText>(R.id.name)
            var email = mView.findViewById<EditText>(R.id.email)
            builder.setView(mView)
                .setPositiveButton("Add",
                    DialogInterface.OnClickListener {_, _ ->
                        // Send the positive button event back to the host activity
                        listener.onDialogPositiveClick(this,name.text.toString(), email.text.toString())
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
        fun onDialogPositiveClick(dialog: DialogFragment, name: String,email: String)
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

