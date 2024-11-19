package ru.firstmt.myrtcshutdown.utils

import android.util.Log
import java.io.DataOutputStream

object Utils {
    private val TAG = Utils::class.java.simpleName
    const val POWER_ON = "power_on"
    const val POWER_ON_DAY_OF_HOUR = "power_on_day_of_hour"
    const val POWER_ON_MINUTE = "power_on_minute"
    const val POWER_ON_YEAR = "power_on_year"
    const val POWER_ON_MONTH = "power_on_month"
    const val POWER_ON_DAY = "power_on_day"
    const val SWITCH_ON = "switch_on"
    const val SWITCH_ON_REPEAT = "switch_on_repeat"
    const val POWER_OFF = "power_off"
    const val POWER_OFF_DAY_OF_HOUR = "power_off_day_of_hour"
    const val POWER_OFF_MINUTE = "power_off_minute"
    const val POWER_OFF_YEAR = "power_off_year"
    const val POWER_OFF_MONTH = "power_off_month"
    const val POWER_OFF_DAY = "power_off_day"
    const val SWITCH_OFF = "switch_off"
    const val SWITCH_OFF_REPEAT = "switch_off_repeat"
    @JvmStatic
    fun setOnTime(time: Long) {
        if (time < 0) {
            return
        }
        var process: Process? = null
        var os: DataOutputStream? = null
        Log.d(TAG, "ontime------------->$time")
        try {
            val command = "echo +$time > /sys/class/rtc/rtc0/wakealarm"
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)
            os.writeBytes(
                """
    $command
    
    """.trimIndent()
            )
            os.writeBytes("exit\n")
            os.flush()
            process.waitFor()
            Log.d(TAG, "已写入-------->$time")
        } catch (e: Exception) {
            Log.d("runtime_exception:", e.message!!)
        } finally {
            try {
                os?.close()
                process!!.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @JvmStatic
    fun cancelOnTime() {
        var process: Process? = null
        var os: DataOutputStream? = null
        try {
            val command = "echo " + 0 + " > " + "/sys/class/rtc/rtc0/wakealarm"
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)
            os.writeBytes(
                """
    $command
    
    """.trimIndent()
            )
            os.writeBytes("exit\n")
            os.flush()
            process.waitFor()
            Log.d(TAG, "已写入 0-------->")
        } catch (e: Exception) {
            Log.d("runtime_exception:", e.message!!)
        } finally {
            try {
                os?.close()
                process!!.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}