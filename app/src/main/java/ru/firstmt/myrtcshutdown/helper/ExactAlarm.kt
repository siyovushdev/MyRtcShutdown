package ru.firstmt.myrtcshutdown.helper

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

object ExactAlarm {
    fun ensure(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 31) return true
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val allowed = am.canScheduleExactAlarms()
        L.i(L.T_PERM, "canScheduleExactAlarms() = $allowed")
        if (!allowed) {
            val i = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
            L.w(L.T_PERM, "Requested user to grant exact alarm permission")
        }
        return allowed
    }
}
