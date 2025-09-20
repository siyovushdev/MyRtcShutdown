package ru.firstmt.myrtcshutdown.helper

import com.topjohnwu.superuser.Shell

object RtcWake {
    private val paths = listOf(
        "/sys/class/rtc/rtc0/wakealarm",
        "/sys/class/rtc/rtc1/wakealarm"
    )

    fun clear(): Boolean {
        L.i(L.T_RTC, "Clearing wakealarm (echo 0)")
        return writeToAny("0")
    }

    fun programEpoch(epochSecondsUtc: Long): Boolean {
        L.i(L.T_RTC, "Program RTC with absolute epoch: ${L.epoch(epochSecondsUtc)}")
        if (!clear()) { L.e(L.T_RTC, "Failed to clear wakealarm"); return false }
        return writeToAny(epochSecondsUtc.toString())
    }

    fun programPlusSeconds(deltaSeconds: Long): Boolean {
        L.i(L.T_RTC, "Program RTC with +$deltaSeconds seconds")
        if (!clear()) { L.e(L.T_RTC, "Failed to clear wakealarm"); return false }
        return writeToAny("+$deltaSeconds")
    }

    private fun writeToAny(payload: String): Boolean {
        for (p in paths) {
            val cmd = "echo $payload > $p"
            val res = Shell.cmd(cmd).exec()
            L.i(L.T_RTC, "write: '$cmd' code=${res.code} ok=${res.isSuccess}")
            if (res.out.isNotEmpty()) L.d(L.T_RTC, "stdout: ${res.out.joinToString(" | ")}")
            if (res.err.isNotEmpty()) L.w(L.T_RTC, "stderr: ${res.err.joinToString(" | ")}")
            if (res.isSuccess) {
                // Дополнительно прочтём текущее значение
                val read = Shell.cmd("cat $p").exec()
                L.i(L.T_RTC, "read back ($p): out=${read.out.joinToString()} code=${read.code}")
                return true
            }
        }
        L.e(L.T_RTC, "Failed to write payload to any wakealarm path: $payload")
        return false
    }
}
