package io.ngrok.kupping.kuppingmobile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import io.ngrok.kupping.kuppingmobile.models.EventModel
import io.ngrok.kupping.kuppingmobile.models.EventWithStudentsModel
import io.ngrok.kupping.kuppingmobile.services.DanceClassApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_event_detail.*
import kotlinx.android.synthetic.main.app_bar_event_list.*
import kotlinx.android.synthetic.main.event_detail.*
import kotlinx.android.synthetic.main.event_detail.view.*
import kotlinx.android.synthetic.main.event_detail.view.event_detail
import android.widget.DatePicker
import android.app.DatePickerDialog
import kotlinx.android.synthetic.main.event_new.*
import java.util.*
import javax.xml.datatype.DatatypeConstants.MONTHS


class EventNewFragment : Fragment() {

//    private lateinit var bar: ProgressBar

//    private val danceClassApiService by lazy {
//        DanceClassApiService.create()
//    }
//    override fun onPause() {
//        super.onPause()
////        disposable?.dispose()
//    }
//    private var disposable: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        var view =
//        bar = view.findViewById(R.id.event_new_bar)
//        bar.visibility = ProgressBar.GONE
        activity?.toolbar_layout?.title = getString(R.string.new_event)
        return inflater.inflate(R.layout.event_new, container, false)
    }
}
