package io.ngrok.kupping.kuppingmobile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.ngrok.kupping.kuppingmobile.models.EventModel
import io.ngrok.kupping.kuppingmobile.models.EventWithStudentsModel
import io.ngrok.kupping.kuppingmobile.services.DanceClassApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_event_detail.*
import kotlinx.android.synthetic.main.event_detail.*
import kotlinx.android.synthetic.main.event_detail.view.*
import kotlinx.android.synthetic.main.event_detail.view.event_detail

class EventDetailFragment : Fragment() {

    private lateinit var item: EventWithStudentsModel


    private val danceClassApiService by lazy {
        DanceClassApiService.create()
    }
    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }

    override fun onResume() {
        super.onResume()
    }
    private var disposable: Disposable? = null
    private fun getEvent(id: String?){
        if(id!!.isNotBlank())
        disposable =
            danceClassApiService.danceClass("Bearer "+Properties.instance.token,id)
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
        }
    }
    private fun showError(message: String?) {
        Toast.makeText(this.context, message, Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {

                getEvent(it.getString(ARG_ITEM_ID))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.event_detail, container, false)
    }

    companion object {
        const val ARG_ITEM_ID = "item_id"
    }
}
