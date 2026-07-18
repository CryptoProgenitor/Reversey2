package com.quokkalabs.thehunt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.quokkalabs.thehunt.ui.HuntApp

class MainActivity : ComponentActivity() {

    private val viewModel: HuntViewModel by viewModels()
    private var fakeScanReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HuntApp(viewModel)
        }

        // Debug-only fake scanning via adb (see TESTING.md). Never present in release builds.
        if (BuildConfig.DEBUG) {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    intent?.getStringExtra(EXTRA_CODE)?.let { viewModel.onScanned(it) }
                }
            }
            fakeScanReceiver = receiver
            ContextCompat.registerReceiver(
                this,
                receiver,
                IntentFilter(ACTION_FAKE_SCAN),
                ContextCompat.RECEIVER_EXPORTED,
            )
        }
    }

    override fun onDestroy() {
        fakeScanReceiver?.let { unregisterReceiver(it) }
        fakeScanReceiver = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_FAKE_SCAN = "com.quokkalabs.thehunt.FAKE_SCAN"
        const val EXTRA_CODE = "code"
    }
}
