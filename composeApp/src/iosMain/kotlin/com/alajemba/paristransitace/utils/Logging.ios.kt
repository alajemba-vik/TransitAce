package com.alajemba.paristransitace.utils

import platform.Foundation.NSLog
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
actual fun debugLog(message: String) {
    // To strictly hide in release, you'd need a custom flag,
    // but this is the standard logging for iOS
    if (Platform.isDebugBinary) {
        NSLog("ParisTransit: $message")
    }
}