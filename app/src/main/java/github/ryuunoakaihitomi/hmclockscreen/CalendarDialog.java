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

    private static DatePickerDialog sDialog;

    /**
     * Note:In some case,for example,
     * use overview btn to exit app while cal dialog is visible and reopen it from the recent apps list.
     * mDialog may be destroyed and will be recreate when needed.
     * But the marker bit(isDialogConfigDone) is still true.It will lead the show() not to config the attrs the dialog should be.
     *
     * @see android.content.ComponentCallbacks2#TRIM_MEMORY_UI_HIDDEN
     */
    private static int sId;

    static boolean create(Context context, DialogInterface.OnCancelListener cancelCallback) {
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
        sDialog = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                new DatePickerDialog(context, themeResId) :
                new DatePickerDialog(context, themeResId, null, 0, 0, 0);
        sDialog.setOnCancelListener(cancelCallback);
        return sDialog != null;
    }

    static void show() {
        if (sDialog == null) return;
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH), date = calendar.get(Calendar.DATE);
        DatePicker datePicker = sDialog.getDatePicker();
        Log.i(TAG, "show: Today is " + Arrays.asList(year, month + 1, date));
        datePicker.updateDate(year, month, date);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) datePicker.setEnabled(false);
        else {
            // datePicker.setEnabled() doesn't work on 5.0+,and datePicker.set(Max/Min)Date can only change look on 5.0.
            long now = System.currentTimeMillis();
            datePicker.setMaxDate(now);
            datePicker.setMinDate(now);
        }
        int newHashId = sDialog.hashCode();
        if (sId != newHashId) {
            Log.d(TAG, "show: Configure dialog attributes");

            // Style List:
            //  19        Title + Done (Tablet)
            //  21-23     Title + Cancel + OK
            //  24-29(+)  Cancel + OK

            // Hide "Set Date".
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                sDialog.setTitle(null);
            // Hide "Done" and "Cancel".
            Message nullMsg = Message.obtain();
            sDialog.setButton(AlertDialog.BUTTON_POSITIVE, null, nullMsg);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
                sDialog.setButton(AlertDialog.BUTTON_NEGATIVE, null, nullMsg);

            if (sId != 0)
                Log.v(TAG, "show: Hashcode changed. It's a new dialog! " + sId + " -> " + newHashId);
            sId = newHashId;
        }
        sDialog.show();
    }

    static void destroy() {
        if (sDialog != null) {
            sDialog.dismiss();
            sDialog = null;
        }
    }
}
