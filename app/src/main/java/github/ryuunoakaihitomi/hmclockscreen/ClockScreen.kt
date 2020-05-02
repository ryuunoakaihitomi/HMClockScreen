package github.ryuunoakaihitomi.hmclockscreen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.graphics.Typeface
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.Toast
import github.ryuunoakaihitomi.hmclockscreen.Utils.bundle2String4Display
import github.ryuunoakaihitomi.hmclockscreen.Utils.percentage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.system.exitProcess

class ClockScreen : Activity(), View.OnClickListener {

    companion object {
        private const val TAG = "ClockScreen"
    }

    private var batteryInfo = Bundle()

    private val broadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_TIME_TICK -> synchronizeClock()
                Intent.ACTION_BATTERY_CHANGED -> {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                    val batteryLevelPercent = percentage(level, scale)
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val isBatteryExist = status != BatteryManager.BATTERY_STATUS_UNKNOWN
                    if (!isBatteryExist) {
                        Log.i(TAG, "onReceive: Battery is not exists")
                        battery_label.visibility = View.INVISIBLE
                        return
                    } else battery_label.visibility = View.VISIBLE
                    // https://developer.android.com/training/monitoring-device-state/battery-monitoring#DetermineChargeState
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                            || status == BatteryManager.BATTERY_STATUS_FULL
                    // BatteryManager.EXTRA_ICON_SMALL is too ugly.
                    val symbol = if (isCharging) "🔌" else "🔋"
                    batteryInfo = intent.extras ?: Bundle()
                    battery_label.text = "$symbol$batteryLevelPercent%"
                }
                Intent.ACTION_BATTERY_LOW -> {
                    Log.w(TAG, "onReceive: Intent.ACTION_BATTERY_LOW info = ${bundle2String4Display(batteryInfo)}")
                    // Exit to protect battery.
                    exitProcess(0)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        configSysUiFlags()
        // Center the time separator completely.
        clock_view.setTypeface(Typeface.createFromAsset(assets, "AndroidClock.ttf"), Typeface.BOLD)
        clock_view.setOnClickListener(this)
        battery_label.setOnLongClickListener {
            Toast.makeText(application, bundle2String4Display(batteryInfo), Toast.LENGTH_LONG).show()
            true
        }
        // Too difficult to select the text before 23, copy it directly.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            clock_view.setOnLongClickListener {
                it.clearFocus()
                (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                        .setPrimaryClip(ClipData.newPlainText(null, clock_view.text))
                Toast.makeText(application,
                        // "Text copied to clipboard."
                        resources.getIdentifier("text_copied", "string", "android"),
                        Toast.LENGTH_SHORT).show()
                true
            }
        }
    }

    override fun onStart() {
        super.onStart()
        synchronizeClock()
        // ClockView seems not to automatically get the focus on 9+.
        // We have to use double-click to show calendar dialog at the first time while app is running.
        clock_view.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_TIME_TICK)
        filter.addAction(Intent.ACTION_BATTERY_CHANGED)
        filter.addAction(Intent.ACTION_BATTERY_LOW)
        registerReceiver(broadcastReceiver, filter)
    }

    override fun onPause() {
        Log.i(TAG, "onPause: [  ]")
        unregisterReceiver(broadcastReceiver)
        super.onPause()
        // WHY DISTURB ME?
        exitProcess(0)
    }

    override fun onClick(v: View) {
        if (v.id == clock_view.id) CalendarDialog.create(this) { configSysUiFlags() }
    }

    private fun configSysUiFlags() {
        var flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        // On 5.0+,the flags will lead the ui to show a blank area for nav bar instead of hiding it completely.
        // But on 4.4,it will prevent the contraction of clockView's font when the calendar dialog is showing.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH)
            flags += View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.decorView.systemUiVisibility = flags
    }

    private fun synchronizeClock() {
        clock_view.text = DateFormat.format("HH:mm", Date())
    }
}
