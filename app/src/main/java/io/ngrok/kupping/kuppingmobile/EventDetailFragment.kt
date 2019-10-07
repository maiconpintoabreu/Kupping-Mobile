package io.ngrok.kupping.kuppingmobile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import io.ngrok.kupping.kuppingmobile.models.EventWithStudentsModel
import io.ngrok.kupping.kuppingmobile.services.EventApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_event_detail.*
import kotlinx.android.synthetic.main.event_detail.*

class EventDetailFragment : Fragment() {

    lateinit var item: EventWithStudentsModel
    private lateinit var bar: ProgressBar

    private val danceClassApiService by lazy {
        EventApiService.create()
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
            event_detail.text = item.about
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.event_detail, container, false)
        bar = view.findViewById(R.id.event_detail_bar)
        getEvent(id)
        return view
    }

    companion object {
        const val ARG_ITEM_ID = "item_id"
    }
}
