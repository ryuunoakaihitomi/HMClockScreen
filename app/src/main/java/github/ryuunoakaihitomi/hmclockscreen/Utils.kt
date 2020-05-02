package github.ryuunoakaihitomi.hmclockscreen

import android.os.Bundle
import android.util.Log
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

object Utils {

    private const val TAG = "Utils"

    fun percentage(part: Int, total: Int): Int {
        val ratio: BigDecimal = BigDecimal.valueOf(part.toFloat() / total * 10.0.pow(2))
        return ratio.setScale(0, RoundingMode.HALF_UP).toInt()
    }

    fun bundle2String4Display(bundle: Bundle): String {
        if (BuildConfig.DEBUG) Log.d(TAG, "bundle2String4Display: $bundle")
        var extrasString = String()
        for (key in bundle.keySet()) extrasString += "$key: ${bundle[key]}${System.lineSeparator()}"
        return extrasString
    }
}
