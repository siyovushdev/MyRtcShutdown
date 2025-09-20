package ru.firstmt.myrtcshutdown

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ru.firstmt.myrtcshutdown.helper.*

class MainActivity : AppCompatActivity() {

    data class Row(val root: View, val chk: CheckBox, val day: TextView, val time: TextView)

    private lateinit var prefs: Prefs7d
    private lateinit var rowsOff: List<Pair<WeekDay, Row>>
    private lateinit var rowsOn:  List<Pair<WeekDay, Row>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = Prefs7d(this)

        rowsOff = bindSection(
            mapOf(
                WeekDay.MON to R.id.off_mon, WeekDay.TUE to R.id.off_tue, WeekDay.WED to R.id.off_wed,
                WeekDay.THU to R.id.off_thu, WeekDay.FRI to R.id.off_fri, WeekDay.SAT to R.id.off_sat,
                WeekDay.SUN to R.id.off_sun
            ),
            off = true
        )
        rowsOn = bindSection(
            mapOf(
                WeekDay.MON to R.id.on_mon, WeekDay.TUE to R.id.on_tue, WeekDay.WED to R.id.on_wed,
                WeekDay.THU to R.id.on_thu, WeekDay.FRI to R.id.on_fri, WeekDay.SAT to R.id.on_sat,
                WeekDay.SUN to R.id.on_sun
            ),
            off = false
        )

        findViewById<View>(R.id.btn_save).setOnClickListener {
            // 1) Сохраняем OFF
            rowsOff.forEach { (wd, row) ->
                val H = row.time.tagAsHour()
                val M = row.time.tagAsMinute()
                prefs.setOff(wd, DayPlan(row.chk.isChecked, H, M))
            }
            // 2) Сохраняем ON
            rowsOn.forEach { (wd, row) ->
                val H = row.time.tagAsHour()
                val M = row.time.tagAsMinute()
                prefs.setOn(wd, DayPlan(row.chk.isChecked, H, M))
            }

            // 3) Проверяем право на точные тревоги и пересоздаём
            if (ExactAlarm.ensure(this)) {
                Scheduler(this).rescheduleAll()
            }
            finish()
        }

        // Кнопка: OFF через 1 мин + ON через 2 мин
        findViewById<View>(R.id.btn_test_cycle).setOnClickListener {
            val offAt = System.currentTimeMillis() + 60_000L        // выключение через 1 минуту
            val onEpoch = (System.currentTimeMillis() / 1000) + 120 // включение через 2 минуты

            // Логируем
            L.i(
                L.T_SCH,
                "TEST CYCLE: schedule OFF at ${L.ts(offAt)}, " +
                        "program ON at ${L.epoch(onEpoch)}"
            )

            // OFF
            if (ExactAlarm.ensure(this)) {
                Scheduler(this).cancelAllWeeklyShutdowns()
                Scheduler(this).apply {
                    val cls = this::class.java
                    val method = cls.getDeclaredMethod("scheduleShutdownAtForDay", Long::class.java, Int::class.java)
                    method.isAccessible = true
                    method.invoke(this, offAt, 0) // используем dayIndex=0 для теста
                }
            }

            // ON (RTC)
            RtcWake.programEpoch(onEpoch)
        }
    }

    /** Привязка семи строк секции (OFF/ON), загрузка значений, обработка TimePicker */
    private fun bindSection(ids: Map<WeekDay, Int>, off: Boolean): List<Pair<WeekDay, Row>> {
        val res = mutableListOf<Pair<WeekDay, Row>>()
        ids.forEach { (wd, includeId) ->
            val root = findViewById<View>(includeId)
            val chk = root.findViewById<CheckBox>(R.id.chk_enabled)
            val day = root.findViewById<TextView>(R.id.txt_day)
            val time = root.findViewById<TextView>(R.id.txt_time)

            day.text = when (wd) {
                WeekDay.MON -> "Понедельник"; WeekDay.TUE -> "Вторник"; WeekDay.WED -> "Среда"
                WeekDay.THU -> "Четверг"; WeekDay.FRI -> "Пятница"; WeekDay.SAT -> "Суббота"; WeekDay.SUN -> "Воскресенье"
            }

            val plan = if (off) prefs.getOff(wd) else prefs.getOn(wd)
            chk.isChecked = plan.enabled
            time.setHHmm(plan.hour, plan.minute)

            time.setOnClickListener {
                val dlg = TimePickerDialog(this, { _, h, m -> time.setHHmm(h, m) }, plan.hour, plan.minute, true)
                dlg.show()
            }

            res += wd to Row(root, chk, day, time)
        }
        return res
    }

    // helpers to store hour/minute inside TextView.tag and display as HH:MM
    private fun TextView.setHHmm(h: Int, m: Int) {
        text = String.format("%02d:%02d", h, m)
        setTag(R.id.tag_hour, h)
        setTag(R.id.tag_minute, m)
    }
    private fun TextView.tagAsHour() = (getTag(R.id.tag_hour) as? Int) ?: 0
    private fun TextView.tagAsMinute() = (getTag(R.id.tag_minute) as? Int) ?: 0
}
