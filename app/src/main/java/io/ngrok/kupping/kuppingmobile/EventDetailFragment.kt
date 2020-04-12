package io.ngrok.kupping.kuppingmobile

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import io.ngrok.kupping.kuppingmobile.models.*
import io.ngrok.kupping.kuppingmobile.services.EventApiService
import io.ngrok.kupping.kuppingmobile.services.StyleApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_event_detail.*
import kotlinx.android.synthetic.main.event_detail.*
import java.text.SimpleDateFormat
import java.util.*

class EventDetailFragment : Fragment() {

    private val sdf = java.text.SimpleDateFormat("dd-MM-yyyy")
    private val sdfTime = java.text.SimpleDateFormat("HH:mm")
    private var dateFormat = "dd-MM-yyyy HH:mm"
    lateinit var item: EventWithStudentsModel
    private lateinit var styleInputSpinner: Spinner
    private lateinit var bar: ProgressBar
    private lateinit var startInputEdit: TextInputEditText
    private lateinit var finishInputEdit: TextInputEditText
    private lateinit var startFromInputEdit: TextInputEditText
    private lateinit var finishToInputEdit: TextInputEditText
    private lateinit var nameInputEditText: TextInputEditText

    private lateinit var aboutInputEditText: TextInputEditText
    private lateinit var zipCodeInputEditText: TextInputEditText
    private lateinit var countryInputEditText: TextInputEditText
    private lateinit var cityInputEditText: TextInputEditText
    private lateinit var addressInputEditText: TextInputEditText

    private lateinit var btnSave: Button
    private var errorMsg:String = ""

    private var listStyle: List<StyleModel> = listOf()

