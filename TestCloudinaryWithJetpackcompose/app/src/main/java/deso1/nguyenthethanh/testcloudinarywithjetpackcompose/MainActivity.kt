package deso1.nguyenthethanh.testcloudinarywithjetpackcompose

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.cloudinary.android.MediaManager
import deso1.nguyenthethanh.testcloudinarywithjetpackcompose.ui.theme.TestCloudinaryWithJetpackcomposeTheme



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Cloudinary
        val config = mapOf(
            "cloud_name" to "Thanh",
            "api_key" to "466154593341231",
            "api_secret" to "51yg5kMNxT9gxB8fmvRG_33Zzb0"
        )
        MediaManager.init(this, config)

        setContent {
            ImageUploaderScreen()
        }
    }
}

@Composable
fun ImageUploaderScreen() {
    val context = LocalContext.current
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) {
            Log.e("ImageUploaderScreen", "No image selected")
            return@rememberLauncherForActivityResult
        }
        Log.d("ImageUploaderScreen", "Image selected: $uri")
        isUploading = true
        CloudinaryHelper.uploadImage(uri, { url ->
            Log.d("ImageUploaderScreen", "Upload successful. URL: $url")
            imageUrl = url
            isUploading = false
        }, { error ->
            Log.e("ImageUploaderScreen", "Upload failed: $error")
            isUploading = false
        })
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("ImageUploaderScreen", "Permission granted")
            imagePickerLauncher.launch("image/*")
        } else {
            Log.e("ImageUploaderScreen", "Permission denied")
            // Show a message to the user
            Toast.makeText(context, "Permission denied. Please enable it in settings.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            // Check and request permission before launching the image picker
            permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES) // Use READ_MEDIA_IMAGES for Android 13+
        }) {
            Text("Pick and Upload Image")
        }

        if (isUploading) {
            CircularProgressIndicator()
        }

        imageUrl?.let { url ->
            Image(
                painter = rememberAsyncImagePainter(url),
                contentDescription = "Uploaded Image",
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(2.dp, Color.Gray, RoundedCornerShape(10.dp))
            )
        }
    }
}