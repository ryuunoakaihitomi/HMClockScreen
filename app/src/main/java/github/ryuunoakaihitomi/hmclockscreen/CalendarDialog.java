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

    private static DatePickerDialog mDialog;

    private static boolean isDialogConfigDone;

    static boolean create(Context context, DialogInterface.OnCancelListener cancelCallback) {
        int themeResId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                android.R.style.Theme_DeviceDefault_Dialog_Alert :
                AlertDialog.THEME_DEVICE_DEFAULT_DARK;
        // On 8.0+,if we use constructor(context,themeResId,listener,year,monthOfYear,dayOfMonth) to create DatePickerDialog,
        // using getDatePicker().updateDate() to update the date will only work for the label(left side)
        // and will not take effect on the calendar(right side).
        // But the issue will be fixed when we use constructor(context,themeResId) instead.
        mDialog = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                new DatePickerDialog(context, themeResId) :
                new DatePickerDialog(context, themeResId, null, 0, 0, 0);
        mDialog.setOnCancelListener(cancelCallback);
        return true;
    }

    static void show() {
        if (mDialog == null) return;
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH), date = calendar.get(Calendar.DATE);
        DatePicker datePicker = mDialog.getDatePicker();
        Log.i(TAG, "show: Today is " + Arrays.asList(year, month + 1, date));
        datePicker.updateDate(year, month, date);
        datePicker.setEnabled(false);
        if (!isDialogConfigDone) {
            Log.d(TAG, "show: Config dialog attrs");
            // Hide "Set Date".
            mDialog.setTitle(null);
            // Hide "Done" and "Cancel".
            Message nullMsg = Message.obtain();
            mDialog.setButton(AlertDialog.BUTTON_POSITIVE, null, nullMsg);
            mDialog.setButton(AlertDialog.BUTTON_NEGATIVE, null, nullMsg);
            isDialogConfigDone = true;
        }
        mDialog.show();
    }

    static void destroy() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}
