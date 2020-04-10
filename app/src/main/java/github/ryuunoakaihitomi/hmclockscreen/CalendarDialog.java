package github.ryuunoakaihitomi.hmclockscreen;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Message;
import android.util.Log;
import android.widget.DatePicker;

import java.util.Arrays;
import java.util.Calendar;

/**
 * Show a calendar dialog with the current date.
 */

public class CalendarDialog {

    private static final String TAG = "CalendarDialog";

    static void create(Context context, DialogInterface.OnCancelListener cancelCallback) {
        int themeResId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                android.R.style.Theme_DeviceDefault_Dialog_Alert :
                // On 5.0,AlertDialog.THEME_DEVICE_DEFAULT_DARK will let the the calendar(right side) be truncated.
                // (Sun -> Thu & Fri)
                Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP ?
                        android.R.style.Theme_Material_Dialog :
                        AlertDialog.THEME_DEVICE_DEFAULT_DARK;
        // On 8.0+,if we use constructor(context,themeResId,listener,year,monthOfYear,dayOfMonth) to create DatePickerDialog,
        // using getDatePicker().updateDate() to update the date will only work for the label(left side)
        // and will not take effect on the calendar(right side).
        // But the issue will be fixed when we use constructor(context,themeResId) instead.
        // p.s. In fact,the situation is very rare.It can only reproduce when the initial value of year < 2,maybe...
        DatePickerDialog dialog = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                new DatePickerDialog(context, themeResId) :
                new DatePickerDialog(context, themeResId, null, 0, 0, 0);
        dialog.setOnCancelListener(cancelCallback);
        /* ------- show ------- */
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH), date = calendar.get(Calendar.DATE);
        DatePicker datePicker = dialog.getDatePicker();
        Log.i(TAG, "show: Today is " + Arrays.asList(year, month + 1, date));
        datePicker.updateDate(year, month, date);
        datePicker.setEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // datePicker.setEnabled() doesn't work properly on 5.0+(Can only let year label be unavailable),
            // but datePicker.set(Max/Min)Date can't limit the scope on 5.0.
            long now = System.currentTimeMillis();
            datePicker.setMaxDate(now);
            datePicker.setMinDate(now);
        }

        // Style List:
        //  19        Title + Done (Tablet)
        //  21-23     Title + Cancel + OK
        //  24-29(+)  Cancel + OK

        // Hide "Set Date".
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            dialog.setTitle(null);
        // Hide "Done" and "Cancel".
        Message nullMsg = Message.obtain();
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, null, nullMsg);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, null, nullMsg);

        Log.i(TAG, "create: " + dialog);
        dialog.show();
    }
}
