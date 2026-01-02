package com.alajemba.paristransitace.utils


fun Double.toEuroString(): String {
    return toString().replace(".", ",") + " €"
}