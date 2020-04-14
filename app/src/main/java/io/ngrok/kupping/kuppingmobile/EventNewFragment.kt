package io.ngrok.kupping.kuppingmobile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ngrok.kupping.kuppingmobile.services.EventApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_event_detail.*
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.view.MotionEvent
import android.widget.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import io.ngrok.kupping.kuppingmobile.models.*
import io.ngrok.kupping.kuppingmobile.services.StyleApiService
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger
import kotlin.math.log


class EventNewFragment : Fragment() {

    private lateinit var bar: ProgressBar
    private lateinit var btnSave: Button
    private lateinit var startInputEdit: TextInputEditText
    private lateinit var finishInputEdit: TextInputEditText
    private lateinit var startFromInputEdit: TextInputEditText
    private lateinit var finishToInputEdit: TextInputEditText
    private lateinit var nameInputEditText: TextInputEditText
    private lateinit var styleInputSpinner: Spinner
    private lateinit var aboutInputEditText: TextInputEditText
    private lateinit var zipCodeInputEditText: TextInputEditText
    private lateinit var countryInputEditText: TextInputEditText
    private lateinit var cityInputEditText: TextInputEditText
    private lateinit var addressInputEditText: TextInputEditText
    private var errorMsg:String = ""
    private var dateFormat = "dd-MM-yyyy HH:mm"
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
                var styleId: String
                if(listStyle[styleInputSpinner.selectedItemPosition] != null){
                    styleId = listStyle[styleInputSpinner.selectedItemPosition]._id
                }else{
                    styleId = listStyle[0]._id
                }
                val eventModel = NewEventModel(
                    nameInputEditText.text.toString(),
                    aboutInputEditText.text.toString(),
                    styleId,
                    dateStart.time,
                    dateFinish.time,
                    PlaceModel(
                        zipCodeInputEditText.text.toString(),
                        countryInputEditText.text.toString(),
                        cityInputEditText.text.toString(),
                        addressInputEditText.text.toString()
                    )
                )
                bar.visibility = ProgressBar.VISIBLE
                disposable =
                    danceClassApiService.createEvent(
                        "Bearer " + Properties.instance.token,
                        eventModel
                    )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            { result -> showResult(view, result) },
                            { error -> showError(view, error.message) }
                        )
            }
        }else{
            Snackbar.make(view, errorMsg, Snackbar.LENGTH_LONG)
                .setAction("Submit-Error", null).show()
        }
    }
    private fun showResult(view: View,response: ResponseModel){
        bar.visibility = ProgressBar.GONE
        Snackbar.make(view, response.message, Snackbar.LENGTH_LONG)
            .setAction("Event-Created", null).show()
        val intent = Intent(view.context, EventListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
    private fun showError(view: View,message: String?) {
        bar.visibility = ProgressBar.GONE
        Snackbar.make(view, message.toString(), Snackbar.LENGTH_LONG)
            .setAction("Event-Create-Error", null).show()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.event_detail, container, false)
        bar = view.findViewById(R.id.event_detail_bar)
        nameInputEditText = view.findViewById(R.id.name_input_edit)
        styleInputSpinner = view.findViewById(R.id.style_input)
        aboutInputEditText = view.findViewById(R.id.about_input_edit)
        zipCodeInputEditText = view.findViewById(R.id.zip_code_input_edit)
        countryInputEditText = view.findViewById(R.id.country_input_edit)
        cityInputEditText = view.findViewById(R.id.city_input_edit)
        addressInputEditText = view.findViewById(R.id.address_input_edit)

        val aa = ArrayAdapter(view.context, android.R.layout.simple_spinner_item, mutableListOf<String>())

        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
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
                        }
                    },
                    { error -> showError(view,error.message) }
                )

        startInputEdit = view.findViewById(R.id.start_input_edit)
        finishInputEdit = view.findViewById(R.id.finish_input_edit)
        startFromInputEdit = view.findViewById(R.id.start_from_input_edit)
        finishToInputEdit = view.findViewById(R.id.finish_to_input_edit)

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
}
