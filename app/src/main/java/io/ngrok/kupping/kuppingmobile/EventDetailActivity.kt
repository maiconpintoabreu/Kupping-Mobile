package io.ngrok.kupping.kuppingmobile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_event_detail.*

class EventDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)
        setSupportActionBar(detail_toolbar)

        fab.setOnClickListener {
            run {
                val intent = Intent(this, CameraQRActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                this.startActivity(intent)
            }
        }
        edit_event_btn.setOnClickListener {
            run {
                val intent = Intent(this, CameraQRActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                this.startActivity(intent)
            }
        }
        edit_students_event_btn.setOnClickListener {
            run {
                val intent = Intent(this, CameraQRActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                this.startActivity(intent)
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

            supportFragmentManager.beginTransaction()
                .add(R.id.event_detail_container, fragment)
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
}
