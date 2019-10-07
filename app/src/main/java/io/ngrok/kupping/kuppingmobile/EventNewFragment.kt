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
import android.content.Intent
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
    private lateinit var startInputEditText: TextInputEditText
    private lateinit var startFromInputEditText: TextInputEditText
    private lateinit var finishInputEditText: TextInputEditText
    private lateinit var finishToInputEditText: TextInputEditText
    private lateinit var nameInputEditText: TextInputEditText
    private lateinit var styleInputSpinner: Spinner
    private lateinit var aboutInputEditText: TextInputEditText
    private lateinit var zipCodeInputEditText: TextInputEditText
    private lateinit var countryInputEditText: TextInputEditText
    private lateinit var cityInputEditText: TextInputEditText
    private lateinit var addressInputEditText: TextInputEditText
    private var errorMsg:String = ""
    private var dateFormat = "dd/MM/yyyy"
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

        if(startInputEditText.text.toString().isNullOrEmpty()){
            requiredErrorFields.add("Start")
        }
        if(startFromInputEditText.text.toString().isNullOrBlank()){
            requiredErrorFields.add("Start From")
        }
        if(finishInputEditText.text.toString().isNullOrBlank()){
            requiredErrorFields.add("Finish")
        }
        if(finishToInputEditText.text.toString().isNullOrBlank()){
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
        }else{
            result = true
        }
        return result
    }
    private fun submit(view: View){
        if(validate(view)) {
            val dateStart =
                SimpleDateFormat(dateFormat).parse(startInputEditText.text.toString() + " " + startFromInputEditText.text.toString())
            val dateFinish =
                SimpleDateFormat(dateFormat).parse(finishInputEditText.text.toString() + " " + finishToInputEditText.text.toString())

            val eventModel = NewEventModel(
                nameInputEditText.text.toString(),
                aboutInputEditText.text.toString(),
                listStyle[styleInputSpinner.selectedItemPosition],
                dateStart.time,
                dateFinish.time,
                zipCodeInputEditText.text.toString(),
                countryInputEditText.text.toString(),
                cityInputEditText.text.toString(),
                addressInputEditText.text.toString()
            )
            bar.visibility = ProgressBar.VISIBLE
            disposable =
                danceClassApiService.createEvent("Bearer " + Properties.instance.token, eventModel)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { result -> showResult(view, result) },
                        { error -> showError(view, error.message) }
                    )
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
        var view = inflater.inflate(R.layout.event_new, container, false)
        bar = view.findViewById(R.id.event_new_bar)
        startInputEditText = view.findViewById(R.id.start_input_edit)
        startInputEditText.setText(SimpleDateFormat(dateFormat).format(System.currentTimeMillis()))
        finishInputEditText = view.findViewById(R.id.finish_input_edit)
        finishInputEditText.setText(SimpleDateFormat(dateFormat).format(System.currentTimeMillis()))
        startFromInputEditText = view.findViewById(R.id.start_from_input_edit)
        finishToInputEditText = view.findViewById(R.id.finish_to_input_edit)
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

        var cal = Calendar.getInstance()

        val dateStartSetListener = DatePickerDialog.OnDateSetListener {_, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val sdf = SimpleDateFormat(dateFormat, Locale.US)
            startInputEditText.setText(sdf.format(cal.time))
        }
        val dateFinishSetListener = DatePickerDialog.OnDateSetListener {_, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val sdf = SimpleDateFormat(dateFormat, Locale.US)
            finishInputEditText.setText(sdf.format(cal.time))
        }
        val startDatePicker = DatePickerDialog(
                context!!, dateStartSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        )
        val finishDatePicker = DatePickerDialog(
            context!!, dateFinishSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        startInputEditText.setOnClickListener {
            startDatePicker.show()
        }
        startInputEditText.setOnFocusChangeListener { _, b ->
            run {
                if(b) {
                    startDatePicker.show()
                }else{
                    startDatePicker.hide()
                }
            }
        }

        finishInputEditText.setOnClickListener {
            finishDatePicker.show()
        }
        finishInputEditText.setOnFocusChangeListener { _, b ->
            run {
                if(b) {
                    finishDatePicker.show()
                }else{
                    finishDatePicker.hide()
                }
            }
        }

        btnSave = view.findViewById(R.id.btn_save)
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
