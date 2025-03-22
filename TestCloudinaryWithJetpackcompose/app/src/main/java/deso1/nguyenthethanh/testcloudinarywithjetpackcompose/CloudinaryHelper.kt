package deso1.nguyenthethanh.testcloudinarywithjetpackcompose

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.lang.Error
import java.util.UUID

object CloudinaryHelper {
    fun uploadImage(uri: Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val requestId = MediaManager.get().upload(uri)
            .option("public_id", "upload_${UUID.randomUUID()}") // Optional: Set a unique public ID
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d("CloudinaryHelper", "Upload started")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    Log.d("CloudinaryHelper", "Upload progress: $bytes/$totalBytes")
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as String
                    Log.d("CloudinaryHelper", "Upload successful. URL: $url")
                    onSuccess(url)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e("CloudinaryHelper", "Upload failed: ${error.description}")
                    onError(error.description)
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    Log.e("CloudinaryHelper", "Upload rescheduled: ${error.description}")
                    onError("Upload rescheduled due to error: ${error.description}")
                }
            })
            .dispatch()
    }
}