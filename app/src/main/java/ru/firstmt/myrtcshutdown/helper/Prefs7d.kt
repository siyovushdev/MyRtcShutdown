package ru.firstmt.myrtcshutdown.helper

import android.content.Context
import android.content.SharedPreferences

class Prefs7d(ctx: Context) {
    private val p: SharedPreferences = ctx.getSharedPreferences("rtc_shutdown_weekly", Context.MODE_PRIVATE)

    // OFF (выключение по дням недели)
    fun getOff(day: WeekDay): DayPlan = DayPlan(
        p.getBoolean("off_${day.name}_enabled", false),
        p.getInt("off_${day.name}_hour", 23),
        p.getInt("off_${day.name}_minute", 0)
    )
    fun setOff(day: WeekDay, plan: DayPlan) {
        L.d("Prefs7d", "setOff: day: $day and plan: $plan")
        p.edit()
            .putBoolean("off_${day.name}_enabled", plan.enabled)
            .putInt("off_${day.name}_hour", plan.hour)
            .putInt("off_${day.name}_minute", plan.minute)
            .apply()
    }

    // ON (включение по RTC — по дням недели); важно: RTC позволяет только ОДИН будильник сразу,
    // поэтому мы будем программировать ближайший из включенных.
    fun getOn(day: WeekDay): DayPlan = DayPlan(
        p.getBoolean("on_${day.name}_enabled", false),
        p.getInt("on_${day.name}_hour", 7),
        p.getInt("on_${day.name}_minute", 30)
    )
    fun setOn(day: WeekDay, plan: DayPlan) {
        L.d("Prefs7d", "setOn: day: $day and plan: $plan")
        p.edit()
            .putBoolean("on_${day.name}_enabled", plan.enabled)
            .putInt("on_${day.name}_hour", plan.hour)
            .putInt("on_${day.name}_minute", plan.minute)
            .apply()
    }
}
