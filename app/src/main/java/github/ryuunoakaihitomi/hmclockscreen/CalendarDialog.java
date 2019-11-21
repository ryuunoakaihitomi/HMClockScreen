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

    /**
     * Note:In some case,for example,
     * use overview btn to exit app while cal dialog is visible and reopen it from the recent apps list.
     * mDialog may be destroyed and will be recreate when needed.
     * But the marker bit(isDialogConfigDone) is still true.It will lead the show() not to config the attrs the dialog should be.
     *
     * @see android.content.ComponentCallbacks2#TRIM_MEMORY_UI_HIDDEN
     */
    private static int mId;

    static boolean create(Context context, DialogInterface.OnCancelListener cancelCallback) {
        int themeResId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                android.R.style.Theme_DeviceDefault_Dialog_Alert :
                AlertDialog.THEME_DEVICE_DEFAULT_DARK;
        // On 8.0+,if we use constructor(context,themeResId,listener,year,monthOfYear,dayOfMonth) to create DatePickerDialog,
        // using getDatePicker().updateDate() to update the date will only work for the label(left side)
        // and will not take effect on the calendar(right side).
        // But the issue will be fixed when we use constructor(context,themeResId) instead.
        // p.s. In fact,the situation is very rare.It can only reproduce when the initial value of year < 2,maybe...
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
        int newHashId = mDialog.hashCode();
        if (mId != newHashId) {
            Log.d(TAG, "show: Configure dialog attributes");
            // Hide "Set Date".
            mDialog.setTitle(null);
            // Hide "Done" and "Cancel".
            Message nullMsg = Message.obtain();
            mDialog.setButton(AlertDialog.BUTTON_POSITIVE, null, nullMsg);
            mDialog.setButton(AlertDialog.BUTTON_NEGATIVE, null, nullMsg);
            if (mId != 0)
                Log.v(TAG, "show: Hashcode changed. It's a new dialog! " + mId + " -> " + newHashId);
            mId = newHashId;
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
