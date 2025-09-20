package ru.firstmt.myrtcshutdown.helper

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object L {
    const val T_APP   = "MyRtc.App"
    const val T_BOOT  = "MyRtc.Boot"
    const val T_ALARM = "MyRtc.Alarm"
    const val T_SCH   = "MyRtc.Scheduler"
    const val T_RTC   = "MyRtc.Rtc"
    const val T_PWR   = "MyRtc.Power"
    const val T_PERM  = "MyRtc.ExactAlarm"

    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun ts(millis: Long) = "${sdf.format(Date(millis))} (${millis})"
    fun epoch(sec: Long) = "${sdf.format(Date(sec * 1000))} (epoch=$sec)"
    fun d(tag: String, msg: String) = Log.d(tag, msg)
    fun i(tag: String, msg: String) = Log.i(tag, msg)
    fun w(tag: String, msg: String) = Log.w(tag, msg)
    fun e(tag: String, msg: String, tr: Throwable? = null) = Log.e(tag, msg, tr)
}
