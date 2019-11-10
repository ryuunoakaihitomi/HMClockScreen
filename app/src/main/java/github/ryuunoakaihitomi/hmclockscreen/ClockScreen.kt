package github.ryuunoakaihitomi.hmclockscreen

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class ClockScreen : Activity(), View.OnClickListener {

    private lateinit var clockView: TextView

    private val loadCalendar by lazy {
        Log.v(TAG, "loadCalendar")
        CalendarDialog.create(this) { configSysUiFlags() }
    }

    private val hmFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_TIME_TICK -> synchronizeClock()
                // Open app -> Press power btn -> Screen Off -> Press power btn -> Screen On -> Unlock ->
                // (Navigator bar will show again).
                Intent.ACTION_USER_PRESENT -> configSysUiFlags()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        configSysUiFlags()
        clockView = findViewById(R.id.clock_view)
        // Center the time separator completely.
        clockView.setTypeface(Typeface.createFromAsset(assets, "AndroidClock.ttf"), Typeface.BOLD)
        clockView.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        synchronizeClock()
        // ClockView seems not to automatically get the focus on 9+.
        // We have to use double-click to show calendar dialog at the first time while app is running.
        clockView.requestFocus()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_TIME_TICK)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        registerReceiver(broadcastReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(broadcastReceiver)
        if ((getSystemService(Context.POWER_SERVICE) as PowerManager).isScreenOn) finish()
        else Log.w(TAG, "onStop: Clock is invisible caused !isScreenOn.Won't exit")
    }

    override fun onClick(v: View) {
        if (v.id == R.id.clock_view) {
            if (loadCalendar) CalendarDialog.show()
        }
    }

    companion object {
        private const val TAG = "ClockScreen"
    }

    private fun configSysUiFlags() {
        var flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        // On 5.0+,the flags will lead the ui to show a blank area for nav bar instead of hiding it completely.
        // But on 4.4,it will prevent the contraction of clockView's font when the calendar dialog is showing.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH)
            flags += View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.decorView.systemUiVisibility = flags
    }

    private fun synchronizeClock() {
        clockView.text = hmFormat.format(Date())
    }

    override fun onDestroy() {
        super.onDestroy()
        CalendarDialog.destroy()
    }
}
