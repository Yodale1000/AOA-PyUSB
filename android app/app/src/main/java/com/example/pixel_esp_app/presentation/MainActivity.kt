/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.pixel_esp_app.presentation

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbAccessory
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.pixel_esp_app.R
import com.example.pixel_esp_app.presentation.theme.Pixel_ESP_AppTheme


private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

private val usbReceiver = object : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("debugg", "test")
        if (ACTION_USB_PERMISSION == intent.action) {
            synchronized(this) {
                val accessory: UsbAccessory? = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY)

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    accessory?.apply {
                        // call method to set up accessory communication
                    }
                } else {
                    Log.d("debugg", "permission denied for accessory $accessory")
                }
            }
        }
    }
}


class MainActivity : ComponentActivity() {
    private lateinit var accessory: UsbAccessory

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val accessoryList: Array<out UsbAccessory>? = manager.accessoryList
        if (!accessoryList.isNullOrEmpty()) {
            accessory = accessoryList[0]
            registerReceiver(usbReceiver, IntentFilter(ACTION_USB_PERMISSION))
            val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
                applicationContext, 0, Intent(ACTION_USB_PERMISSION),
                PendingIntent.FLAG_IMMUTABLE
            )
            manager.requestPermission(accessory, pendingIntent)
        }

        setContent {
            WearApp("Android: ${accessory.model}")
        }


    }
}

@Composable
fun WearApp(greetingName: String) {
    Pixel_ESP_AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Greeting(greetingName = greetingName)
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}

