package ru.firstmt.myrtcshutdown.helper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import ru.firstmt.myrtcshutdown.AlarmReceiver
import java.time.DayOfWeek
import java.time.ZoneId

class Scheduler(private val ctx: Context) {

    fun rescheduleAll() {
        L.i(L.T_SCH, "RescheduleAll: start")
        val prefs = Prefs7d(ctx)

        // OFF
        cancelAllWeeklyShutdowns()
        WeekDay.all.forEachIndexed { index, wd ->
            val plan = prefs.getOff(wd)
            if (plan.enabled) {
//                val millis = nextWeeklyWallClockMillis(wd.toJavaDay(), plan.hour, plan.minute)
                val millis = nextWeeklyWallClockMillis(wd.toJavaDay(), plan.hour, plan.minute, ZoneId.systemDefault(), minLeadSeconds = 90L)
                L.i(L.T_SCH, "OFF[$wd]: enabled, schedule at ${L.ts(millis)} (req=${REQ_SHUTDOWN_BASE + index})")
                scheduleShutdownAtForDay(millis, index)
            } else {
                L.d(L.T_SCH, "OFF[$wd]: disabled")
            }
        }

        // ON (RTC): только ближайший
        val now = System.currentTimeMillis()
        val leadMs = 90_000L
        var bestEpoch: Long? = null
        WeekDay.all.forEach { wd ->
            val plan = prefs.getOn(wd)
            if (plan.enabled) {
//                val epoch = nextWeeklyEpochUtc(wd.toJavaDay(), plan.hour, plan.minute)
                val epoch = nextWeeklyEpochUtc(wd.toJavaDay(), plan.hour, plan.minute, ZoneId.systemDefault(), minLeadSeconds = 90L)
                L.d(L.T_SCH, "ON[$wd]: candidate ${L.epoch(epoch)}")
                if (epoch * 1000 >= now + leadMs && (bestEpoch == null || epoch < bestEpoch!!)) {
                    bestEpoch = epoch
                }
            }
        }
        if (bestEpoch != null) {
            L.i(L.T_SCH, "ON: program nearest ${L.epoch(bestEpoch!!)}")
            RtcWake.programEpoch(bestEpoch!!)
        } else {
            L.w(L.T_SCH, "ON: no active days, clear wakealarm")
            RtcWake.clear()
        }
        L.i(L.T_SCH, "RescheduleAll: done")
    }

    private fun scheduleShutdownAtForDay(triggerAtMillis: Long, dayIndex: Int) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ctx, AlarmReceiver::class.java)
            .setAction(ACTION_SHUTDOWN_WEEKLY)
            .putExtra(EXTRA_DAY_INDEX, dayIndex)
        val pi = PendingIntent.getBroadcast(
            ctx, REQ_SHUTDOWN_BASE + dayIndex, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= 23) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }
        L.i(L.T_SCH, "Scheduled OFF req=${REQ_SHUTDOWN_BASE + dayIndex} at ${L.ts(triggerAtMillis)}")
    }

    fun cancelAllWeeklyShutdowns() {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0 until 7) {
            val intent = Intent(ctx, AlarmReceiver::class.java)
                .setAction(ACTION_SHUTDOWN_WEEKLY)
                .putExtra(EXTRA_DAY_INDEX, i)
            val pi = PendingIntent.getBroadcast(
                ctx, REQ_SHUTDOWN_BASE + i, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pi != null) {
                am.cancel(pi)
                pi.cancel()
                L.d(L.T_SCH, "Cancelled OFF req=${REQ_SHUTDOWN_BASE + i}")
            }
        }
    }

    companion object {
        private const val REQ_SHUTDOWN_BASE = 2000
        const val ACTION_SHUTDOWN_WEEKLY = "ru.firstmt.myrtcshutdown.ACTION_SHUTDOWN_WEEKLY"
        const val EXTRA_DAY_INDEX = "dayIndex"
    }
}