    private val danceClassApiService by lazy {
        EventApiService.create()
    }
    private val styleApiService by lazy {
        StyleApiService.create()
    }
    override fun onPause() {
        super.onPause()
        disposable?.dispose()
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
        item = event
        activity?.toolbar_layout?.title = item.name
        item.let {
            val startNumber = java.util.Date(item.fromDate)
            val finishNumber = java.util.Date(item.toDate)
            var startDate:String = sdf.format(startNumber)
            var startTime:String = sdfTime.format(startNumber)
            var finishDate:String = sdf.format(finishNumber)
            var finishTime:String = sdfTime.format(finishNumber)
            start_input_edit.setText(startDate)
            start_from_input_edit.setText(startTime)
            finish_input_edit.setText(finishDate)
            finish_to_input_edit.setText(finishTime)
            name_input_edit.setText(item.name)
            if(item.place != null) {
                zip_code_input_edit.setText(item.place.zipcode)
                country_input_edit.setText(item.place.country)
                city_input_edit.setText(item.place.city)
                address_input_edit.setText(item.place.description)
            }else{
                Log.d("Event Test",item.toString())
            }
            about_input_edit.setText(item.about)
                styleInputSpinner.setSelection(listStyle.indexOfFirst { x -> x._id == item.style._id })
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
        var view = inflater.inflate(R.layout.event_detail, container, false)

        val aa = ArrayAdapter(view.context, android.R.layout.simple_spinner_item, mutableListOf<String>())

        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        styleInputSpinner = view.findViewById(R.id.style_input)
        styleInputSpinner.adapter = aa
        disposable =
            styleApiService.styles("Bearer "+Properties.instance.token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        run {
                            listStyle = result
                            aa.clear()
                            aa.addAll(listStyle.map { it.name })
                            getEvent(this.id)
                        }
                    },
                    { error -> showError(view!!,error.message) }
                )
        bar = view.findViewById(R.id.event_detail_bar)
        startInputEdit = view.findViewById(R.id.start_input_edit)
        finishInputEdit = view.findViewById(R.id.finish_input_edit)
        startFromInputEdit = view.findViewById(R.id.start_from_input_edit)
        finishToInputEdit = view.findViewById(R.id.finish_to_input_edit)
        nameInputEditText = view.findViewById(R.id.name_input_edit)
        aboutInputEditText = view.findViewById(R.id.about_input_edit)
        zipCodeInputEditText = view.findViewById(R.id.zip_code_input_edit)
        countryInputEditText = view.findViewById(R.id.country_input_edit)
        cityInputEditText = view.findViewById(R.id.city_input_edit)
        addressInputEditText = view.findViewById(R.id.address_input_edit)


        var cal = Calendar.getInstance()

        var calTime = Calendar.getInstance()

        val dateStartSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd-MM-yyyy" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            startInputEdit.setText(sdf.format(cal.time))

        }
        val dateFinishSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd-MM-yyyy" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            finishInputEdit.setText(sdf.format(cal.time))

        }
        val timeStartSetListener = TimePickerDialog.OnTimeSetListener { _, hours, minute ->
            calTime.set(Calendar.HOUR_OF_DAY, hours)
            calTime.set(Calendar.MINUTE, minute)
            startFromInputEdit.setText(SimpleDateFormat("HH:mm").format(calTime.time))

        }
        val timeFinishSetListener= TimePickerDialog.OnTimeSetListener { _, hours, minute ->
            calTime.set(Calendar.HOUR_OF_DAY, hours)
            calTime.set(Calendar.MINUTE, minute)

            finishToInputEdit.setText(SimpleDateFormat("HH:mm").format(calTime.time))

        }
        startInputEdit.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    DatePickerDialog(v.context, dateStartSetListener,
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)).show()

                }
            }

            v?.onTouchEvent(event) ?: true
        }
        finishInputEdit.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    DatePickerDialog(v.context, dateFinishSetListener,
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)).show()

                }
            }

            v?.onTouchEvent(event) ?: true
        }
        startFromInputEdit.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    TimePickerDialog(v.context, timeStartSetListener,
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE),true).show()

                }
            }

            v?.onTouchEvent(event) ?: true
        }
        finishToInputEdit.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    TimePickerDialog(v.context, timeFinishSetListener,
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE),true).show()

                }
            }

            v?.onTouchEvent(event) ?: true
        }

        btnSave = view.findViewById(R.id.btn_save_event)
        bar.visibility = ProgressBar.GONE
        activity?.toolbar_layout?.title = getString(R.string.new_event)
        btnSave.setOnClickListener {
            run {
                submit(it)
            }
        }
        return view
    }
    private fun showError(view: View,message: String?) {
        bar.visibility = ProgressBar.GONE
        Snackbar.make(view, message.toString(), Snackbar.LENGTH_LONG)
            .setAction("Event-Create-Error", null).show()
    }

    private fun validate(view: View): Boolean{
        var result: Boolean
        val requiredErrorFields:ArrayList<String> = ArrayList()

        if(startInputEdit.text.toString().isNullOrEmpty()){
            requiredErrorFields.add("Start")
        }
        if(startFromInputEdit.text.toString().isNullOrBlank()){
            requiredErrorFields.add("Start From")
        }
        if(finishInputEdit.text.toString().isNullOrBlank()){
            requiredErrorFields.add("Finish")
        }
        if(finishToInputEdit.text.toString().isNullOrBlank()){
            requiredErrorFields.add("Finish To")
        }
        if(nameInputEditText.text.toString().isNullOrEmpty()){
            requiredErrorFields.add("Name")
        }
        if(aboutInputEditText.text.toString().isNullOrBlank()){
            requiredErrorFields.add("About")
        }

        if(styleInputSpinner.selectedItemPosition < 0){
            requiredErrorFields.add("Style")
        }
        if(zipCodeInputEditText.text.toString().isNullOrBlank()){
            requiredErrorFields.add("ZipCode")
        }
        if(countryInputEditText.text.toString().isNullOrBlank()){
            requiredErrorFields.add("Country")
        }
        if(cityInputEditText.text.toString().isNullOrBlank()){
            requiredErrorFields.add("City")
        }
        if(addressInputEditText.text.toString().isNullOrBlank()){
            requiredErrorFields.add("Address")
        }
        if(requiredErrorFields.isNotEmpty()){
            result = false
            errorMsg = "Required fields: "+requiredErrorFields.joinToString()
            Snackbar.make(view, errorMsg, Snackbar.LENGTH_LONG)
                .setAction("Error-required-fields", null).show()
        }else{
            result = true
        }
        return result
    }
    private fun submit(view: View){
        if(validate(view)) {
            val dateStart =
                SimpleDateFormat(dateFormat).parse(startInputEdit.text.toString() + " " + startFromInputEdit.text.toString())
            val dateFinish =
                SimpleDateFormat(dateFormat).parse(finishInputEdit.text.toString() + " " + finishToInputEdit.text.toString())
            if(dateStart != null && dateFinish != null) {
                var style: StyleModel
                if(listStyle[styleInputSpinner.selectedItemPosition] != null){
                    style = listStyle[styleInputSpinner.selectedItemPosition]
                }else{
                    style = listStyle[0]
                }
                val eventModel = EventWithStudentsModel(
                    this.item._id,
                    style,
                    dateStart.time,
                    dateFinish.time,
                    aboutInputEditText.text.toString(),
                    nameInputEditText.text.toString(),
                    this.item.students,
                    PlaceModel(
                        zipCodeInputEditText.text.toString(),
                        countryInputEditText.text.toString(),
                        cityInputEditText.text.toString(),
                        addressInputEditText.text.toString()
                    )
                )
                bar.visibility = ProgressBar.VISIBLE
                disposable =
                    danceClassApiService.updateEvent(
                        "Bearer " + Properties.instance.token,this.item._id,
                        eventModel
                    )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            { result -> showUpdateResult(view, result) },
                            { error -> showError(view, error.message) }
                        )
            }
        }else{
            Snackbar.make(view, errorMsg, Snackbar.LENGTH_LONG)
                .setAction("Submit-Error", null).show()
        }
    }
    private fun showUpdateResult(view: View,result: ResponseModel) {
        bar.visibility = ProgressBar.GONE
        Snackbar.make(view, result.message, Snackbar.LENGTH_LONG)
            .setAction("Event Updated", null).show()
    }

    companion object {
        const val ARG_ITEM_ID = "item_id"
    }
}
