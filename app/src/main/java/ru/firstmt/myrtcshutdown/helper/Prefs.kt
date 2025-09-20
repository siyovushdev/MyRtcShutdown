package ru.firstmt.myrtcshutdown.helper

import android.content.Context
import android.content.SharedPreferences

class Prefs(ctx: Context) {
    private val p: SharedPreferences = ctx.getSharedPreferences("rtc_shutdown", Context.MODE_PRIVATE)

    var offEnabled: Boolean
        get() = p.getBoolean("offEnabled", false)
        set(v) { p.edit().putBoolean("offEnabled", v).apply() }

    var offHour: Int
        get() = p.getInt("offHour", 23)
        set(v) { p.edit().putInt("offHour", v).apply() }

    var offMinute: Int
        get() = p.getInt("offMinute", 0)
        set(v) { p.edit().putInt("offMinute", v).apply() }

    var onEnabled: Boolean
        get() = p.getBoolean("onEnabled", false)
        set(v) { p.edit().putBoolean("onEnabled", v).apply() }

    var onHour: Int
        get() = p.getInt("onHour", 7)
        set(v) { p.edit().putInt("onHour", v).apply() }

    var onMinute: Int
        get() = p.getInt("onMinute", 30)
        set(v) { p.edit().putInt("onMinute", v).apply() }
}
