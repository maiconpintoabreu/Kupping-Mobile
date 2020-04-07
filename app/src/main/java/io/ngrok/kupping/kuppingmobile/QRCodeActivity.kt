package io.ngrok.kupping.kuppingmobile

import android.R.attr.mimeType
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import io.ngrok.kupping.kuppingmobile.models.ImageModel
import io.ngrok.kupping.kuppingmobile.services.EventApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.qr_code_dialog_layout.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class QRCodeActivity : AppCompatActivity(){
    lateinit var eventId:String
    lateinit var studentId:String
    private lateinit var properties: Properties
    private lateinit var qrImage: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qr_code_dialog_layout)
        this.eventId = intent.getStringExtra(ARG_EVENT_ID).orEmpty()
        this.studentId = intent.getStringExtra(ARG_STUDENT_ID).orEmpty()
        Log.d("eventId",this.eventId)
        Log.d("studentId",this.studentId)
        properties = Properties.instance
        this.getQRCodeImage()
        share_qr_code_image.setOnClickListener {
            shareQRCode(this.qrImage);
        }
        delete_student.setOnClickListener {
            removePersonEvent()
        }

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
        getQRCodeImage()
    }
    private var disposable: Disposable? = null
    private fun getQRCodeImage(){
        disposable =
            danceClassApiService.qrcode("Bearer "+properties.token,this.eventId, this.studentId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result -> showResult(result as ImageModel) },
                    { error -> showError(error.message) }
                )
    }
    private fun showResult(imageString: ImageModel){
        val imageBytes = Base64.decode(imageString.image, Base64.DEFAULT)
        this.qrImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        qr_code_image.setImageBitmap(this.qrImage)
//
    }
    private fun showError(message: String?) {
        if (message != null) {
            Log.e("Connection ERROR",message)
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }
    private fun removePersonEvent(){
        finish()
    }
    private fun shareQRCode(decodedImage: Bitmap){
        var file = File(this.externalCacheDir?.absolutePath, "ticket"+this.eventId+".png")
        var fOut = FileOutputStream(file)
        decodedImage.compress(Bitmap.CompressFormat.PNG,80,fOut)
        fOut.close()
        file.setReadable(true,false)
        try {
            val uriToImage = FileProvider.getUriForFile(
                baseContext, applicationContext
                    .packageName.toString() + ".provider", file
            )
            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uriToImage)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.send_to)))
        }catch (e: FileNotFoundException){
            e.printStackTrace()
            Log.e("FileNotFoundException",e.message)
        }catch (e: IOException){
            e.printStackTrace()
            Log.e("IOException",e.message)
        }catch (e: Exception){
            e.printStackTrace()
            Log.e("Exception",e.message)
        }
    }
    companion object {
        const val ARG_STUDENT_ID = "student_id"
        const val ARG_EVENT_ID = "event_id"
    }
}
