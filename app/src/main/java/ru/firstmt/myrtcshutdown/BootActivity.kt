package ru.firstmt.myrtcshutdown

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import ru.firstmt.myrtcshutdown.utils.Utils
import ru.firstmt.myrtcshutdown.utils.Utils.cancelOnTime
import ru.firstmt.myrtcshutdown.utils.Utils.setOnTime
import java.util.*

class BootActivity : Activity() {
    private var sharedPreferencesOn: SharedPreferences? = null
    private var sharedpreferencesOff: SharedPreferences? = null
    private var isSwitchOn = false
    private var isSwitchOnRepeat = false
    private var isSwitchOff = false
    private var isSwitchOffRepeat = false
    private var timeOff: Long = 0
    private var timeOn: Long = 0
    private var alarmManager: AlarmManager? = null
    private var pendingIntent: PendingIntent? = null
    private var hour = 0
    private var minute = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getData() //获取保存的数据
        getOnTime()
        getOffTime()
    }

    private fun getData() {
            sharedPreferencesOn = getSharedPreferences(Utils.POWER_ON, MODE_PRIVATE)
            isSwitchOn = sharedPreferencesOn!!.getBoolean(Utils.SWITCH_ON, false)
            isSwitchOnRepeat = sharedPreferencesOn!!.getBoolean(Utils.SWITCH_ON_REPEAT, false)
            sharedpreferencesOff = getSharedPreferences(Utils.POWER_OFF, MODE_PRIVATE)
            isSwitchOff = sharedpreferencesOff!!.getBoolean(Utils.SWITCH_OFF, false)
            isSwitchOffRepeat = sharedpreferencesOff!!.getBoolean(Utils.SWITCH_OFF_REPEAT, false)
        }
    private fun getOnTime() {
            if (isSwitchOn && !isSwitchOnRepeat) {
                Log.d(TAG, "getOnTime11111111")
                hour = sharedPreferencesOn!!.getInt(Utils.POWER_ON_DAY_OF_HOUR, 0)
                minute = sharedPreferencesOn!!.getInt(Utils.POWER_ON_MINUTE, 0)
                val year = sharedPreferencesOn!!.getInt(Utils.POWER_ON_YEAR, 0)
                val month = sharedPreferencesOn!!.getInt(Utils.POWER_ON_MONTH, 0)
                val day = sharedPreferencesOn!!.getInt(Utils.POWER_ON_DAY, 0)
                val calendar = Calendar.getInstance()
                calendar[Calendar.YEAR] = year
                calendar[Calendar.MONTH] = month
                calendar[Calendar.DAY_OF_MONTH] = day
                calendar[Calendar.HOUR_OF_DAY] = hour
                calendar[Calendar.MINUTE] = minute
                calendar[Calendar.SECOND] = 0
                timeOn = calendar.timeInMillis
                val system_time = System.currentTimeMillis()
                Log.d(TAG, "timeon-------->$timeOn")
                Log.d(TAG, "systim-------->$system_time")
                if (timeOn > system_time) {
                    cancelOnTime()
                    setOnTime((timeOn - system_time) / 1000)
                }
            } else if (isSwitchOn && isSwitchOnRepeat) {
                Log.d(TAG, "getOnTime222222")
                hour = sharedPreferencesOn!!.getInt(Utils.POWER_ON_DAY_OF_HOUR, 0)
                minute = sharedPreferencesOn!!.getInt(Utils.POWER_ON_MINUTE, 0)
                val calendar = Calendar.getInstance()
                calendar[Calendar.HOUR_OF_DAY] = hour
                calendar[Calendar.MINUTE] = minute
                calendar[Calendar.SECOND] = 0
                timeOn = calendar.timeInMillis
                Log.d(TAG, "timeon-------->$timeOn")
                Log.d(TAG, "systim-------->" + System.currentTimeMillis())
                if (timeOn < System.currentTimeMillis()) {
                    calendar[Calendar.HOUR_OF_DAY] = hour
                    calendar[Calendar.MINUTE] = minute
                    calendar[Calendar.DAY_OF_MONTH] = calendar[Calendar.DAY_OF_MONTH] + 1
                    calendar[Calendar.SECOND] = 0
                    timeOn = calendar.timeInMillis
                }
                Log.d(TAG, "timeon new---------->$timeOn")
                cancelOnTime()
                setOnTime((timeOn - System.currentTimeMillis()) / 1000)
            }
        }
    private fun getOffTime() {
            if (isSwitchOff && !isSwitchOffRepeat) {
                Log.d(TAG, "getOffTime111111111")
                val hour = sharedpreferencesOff!!.getInt(Utils.POWER_OFF_DAY_OF_HOUR, 0)
                val minute = sharedpreferencesOff!!.getInt(Utils.POWER_OFF_MINUTE, 0)
                val year = sharedpreferencesOff!!.getInt(Utils.POWER_OFF_YEAR, 0)
                val month = sharedpreferencesOff!!.getInt(Utils.POWER_OFF_MONTH, 0)
                val day = sharedpreferencesOff!!.getInt(Utils.POWER_OFF_DAY, 0)
                val calendar = Calendar.getInstance()
                calendar[Calendar.YEAR] = year
                calendar[Calendar.MONTH] = month
                calendar[Calendar.DAY_OF_MONTH] = day
                calendar[Calendar.HOUR_OF_DAY] = hour
                calendar[Calendar.MINUTE] = minute
                calendar[Calendar.SECOND] = 0
                timeOff = calendar.timeInMillis
                val system_time = System.currentTimeMillis()
                Log.d(TAG, "timeoff---------->$timeOff")
                Log.d(TAG, "systemt---------->$system_time")
                if (timeOff > system_time) {
                    setAlarm(timeOff)
                }
            } else if (isSwitchOff && isSwitchOffRepeat) {
                Log.d(TAG, "getOffTime222222222")
                val hour = sharedpreferencesOff!!.getInt(Utils.POWER_OFF_DAY_OF_HOUR, 0)
                val minute = sharedpreferencesOff!!.getInt(Utils.POWER_OFF_MINUTE, 0)
                val calendar = Calendar.getInstance()
                calendar[Calendar.HOUR_OF_DAY] = hour
                calendar[Calendar.MINUTE] = minute
                calendar[Calendar.SECOND] = 0
                timeOff = calendar.timeInMillis
                Log.d(TAG, "timeoff---------->$timeOff")
                Log.d(TAG, "systemt---------->" + System.currentTimeMillis())
                if (timeOff < System.currentTimeMillis()) {
                    calendar[Calendar.HOUR_OF_DAY] = hour
                    calendar[Calendar.MINUTE] = minute
                    calendar[Calendar.SECOND] = 0
                    calendar[Calendar.DAY_OF_MONTH] = calendar[Calendar.DAY_OF_MONTH] + 1
                    timeOff = calendar.timeInMillis
                }
                Log.d(TAG, "timeoff new ---------->$timeOff")
                setAlarm(timeOff)
            }
        }

    override fun onStart() {
        super.onStart()
        finish()
    }

    private fun setAlarm(timeoff: Long) {
        if (timeoff < 0) {
            return
        }
        val intent = Intent(applicationContext, AlarmReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager!!.set(AlarmManager.RTC_WAKEUP, timeoff, pendingIntent!!)
    }

    companion object {
        private val TAG = BootActivity::class.java.simpleName
    }
}