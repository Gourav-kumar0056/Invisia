import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.invisia.MainActivity
import com.example.invisia.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient


const val channelId = "invisia_channel_id"
const val channelName = "invisia_channel_name"
@Composable
fun SendNotification() {
    val context = LocalContext.current
    var notificationSent by remember { mutableStateOf(false) }
    var numNotifications by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()

    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_logo) // app icon
        .setContentTitle("Notification Title")
        .setContentText("Notification Message")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    val notificationIntent = Intent(context, MainActivity::class.java)
    notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent,
        PendingIntent.FLAG_IMMUTABLE)
    notificationBuilder.setContentIntent(pendingIntent)

    context.createNotificationChannel(channelId, channelName)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        //Basic notification
        Button(
            onClick = {
                notificationBuilder.setContentText("Notification Message 1")
                with(NotificationManagerCompat.from(context)) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return@Button
                    }
                    notify(1, notificationBuilder.build())
                }
                notificationSent = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text("Notify 1")
        }

        //To show image in the notification
        Button(
            onClick = {
                if (isInternetPermissionGranted(context)) {
                    val imageUrl = "https://files.readme.io/001fc13-Sample2.png"

                    val imageLoader = ImageLoader.Builder(context).build()
                    val request = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .build()

                    scope.launch {
                        val result = imageLoader.execute(request)

                        if (result is SuccessResult) {
                            val imageBitmap = (result.drawable.toBitmap())

                            notificationBuilder.setContentText("Notification Message 2")
                            notificationBuilder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(imageBitmap))

                            with(NotificationManagerCompat.from(context)) {
                                notify(2, notificationBuilder.build())
                            }
                        }
                    }
                    notificationSent = true

                } else {
                    Toast.makeText(context, "Internet is not available", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text("Notify 2")
        }

        //To generate multiple notifications
        Button(
            onClick = {
                numNotifications = 0
                scope.launch(Dispatchers.IO) {
                    repeat(5) {
                        notificationBuilder.setContentText("Notification Message 3: $numNotifications")
                        numNotifications++
                        delay(12000) // 12 seconds delay (12,000 milliseconds)

                        with(NotificationManagerCompat.from(context)) {
                            notify(numNotifications, notificationBuilder.build())
                        }
                    }
                }
                notificationSent = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)

        ) {
            Text("Notify 3")
        }
    }
}



private fun Context.createNotificationChannel(channelId: String, channelName: String) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun isInternetPermissionGranted(context: Context): Boolean {
    val permission = Manifest.permission.INTERNET
    val granted = PackageManager.PERMISSION_GRANTED
    return ContextCompat.checkSelfPermission(context, permission) == granted
}
