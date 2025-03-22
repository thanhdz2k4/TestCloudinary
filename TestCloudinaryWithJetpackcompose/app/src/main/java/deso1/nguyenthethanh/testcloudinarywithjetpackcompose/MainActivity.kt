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

// Lớp MainActivity là Activity chính của ứng dụng
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {  // Phương thức khởi tạo Activity
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()  // Bật hiển thị toàn màn hình không có thanh viền

        // Cấu hình Cloudinary
        val config = mapOf(
            "cloud_name" to "",  // Tên cloud trên Cloudinary
            "api_key" to "",  // API Key để xác thực
            "api_secret" to ""  // API Secret (không nên để lộ mã này)
        )
        MediaManager.init(this, config)  // Khởi tạo Cloudinary với cấu hình trên

        setContent {
            ImageUploaderScreen()  // Hiển thị màn hình tải ảnh lên
        }
    }
}

// Hàm Composable để tạo giao diện tải ảnh lên Cloudinary
@Composable
fun ImageUploaderScreen() {
    val context = LocalContext.current  // Lấy context hiện tại
    var imageUrl by remember { mutableStateOf<String?>(null) }  // Biến trạng thái chứa URL của ảnh sau khi tải lên
    var isUploading by remember { mutableStateOf(false) }  // Biến trạng thái theo dõi tiến trình tải ảnh

    // Khởi tạo trình chọn ảnh từ bộ nhớ thiết bị
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) {  // Kiểm tra nếu không có ảnh nào được chọn
            Log.e("ImageUploaderScreen", "No image selected")  // Ghi log lỗi
            return@rememberLauncherForActivityResult
        }
        Log.d("ImageUploaderScreen", "Image selected: $uri")  // Ghi log đường dẫn ảnh được chọn
        isUploading = true  // Đánh dấu bắt đầu quá trình tải lên
        CloudinaryHelper.uploadImage(uri, { url ->  // Gọi phương thức tải ảnh lên Cloudinary
            Log.d("ImageUploaderScreen", "Upload successful. URL: $url")  // Ghi log khi tải lên thành công
            imageUrl = url  // Cập nhật trạng thái URL của ảnh
            isUploading = false  // Đánh dấu hoàn tất tải lên
        }, { error ->
            Log.e("ImageUploaderScreen", "Upload failed: $error")  // Ghi log khi tải lên thất bại
            isUploading = false  // Đánh dấu kết thúc tải lên dù thất bại
        })
    }

    // Khởi tạo trình xin quyền truy cập ảnh
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->  // Kiểm tra kết quả xin quyền
        if (isGranted) {  // Nếu được cấp quyền
            Log.d("ImageUploaderScreen", "Permission granted")  // Ghi log quyền được cấp
            imagePickerLauncher.launch("image/*")  // Mở trình chọn ảnh
        } else {
            Log.e("ImageUploaderScreen", "Permission denied")  // Ghi log khi quyền bị từ chối
            Toast.makeText(context, "Permission denied. Please enable it in settings.", Toast.LENGTH_SHORT).show()
            // Hiển thị thông báo yêu cầu cấp quyền
        }
    }

    // Giao diện màn hình tải ảnh
    Column(
        modifier = Modifier.fillMaxSize(),  // Chiếm toàn bộ kích thước màn hình
        horizontalAlignment = Alignment.CenterHorizontally,  // Căn giữa theo chiều ngang
        verticalArrangement = Arrangement.Center  // Căn giữa theo chiều dọc
    ) {
        Button(onClick = {
            // Kiểm tra và xin quyền trước khi chọn ảnh
            permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
            // Sử dụng quyền READ_MEDIA_IMAGES cho Android 13+
        }) {
            Text("Pick and Upload Image")  // Nút chọn ảnh
        }

        if (isUploading) {  // Nếu đang tải ảnh, hiển thị vòng tròn tiến trình
            CircularProgressIndicator()
        }

        imageUrl?.let { url ->  // Nếu đã có ảnh tải lên, hiển thị ảnh
            Image(
                painter = rememberAsyncImagePainter(url),  // Tải ảnh từ URL
                contentDescription = "Uploaded Image",  // Mô tả ảnh
                modifier = Modifier
                    .size(200.dp)  // Kích thước ảnh 200x200 dp
                    .clip(RoundedCornerShape(10.dp))  // Bo góc ảnh
                    .border(2.dp, Color.Gray, RoundedCornerShape(10.dp))  // Viền xám quanh ảnh
            )
        }
    }
}
