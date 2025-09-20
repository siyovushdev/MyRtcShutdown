package ru.firstmt.myrtcshutdown

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.json.JSONObject
import ru.firstmt.myrtcshutdown.helper.*

class RemoteScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        L.i(L.T_SCH, "Remote cmd: ${intent.action}")
        when (intent.action) {
            RemoteProtocol.ACTION_INIT -> {
                // Один раз “разбудить” приложение и сразу расставить расписания из уже сохранённых prefs
                L.i(L.T_SCH, "INIT received → rescheduleAll()")
                if (ExactAlarm.ensure(context)) {
                    Scheduler(context).rescheduleAll()
                    sendResult(context, true, "init ok")
                } else {
                    sendResult(context, false, "exact alarm permission missing")
                }
            }
            RemoteProtocol.ACTION_SET_SCHEDULE -> {
                val fmt = intent.getStringExtra(RemoteProtocol.EXTRA_FORMAT)
                val payload = intent.getStringExtra(RemoteProtocol.EXTRA_PAYLOAD)
                if (fmt != RemoteProtocol.FORMAT_JSON || payload.isNullOrEmpty()) {
                    sendResult(context, false, "Bad payload/format"); return
                }
                try {
                    val jo = JSONObject(payload)
                    val prefs = Prefs7d(context)
                    fun putDay(kind: String, key: String, wd: WeekDay) {
                        if (!jo.has(kind)) return
                        val d = jo.getJSONObject(kind).optJSONObject(key) ?: return
                        val en = d.optBoolean("enabled", false)
                        val h  = d.optInt("hour", 0)
                        val m  = d.optInt("minute", 0)
                        if (kind == "off") prefs.setOff(wd, DayPlan(en, h, m))
                        else               prefs.setOn (wd, DayPlan(en, h, m))
                        L.d(L.T_SCH, "Set $kind/$key -> enabled=$en $h:$m")
                    }
                    // OFF
                    putDay("off", "mon", WeekDay.MON); putDay("off", "tue", WeekDay.TUE)
                    putDay("off", "wed", WeekDay.WED); putDay("off", "thu", WeekDay.THU)
                    putDay("off", "fri", WeekDay.FRI); putDay("off", "sat", WeekDay.SAT)
                    putDay("off", "sun", WeekDay.SUN)
                    // ON
                    putDay("on",  "mon", WeekDay.MON); putDay("on",  "tue", WeekDay.TUE)
                    putDay("on",  "wed", WeekDay.WED); putDay("on",  "thu", WeekDay.THU)
                    putDay("on",  "fri", WeekDay.FRI); putDay("on",  "sat", WeekDay.SAT)
                    putDay("on",  "sun", WeekDay.SUN)

                    if (ExactAlarm.ensure(context)) {
                        Scheduler(context).rescheduleAll()
                        sendResult(context, true, "schedule applied")
                    } else {
                        sendResult(context, false, "exact alarm permission missing")
                    }
                } catch (t: Throwable) {
                    L.e(L.T_SCH, "SET_SCHEDULE error", t)
                    sendResult(context, false, "error: ${t.message}")
                }
            }

            RemoteProtocol.ACTION_TEST_CYCLE -> {
                val offSec = intent.getIntExtra(RemoteProtocol.EXTRA_OFF_SEC, 60)
                val onSec  = intent.getIntExtra(RemoteProtocol.EXTRA_ON_SEC,  120)
                val offAt  = System.currentTimeMillis() + offSec * 1000L
                val onEpoch= (System.currentTimeMillis() / 1000) + onSec
                L.i(L.T_SCH, "Remote TEST_CYCLE: OFF @ ${L.ts(offAt)}; ON @ ${L.epoch(onEpoch)}")

                if (ExactAlarm.ensure(context)) {
                    // OFF — используем dayIndex=0 как тестовый слот
                    Scheduler(context).cancelAllWeeklyShutdowns()
                    val sch = Scheduler(context)
                    val m = sch::class.java.getDeclaredMethod("scheduleShutdownAtForDay", Long::class.java, Int::class.java)
                    m.isAccessible = true
                    m.invoke(sch, offAt, 0)
                    // ON — RTC
                    RtcWake.programEpoch(onEpoch)
                    sendResult(context, true, "test cycle set")
                } else {
                    sendResult(context, false, "exact alarm permission missing")
                }
            }
        }
    }

    private fun sendResult(ctx: Context, ok: Boolean, msg: String) {
        L.i(L.T_SCH, "Remote result: ok=$ok, msg=$msg")
        ctx.sendBroadcast(Intent(RemoteProtocol.ACTION_RESULT).apply {
            putExtra("OK", ok)
            putExtra("MESSAGE", msg)
        })
    }
}
