package github.ryuunoakaihitomi.hmclockscreen

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import java.text.SimpleDateFormat
import java.util.*

class ClockScreen : Activity(), View.OnClickListener {

    private lateinit var clockView: TextView

    private var isClockViewFixed: Boolean = false

    private val hmFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
//            Log.d(TAG, "onReceive() called with: context = [$context], intent.action = [${intent.action}]")
            when (intent.action) {
                Intent.ACTION_TIME_TICK -> {
                    val timeText = hmFormat.format(Date())
                    clockView.text = timeText
                    // 1 has a narrower width than the other numbers.So if you turn 1 to 2 without autosizing...
                    if (!isClockViewFixed && "1" !in timeText) {
                        // Fix the view for lower power consumption.
                        Log.d(TAG, "onReceive: [ClockView] size=" + clockView.textSize.toInt())
                        TextViewCompat.setAutoSizeTextTypeWithDefaults(clockView, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE)
                        isClockViewFixed = true
                    }
                }
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
        clockView.text = hmFormat.format(Date())
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_TIME_TICK)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        registerReceiver(broadcastReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(broadcastReceiver)
        if ((getSystemService(Context.POWER_SERVICE) as PowerManager).isScreenOn) finish()
        else Log.w(TAG, "onStop: Clock is invisible caused !isScreenOn.Won't exit.")
    }

    override fun onClick(v: View) {
        // Show a calendar dialog with the current date.
        if (v.id == R.id.clock_view) {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val date = calendar.get(Calendar.DATE)
            val datePickerDialog = DatePickerDialog(this,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        android.R.style.Theme_DeviceDefault_Dialog_Alert
                    else AlertDialog.THEME_DEVICE_DEFAULT_DARK,
                    null, year, month, date)
            // Hide "Set Date".
            datePickerDialog.setTitle(null)
            // Hide "Done" and "Cancel".
            val nullMsg = Message.obtain()
            datePickerDialog.setButton(AlertDialog.BUTTON_POSITIVE, null, nullMsg)
            datePickerDialog.setButton(AlertDialog.BUTTON_NEGATIVE, null, nullMsg)
            datePickerDialog.datePicker.isEnabled = false
            datePickerDialog.setOnDismissListener { configSysUiFlags() }
            datePickerDialog.show()
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
}
