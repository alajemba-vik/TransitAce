package com.alajemba.paristransitace.utils

import android.util.Log
import com.alajemba.paristransitace.BuildConfig

actual fun debugLog(message: String) {
    if (BuildConfig.DEBUG) {
        Log.d("ParisTransit", message)
    }
}