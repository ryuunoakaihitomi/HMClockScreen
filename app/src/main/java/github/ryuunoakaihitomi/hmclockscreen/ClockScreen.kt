package github.ryuunoakaihitomi.hmclockscreen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.graphics.Typeface
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow
import kotlin.system.exitProcess

class ClockScreen : Activity(), View.OnClickListener {

    private val loadCalendar by lazy {
        Log.v(TAG, "loadCalendar")
        CalendarDialog.create(this) { configSysUiFlags() }
    }

    private val hmFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private lateinit var mBatteryInfo: Bundle

    private val broadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_TIME_TICK -> synchronizeClock()
                // Open app -> Press power btn -> Screen Off -> Press power btn -> Screen On -> Unlock ->
                // (Navigator bar will show again).
                Intent.ACTION_USER_PRESENT -> configSysUiFlags()
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
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                            || status == BatteryManager.BATTERY_STATUS_FULL
                    // BatteryManager.EXTRA_ICON_SMALL is too ugly.
                    val symbol = if (isCharging) "🔌" else "🔋"
                    mBatteryInfo = intent.extras ?: Bundle()
                    battery_label.text = "$symbol$batteryLevelPercent%"
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
            Toast.makeText(application, bundle2String4Display(mBatteryInfo), Toast.LENGTH_LONG).show()
            true
        }
        // Too difficult to select the text before 23, copy it directly.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            clock_view.setOnLongClickListener {
                it.clearFocus()
                (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                        .setPrimaryClip(ClipData.newPlainText(null, clock_view.text))
                // text_copied: System string resource: "Text copied to clipboard."
                Toast.makeText(application,
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
        filter.addAction(Intent.ACTION_USER_PRESENT)
        filter.addAction(Intent.ACTION_BATTERY_CHANGED)
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
        if (v.id == clock_view.id) if (loadCalendar) CalendarDialog.show()
    }

    companion object {
        private const val TAG = "ClockScreen"
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
        clock_view.text = hmFormat.format(Date())
    }

    override fun onDestroy() {
        super.onDestroy()
        CalendarDialog.destroy()
    }

    override fun onTrimMemory(level: Int) {
        var levelStr: String = level.toString()
        for (f in ComponentCallbacks2::class.java.fields)
            if (f.get(null) == level) {
                levelStr = f.name
                break
            }
        Log.w(TAG, "onTrimMemory: $levelStr")
        super.onTrimMemory(level)
    }

    private fun percentage(part: Int, total: Int): Int {
        val ratio: BigDecimal = BigDecimal.valueOf(part.toFloat() / total * 10.0.pow(2))
        return ratio.setScale(0, RoundingMode.HALF_UP).toInt()
    }

    private fun bundle2String4Display(bundle: Bundle): String {
        if (BuildConfig.DEBUG) Log.d(TAG, "bundle2String4Display: $bundle")
        var extrasString = String()
        for (key in bundle.keySet()) extrasString += "$key: ${bundle[key]}${System.lineSeparator()}"
        return extrasString
    }
}
