package ru.firstmt.myrtcshutdown

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import ru.firstmt.myrtcshutdown.utils.Utils
import ru.firstmt.myrtcshutdown.utils.Utils.cancelOnTime
import ru.firstmt.myrtcshutdown.utils.Utils.setOnTime
import ru.firstmt.myrtcshutdown.view.ItemView
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*

class MainActivity : Activity(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private var relatOff: RelativeLayout? = null
    private var relatOn: RelativeLayout? = null
    private var timepickerOn: TimePicker? = null
    private var timepickerView: View? = null
    private var sharedpreferencesOn: SharedPreferences? = null
    private var sharedpreferencesOff: SharedPreferences? = null
    private var switchOn: SwitchCompat? = null
    private var switchOffRepeat: SwitchCompat? = null
    private var switchOnRepeat: SwitchCompat? = null
    private var switchOff: SwitchCompat? = null
    private var textOnTime: TextView? = null
    private var textOffTime: TextView? = null
    private var alarmManager: AlarmManager? = null
    private var pendingIntent: PendingIntent? = null
    private var editOn: SharedPreferences.Editor? = null
    private var editOff: SharedPreferences.Editor? = null
    private var itemCancel: ItemView? = null
    private var itemCommit: ItemView? = null
    private var defaultHourOn //定义这个是为了防止只开启了定时开机就提交
            = 0
    private var defaultMinuteOn //定义这个是为了防止只开启了定时开机就提交
            = 0
    private var defaultMinuteOff //定义这个是为了防止只开启了定时关机就提交
            = 0
    private var defaultHourOff //定义这个是为了防止只开启了定时关机就提交
            = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("TIMER_SWITCH", execRootCmd("date"))
        Log.d("TIMER_SWITCH", execRootCmd("cat /system/build.prop"))
        initview()
        getData() //显示上回保存的数据
        initevent()
        setAlarm()
    }

    private fun execRootCmd(cmd: String): String {
        var result = ""
        val dos: DataOutputStream? = null
        val dis: DataInputStream? = null
        try {
            val p = Runtime.getRuntime().exec("su")
            val dos2 = DataOutputStream(p.outputStream)
            val dis2 = DataInputStream(p.inputStream)
            dos2.writeBytes(
                """
                $cmd

                """.trimIndent()
            )
            dos2.flush()
            dos2.writeBytes("exit\n")
            dos2.flush()
            while (true) {
                val readLine = dis2.readLine() ?: break
                Log.d(TAG, "shell result = $readLine")
                result += readLine
            }
            p.waitFor()
            try {
                dos2.close()
            } catch (e: IOException) {
                Log.e(TAG, "catch (e: Exception): ", e)
                e.printStackTrace()
            }
            try {
                dis2.close()
            } catch (e2: IOException) {
                Log.e(TAG, "catch (e2: Exception): ", e2)
                e2.printStackTrace()
            }
        } catch (e3: Exception) {
            Log.e(TAG, "catch (e3: Exception): ", e3)
            e3.printStackTrace()
            if (dos != null) {
                try {
                    dos.close()
                } catch (e4: IOException) {
                    e4.printStackTrace()
                }
            }
            if (dis != null) {
                try {
                    dis.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } catch (th: Throwable) {
            Log.e(TAG, "catch (th: Throwable): ", th)
            if (dos != null) {
                try {
                    dos.close()
                } catch (e5: IOException) {
                    e5.printStackTrace()
                }
            }
            if (dis != null) {
                try {
                    dis.close()
                } catch (e6: IOException) {
                    e6.printStackTrace()
                }
            }
            throw th
        }
        return result
    }

    private fun initview() {
        relatOff = findViewById(R.id.relat_off)
        relatOn = findViewById(R.id.relat_on)
        switchOn = findViewById(R.id.switch_on)
        switchOff = findViewById(R.id.switch_off)
        switchOnRepeat = findViewById(R.id.switch_on_repeat)
        switchOffRepeat = findViewById(R.id.switch_off_repeat)
        textOnTime = findViewById(R.id.text_on_time)
        textOffTime = findViewById(R.id.text_off_time)
        itemCancel = findViewById(R.id.item_cancel)
        itemCommit = findViewById(R.id.item_commit)
        switchOn!!.requestFocus()
    }

    private fun initevent() {
        relatOff!!.setOnClickListener(this)
        relatOn!!.setOnClickListener(this)
        switchOn!!.setOnCheckedChangeListener(this)
        switchOffRepeat!!.setOnCheckedChangeListener(this)
        switchOnRepeat!!.setOnCheckedChangeListener(this)
        switchOff!!.setOnCheckedChangeListener(this)
        itemCancel!!.setOnClickListener(this)
        itemCommit!!.setOnClickListener(this)
    }

    private fun getData() {
        sharedpreferencesOn = getSharedPreferences(Utils.POWER_ON, MODE_PRIVATE)
        editOn = sharedpreferencesOn!!.edit()
        val isSwitchOn = sharedpreferencesOn!!.getBoolean(Utils.SWITCH_ON, false)
        val isSwitchOnRepeat = sharedpreferencesOn!!.getBoolean(Utils.SWITCH_ON_REPEAT, false)
        sharedpreferencesOff = getSharedPreferences(Utils.POWER_OFF, MODE_PRIVATE)
        editOff = sharedpreferencesOff!!.edit()
        val isSwitchOff = sharedpreferencesOff!!.getBoolean(Utils.SWITCH_OFF, false)
        val isSwitchOffRepeat =
            sharedpreferencesOff!!.getBoolean(Utils.SWITCH_OFF_REPEAT, false)
        switchOn!!.isChecked = isSwitchOn
        switchOff!!.isChecked = isSwitchOff
        switchOnRepeat!!.isChecked = isSwitchOnRepeat
        switchOffRepeat!!.isChecked = isSwitchOffRepeat
        defaultHourOn = sharedpreferencesOn!!.getInt(Utils.POWER_ON_DAY_OF_HOUR, 0)
        defaultMinuteOn = sharedpreferencesOn!!.getInt(Utils.POWER_ON_MINUTE, 0)
        textOnTime!!.text = String.format("%s:%s", defaultHourOn, defaultMinuteOn)
        defaultHourOff = sharedpreferencesOff!!.getInt(Utils.POWER_OFF_DAY_OF_HOUR, 0)
        defaultMinuteOff = sharedpreferencesOff!!.getInt(Utils.POWER_OFF_MINUTE, 0)
        textOffTime!!.text = String.format("%s:%s", defaultHourOff, defaultMinuteOff)
        relatOn!!.visibility = if (isSwitchOn) View.VISIBLE else View.GONE
        switchOnRepeat!!.visibility = if (isSwitchOn) View.VISIBLE else View.GONE
        relatOff!!.visibility =
            if (isSwitchOff) View.VISIBLE else View.GONE
        switchOffRepeat!!.visibility = if (isSwitchOff) View.VISIBLE else View.GONE
        stateCheckPowerOff = isSwitchOff
        stateCheckPowerOn = isSwitchOn
    }

    private fun setAlarm() {
        val intent = Intent(this@MainActivity, AlarmReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
    }

    override fun onRestart() {
        super.onRestart()
        switchOn!!.requestFocus()
    }

    private var hourOn = 0
    private var minuteOn = 0
    private var hourOff = 0
    private var minuteOff = 0
    private var timeOn: Long = 0
    private var timeOff: Long = 0
    private var stateOn = false //定时开机时间是否有修改
    private var stateOff = false //定时关机时间是否有修改
    private var stateCheckPowerOff = false //监听定时关机开关状态
    private var stateCheckPowerOn = false //监听定时开机开关状态
    override fun onClick(view: View) {
        when (view.id) {
            R.id.relat_off -> {
                inittimepicker_datepicker(false)
                createDialog(false)
            }
            R.id.relat_on -> {
                inittimepicker_datepicker(true)
                createDialog(true)
            }
            R.id.item_cancel -> finish()
            R.id.item_commit -> commitData()
            else -> {}
        }
    }

    private fun commitData() {
        if (stateCheckPowerOn) {
            if (!stateOn) {
                //如果定时关机默认是关的，开启定时开机就提交
                getdefaultOnTime()
            }
            cancelOnTime()
            setOnTime(timeOn)
        } else {
            cancelOnTime()
        }
        if (stateCheckPowerOff) {
            if (!stateOff) {
                //如果定时关机默认是关的，开启定时开机就提交
                getDefaultOffTime()
            }
            alarmManager!![AlarmManager.RTC_WAKEUP, timeOff] = pendingIntent!!
        } else {
            alarmManager!!.cancel(pendingIntent!!)
        }
        editOn!!.apply()
        editOff!!.apply()
        Toast.makeText(this, "Successfully saved", Toast.LENGTH_LONG).show()
        finish()
    }

    /**
     * true 开机对话框，false关机对话框
     *
     * @param on_off
     */
    private fun inittimepicker_datepicker(on_off: Boolean) {
        timepickerView = LayoutInflater.from(this).inflate(R.layout.timepicker_dialog, null)
        timepickerOn = timepickerView!!.findViewById(R.id.timepicker_on)
        timepickerOn!!.setIs24HourView(true)
        if (on_off) {
            hourOn = timepickerOn!!.hour
            minuteOn = timepickerOn!!.minute
            timepickerOn!!.setOnTimeChangedListener { timePicker: TimePicker?, hour: Int, minute: Int ->
                hourOn = hour
                minuteOn = minute
            }
        } else {
            hourOff = timepickerOn!!.hour
            minuteOff = timepickerOn!!.minute
            timepickerOn!!.setOnTimeChangedListener { timePicker: TimePicker?, hour: Int, minute: Int ->
                hourOff = hour
                minuteOff = minute
            }
        }
    }

    /**
     * true 设置开机时间，false设置关机时间
     *
     * @param isOn
     */
    private fun createDialog(isOn: Boolean) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("set time")
        dialog.setView(timepickerView)
        dialog.setPositiveButton(
            android.R.string.ok
        ) { dialogInterface, i ->
            if (isOn) {
                if (getOnTime() > 0) {
                    stateOn = true
                    savePowerOnTime()
                    textOnTime!!.text = String.format("%s:%s", hourOn, minuteOn)
                }
            } else {
                if (offTime > 0) {
                    stateOff = true
                    savePowerOffTime()
                    textOffTime!!.text = String.format("%s:%s", hourOff, minuteOff)
                }
            }
        }.setNegativeButton(android.R.string.cancel) { dialogInterface, i ->
            Log.d(TAG, "cancel----------")
            clearDate(isOn)
            dialogInterface.dismiss()
        }
        val alertDialog = dialog.create()
        //alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun getOnTime(): Long {
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = hourOn
        calendar[Calendar.MINUTE] = minuteOn
        calendar[Calendar.SECOND] = 0
        var time = calendar.timeInMillis
        val systemTime = System.currentTimeMillis()
        Log.d(TAG, "getOnTime---------->" + (time - systemTime) / 1000)
        if (time < systemTime) {
            calendar[Calendar.HOUR_OF_DAY] = hourOn
            calendar[Calendar.MINUTE] = minuteOn
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.DAY_OF_MONTH] = calendar[Calendar.DAY_OF_MONTH] + 1
            time = calendar.timeInMillis
            timeOn = (time - systemTime) / 1000
            Toast.makeText(
                this,
                "The scheduled boot time is tomorrow at" + hourOn + "o'clock" + minuteOn + "minutes",
                Toast.LENGTH_LONG
            ).show()
        } else {
            timeOn = (time - systemTime) / 1000
            Toast.makeText(
                this,
                "The scheduled boot time is at" + hourOn + "o'clock" + minuteOn + "minutes",
                Toast.LENGTH_LONG
            ).show()
        }
        saveyearMonthDay(true, calendar)
        Log.d(TAG, "time_on------------->$timeOn")
        return timeOn
    }

    /**
     * 清除日期
     *
     * @param isPowerOn
     */
    private fun clearDate(isPowerOn: Boolean) {
        if (isPowerOn) {
            hourOn = 0
            minuteOn = 0
        } else {
            hourOff = 0
            minuteOff = 0
        }
    }

    private val offTime: Long
        private get() {
            val calendar = Calendar.getInstance()
            calendar[Calendar.HOUR_OF_DAY] = hourOff
            calendar[Calendar.MINUTE] = minuteOff
            calendar[Calendar.SECOND] = 0
            timeOff = calendar.timeInMillis
            if (timeOff < System.currentTimeMillis()) {
                calendar[Calendar.HOUR_OF_DAY] = hourOff
                calendar[Calendar.MINUTE] = minuteOff
                calendar[Calendar.SECOND] = 0
                calendar[Calendar.DAY_OF_MONTH] = calendar[Calendar.DAY_OF_MONTH] + 1
                timeOff = calendar.timeInMillis
                Toast.makeText(
                    this,
                    "The shutdown date you set is tomorrow" + hourOff + "o'clock" + minuteOff + "minutes",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "The shutdown date you have set is" + hourOff + "o'clock" + minuteOff + "minutes",
                    Toast.LENGTH_LONG
                ).show()
            }
            saveyearMonthDay(false, calendar)
            return timeOff
        }

    private fun savePowerOnTime() {
        editOn!!.putInt(Utils.POWER_ON_DAY_OF_HOUR, hourOn)
        editOn!!.putInt(Utils.POWER_ON_MINUTE, minuteOn)
    }

    private fun savePowerOffTime() {
        editOff!!.putInt(Utils.POWER_OFF_DAY_OF_HOUR, hourOff)
        editOff!!.putInt(Utils.POWER_OFF_MINUTE, minuteOff)
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, ischeck: Boolean) {
        when (compoundButton.id) {
            R.id.switch_on -> {
                stateCheckPowerOn = ischeck
                relatOn!!.visibility = if (ischeck) View.VISIBLE else View.GONE
                switchOnRepeat!!.visibility = if (ischeck) View.VISIBLE else View.GONE
                editOn!!.putBoolean(Utils.SWITCH_ON, ischeck)
            }
            R.id.switch_off -> {
                stateCheckPowerOff = ischeck
                relatOff!!.visibility = if (ischeck) View.VISIBLE else View.GONE
                switchOffRepeat!!.visibility =
                    if (ischeck) View.VISIBLE else View.GONE
                editOff!!.putBoolean(Utils.SWITCH_OFF, ischeck)
            }
            R.id.switch_on_repeat -> editOn!!.putBoolean(Utils.SWITCH_ON_REPEAT, ischeck)
            R.id.switch_off_repeat -> editOff!!.putBoolean(Utils.SWITCH_OFF_REPEAT, ischeck)
            else -> {}
        }
    }

    /**
     * 只开启了定时开机就提交的方法
     */
    private fun getdefaultOnTime() {
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = defaultHourOn
        calendar[Calendar.MINUTE] = defaultMinuteOn
        calendar[Calendar.SECOND] = 0
        var time = calendar.timeInMillis
        val systemTime = System.currentTimeMillis()
        Log.d(TAG, "getOnTime---------->" + (time - systemTime) / 1000)
        if (time < systemTime) {
            calendar[Calendar.HOUR_OF_DAY] = defaultHourOn
            calendar[Calendar.MINUTE] = defaultMinuteOn
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.DAY_OF_MONTH] = calendar[Calendar.DAY_OF_MONTH] + 1
            time = calendar.timeInMillis
            timeOn = (time - systemTime) / 1000
            Toast.makeText(
                this,
                "The boot date you set is tomorrow" + defaultHourOn + "o'clock" + defaultMinuteOn + "minutes",
                Toast.LENGTH_LONG
            ).show()
        } else {
            timeOn = (time - systemTime) / 1000
            Toast.makeText(
                this,
                "The boot date you have set is" + defaultHourOn + "o'clock" + defaultMinuteOn + "minutes",
                Toast.LENGTH_LONG
            ).show()
        }
        saveyearMonthDay(true, calendar)
        Log.d(TAG, "time_on------------->$timeOn")
    }

    /**
     * 只开启了定时关机就提交的方法
     */
    private fun getDefaultOffTime() {
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = defaultHourOff
        calendar[Calendar.MINUTE] = defaultMinuteOff
        calendar[Calendar.SECOND] = 0
        timeOff = calendar.timeInMillis
        if (timeOff < System.currentTimeMillis()) {
            calendar[Calendar.HOUR_OF_DAY] = defaultHourOff
            calendar[Calendar.MINUTE] = defaultMinuteOff
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.DAY_OF_MONTH] = calendar[Calendar.DAY_OF_MONTH] + 1
            timeOff = calendar.timeInMillis
            Toast.makeText(
                this,
                "The shutdown date you set is tomorrow" + defaultHourOff + "o'clock" + defaultMinuteOff + "minutes",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                this,
                "The shutdown date you have set is" + defaultHourOff + "o'clock" + defaultMinuteOff + "minutes",
                Toast.LENGTH_LONG
            ).show()
        }
        saveyearMonthDay(false, calendar)
    }

    /**
     * isPowerOn等于true，写入开机日期年月日，反之关机日期
     * 这个方法是为了没有开启重复开关而准备
     *
     * @param isPowerOn
     * @param calendar
     */
    private fun saveyearMonthDay(isPowerOn: Boolean, calendar: Calendar) {
        if (isPowerOn) {
            editOn!!.putInt(Utils.POWER_ON_YEAR, calendar[Calendar.YEAR])
            editOn!!.putInt(Utils.POWER_ON_MONTH, calendar[Calendar.MONTH])
            editOn!!.putInt(Utils.POWER_ON_DAY, calendar[Calendar.DAY_OF_MONTH])
        } else {
            editOff!!.putInt(Utils.POWER_OFF_YEAR, calendar[Calendar.YEAR])
            editOff!!.putInt(Utils.POWER_OFF_MONTH, calendar[Calendar.MONTH])
            editOff!!.putInt(Utils.POWER_OFF_DAY, calendar[Calendar.DAY_OF_MONTH])
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}