package ru.firstmt.myrtcshutdown

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.TimePickerDialog
import android.app.usage.ExternalStorageStats
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.gson.Gson
import ru.firstmt.myrtcshutdown.helper.*
import java.io.File
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    data class Row(val root: View, val chk: CheckBox, val day: TextView, val time: TextView)

    private lateinit var prefs: Prefs7d
    private lateinit var rowsOff: List<Pair<WeekDay, Row>>
    private lateinit var rowsOn:  List<Pair<WeekDay, Row>>

    private lateinit var clockTime: TextClock
    private lateinit var clockDate: TextView
    private lateinit var clockTz: TextView

    private val tzReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateStatusStrip()
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        val f = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_TIME_TICK)
        }
        if (Build.VERSION.SDK_INT >= 26) f.addAction(Intent.ACTION_LOCALE_CHANGED)
        registerReceiver(tzReceiver, f)

        registerReceiver(scheduleChangedReceiver,IntentFilter(RemoteProtocol.ACTION_SCHEDULE_CHANGED))

        updateStatusStrip()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(tzReceiver)
        unregisterReceiver(scheduleChangedReceiver)
    }

    private fun updateStatusStrip() {
        val zone = ZoneId.systemDefault()
        val now  = ZonedDateTime.now(zone)

        val dateFmt = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("ru", "RU"))
        clockDate.text = now.format(dateFmt)

        val offset: ZoneOffset = now.offset
        val totalMinutes = offset.totalSeconds / 60
        val sign = if (totalMinutes >= 0) "+" else "-"
        val hh = kotlin.math.abs(totalMinutes) / 60
        val mm = kotlin.math.abs(totalMinutes) % 60
        val utcLabel = "UTC$sign%02d".format(hh) + if (mm != 0) ":%02d".format(mm) else ""
        clockTz.text = "${zone.id} ($utcLabel)"

        if (DateFormat.is24HourFormat(this)) {
            clockTime.format24Hour = "HH:mm"
        } else {
            clockTime.format12Hour = "hh:mm a"
        }
    }

    private val scheduleChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            refreshScheduleUi() // перечитали prefs и обновили чекбоксы/время
        }
    }

    private fun refreshScheduleUi() {
        val result = HashMap<String, String>()
        val gson = Gson()
        // OFF
        result += Pair("OFF", ">>>>")
        rowsOff.forEach { (wd, row) ->
            val plan = prefs.getOff(wd)
            row.chk.isChecked = plan.enabled
            row.time.setHHmm(plan.hour, plan.minute)
            result += Pair("$wd", "enabled: ${plan.enabled} | time:${plan.hour}:${plan.minute}")
        }

        // ON
        result += Pair("ON", ">>>>")
        rowsOn.forEach { (wd, row) ->
            val plan = prefs.getOn(wd)
            row.chk.isChecked = plan.enabled
            row.time.setHHmm(plan.hour, plan.minute)
            result += Pair("$wd", "enabled: ${plan.enabled} | time:${plan.hour}:${plan.minute}")
        }
        // при необходимости — всплывашка или лог
         Toast.makeText(this, "Расписание обновлено", Toast.LENGTH_SHORT).show()


        try {
            val json = gson.toJson(result)
            File("/sdcard/timerswitch.json").writeText(json)
        }catch (t: Throwable){
            Log.e("refreshScheduleUi", "ERROR: ", t)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_kiosk)

        clockTime = findViewById(R.id.clock_time)
        clockDate = findViewById(R.id.clock_date)
        clockTz   = findViewById(R.id.clock_tz)
        updateStatusStrip()

        findViewById<View>(R.id.status_strip).setOnKeyListener { _, _, _ -> false }

        prefs = Prefs7d(this)

        // Секции по дням
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

        fun bindAllRow(includeId: Int, title: String): Row {
            val root = findViewById<View>(includeId)
            val chk  = root.findViewById<CheckBox>(R.id.chk_enabled)
            val day  = root.findViewById<TextView>(R.id.txt_day)
            val time = root.findViewById<TextView>(R.id.txt_time)

            day.text = title
            chk.isChecked = false                    // не тянуть состояние из prefs!
            // Поставим какое-то стартовое время (например, из понедельника для удобства UI),
            // но только визуально — это НЕ prefs «всех дней».
            val monPlan = prefs.getOff(WeekDay.MON)  // или getOn — не важно, просто старт
            time.setHHmm(monPlan.hour, monPlan.minute)

            time.isFocusable = true
            time.isFocusableInTouchMode = true
            time.setOnClickListener { showNativeTimePicker(time) } // обновляет text+теги

            return Row(root, chk, day, time)
        }

        val rowOffAll = bindAllRow(R.id.off_all, "Для всех дней")
        val rowOnAll  = bindAllRow(R.id.on_all,  "Для всех дней")

        // Переименуем, чтобы не было второго "Понедельника"
        rowOffAll.day.text = "Для всех дней"
        rowOnAll.day.text  = "Для всех дней"

        // Чекбокс "общего" — переключает ВСЕ чекбоксы соответствующей колонки
        rowOffAll.chk.setOnCheckedChangeListener { _, checked ->
            rowsOff.forEach { it.second.chk.isChecked = checked }
        }
        rowOnAll.chk.setOnCheckedChangeListener { _, checked ->
            rowsOn.forEach { it.second.chk.isChecked = checked }
        }

        // Выбор времени в "общем" — сразу проставляем всем дням на экране
        rowOffAll.time.setOnClickListener {
            showNativeTimePicker(rowOffAll.time) { h, m ->
                rowsOff.forEach { it.second.time.setHHmm(h, m) }
            }
        }
        rowOnAll.time.setOnClickListener {
            showNativeTimePicker(rowOnAll.time) { h, m ->
                rowsOn.forEach { it.second.time.setHHmm(h, m) }
            }
        }

        // Применить расписание
        findViewById<View>(R.id.btn_reschedule).setOnClickListener {
            val H_off = rowOffAll.time.tagAsHour()
            val M_off = rowOffAll.time.tagAsMinute()
            val useOffAll = rowOffAll.chk.isChecked

            val H_on = rowOnAll.time.tagAsHour()
            val M_on = rowOnAll.time.tagAsMinute()
            val useOnAll = rowOnAll.chk.isChecked

            // OFF
            rowsOff.forEach { (wd, row) ->
                L.d("btn_reschedule", "rowsOff: wd: $wd and row: ${row.time.text} and tagAsHour: ${row.time.tagAsHour()}")
                val enabled = if (useOffAll) true else row.chk.isChecked
                val hour    = if (useOffAll) H_off else row.time.tagAsHour()
                val minute  = if (useOffAll) M_off else row.time.tagAsMinute()
                prefs.setOff(wd, DayPlan(enabled, hour, minute))
            }
            // ON
            rowsOn.forEach { (wd, row) ->
                L.d("btn_reschedule", "rowsOn: wd: $wd and row: ${row.time.text} and tagAsHour: ${row.time.tagAsHour()}")
                val enabled = if (useOnAll) true else row.chk.isChecked
                val hour    = if (useOnAll) H_on else row.time.tagAsHour()
                val minute  = if (useOnAll) M_on else row.time.tagAsMinute()
                prefs.setOn(wd, DayPlan(enabled, hour, minute))
            }

            if (ExactAlarm.ensure(this)) {
                Scheduler(this).rescheduleAll()
            }
            // finish() — можно вернуть, когда убедитесь визуально
            Toast.makeText(this, "Изменения сохранены", Toast.LENGTH_SHORT).show()

        }

//        // Тест-кнопка (опционально)
//        findViewById<View?>(R.id.btn_test_cycle)?.setOnClickListener {
//            val offAt = System.currentTimeMillis() + 60_000L
//            val onEpoch = (System.currentTimeMillis() / 1000) + 120
//            L.i(L.T_SCH, "TEST CYCLE: schedule OFF at ${L.ts(offAt)}, program ON at ${L.epoch(onEpoch)}")
//            if (ExactAlarm.ensure(this)) {
//                Scheduler(this).cancelAllWeeklyShutdowns()
//                Scheduler(this).apply {
//                    val cls = this::class.java
//                    val method = cls.getDeclaredMethod("scheduleShutdownAtForDay", Long::class.java, Int::class.java)
//                    method.isAccessible = true
//                    method.invoke(this, offAt, 0)
//                }
//            }
//            RtcWake.programEpoch(onEpoch)
//        }
    }

    /** Привязка строк секции (OFF/ON), загрузка значений, обработка DPAD и диалога */
    private fun bindSection(ids: Map<WeekDay, Int>, off: Boolean): List<Pair<WeekDay, Row>> {
        val res = mutableListOf<Pair<WeekDay, Row>>()
        ids.forEach { (wd, includeId) ->
            val root = findViewById<View>(includeId)
            val chk = root.findViewById<CheckBox>(R.id.chk_enabled)
            val day = root.findViewById<TextView>(R.id.txt_day)
            val time = root.findViewById<TextView>(R.id.txt_time)

            day.text = when (wd) {
                WeekDay.MON -> "Понедельник"
                WeekDay.TUE -> "Вторник"
                WeekDay.WED -> "Среда"
                WeekDay.THU -> "Четверг"
                WeekDay.FRI -> "Пятница"
                WeekDay.SAT -> "Суббота"
                WeekDay.SUN -> "Воскресенье"
            }

            val plan = if (off) prefs.getOff(wd) else prefs.getOn(wd)
            chk.isChecked = plan.enabled
            time.setHHmm(plan.hour, plan.minute)

            time.isFocusable = true
            time.isFocusableInTouchMode = true
            time.setOnClickListener { showNativeTimePicker(time) }
//            time.setOnKeyListener { _, keyCode, event ->
//                if (event.action != android.view.KeyEvent.ACTION_DOWN) return@setOnKeyListener false
//                val stepMin = 5
//                val h0 = time.tagAsHour()
//                val m0 = time.tagAsMinute()
//                when (keyCode) {
//                    android.view.KeyEvent.KEYCODE_DPAD_LEFT  -> { time.setHHmm(h0, (m0 - stepMin + 60) % 60); true }
//                    android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> { time.setHHmm(h0, (m0 + stepMin) % 60); true }
//                    android.view.KeyEvent.KEYCODE_DPAD_UP    -> { time.setHHmm((h0 + 23) % 24, m0); true }
//                    android.view.KeyEvent.KEYCODE_DPAD_DOWN  -> { time.setHHmm((h0 + 1) % 24, m0); true }
//                    android.view.KeyEvent.KEYCODE_DPAD_CENTER,
//                    android.view.KeyEvent.KEYCODE_ENTER      -> { showNativeTimePicker(time); true }
//                    else -> false
//                }
//            }

            res += wd to Row(root, chk, day, time)
        }
        return res
    }

    /** Диалог выбора времени; onPicked — коллбэк для "общей" строки */
    private fun showNativeTimePicker(target: TextView, onPicked: ((Int, Int) -> Unit)? = null) {
        // стартовать не от "сейчас", а от текущего значения в target
        val (h0, m0) = run {
            val th = target.getTag(R.id.tag_hour) as? Int
            val tm = target.getTag(R.id.tag_minute) as? Int
            if (th != null && tm != null) th to tm
            else {
                val m = Regex("""^\s*(\d{1,2}):(\d{2})\s*$""").find(target.text?.toString().orEmpty())
                if (m != null) m.groupValues[1].toInt() to m.groupValues[2].toInt() else 0 to 0
            }
        }

        TimePickerDialog(
            this,
            { _, h, m ->
                // КЛЮЧЕВОЕ: обновляем ТЕКСТ + ТЕГИ
                target.setHHmm(h, m)
                onPicked?.invoke(h, m)
            },
            h0, m0,
            true
        ).show()
    }

    // helpers to store hour/minute inside TextView.tag and display as HH:MM
    private fun TextView.setHHmm(h: Int, m: Int) {
        text = String.format("%02d:%02d", h, m)
        setTag(R.id.tag_hour, h)
        setTag(R.id.tag_minute, m)
    }
    private fun TextView.tagAsHour() = (getTag(R.id.tag_hour) as? Int) ?: 0
    private fun TextView.tagAsMinute() = (getTag(R.id.tag_minute) as? Int) ?: 0

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
