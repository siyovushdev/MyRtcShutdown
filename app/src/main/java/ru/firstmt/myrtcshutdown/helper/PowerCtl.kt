package ru.firstmt.myrtcshutdown.helper

import com.topjohnwu.superuser.Shell

object PowerCtl {
    fun shutdownNow(): Boolean {
        L.i(L.T_PWR, "Shutdown requested via sys.powerctl")
        val r1 = Shell.cmd("setprop sys.powerctl shutdown").exec()
        logResult("sys.powerctl shutdown", r1)
        if (r1.isSuccess) return true

        L.w(L.T_PWR, "sys.powerctl failed, fallback to reboot -p")
        val r2 = Shell.cmd("reboot -p").exec()
        logResult("reboot -p", r2)
        return r2.isSuccess
    }

    private fun logResult(cmd: String, res: Shell.Result) {
        L.i(L.T_PWR, "cmd: $cmd, code=${res.code}, ok=${res.isSuccess}")
        if (res.out.isNotEmpty()) L.d(L.T_PWR, "stdout: ${res.out.joinToString(" | ")}")
        if (res.err.isNotEmpty()) L.w(L.T_PWR, "stderr: ${res.err.joinToString(" | ")}")
    }
}
