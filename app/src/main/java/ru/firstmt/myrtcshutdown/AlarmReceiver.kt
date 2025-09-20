package ru.firstmt.myrtcshutdown

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.firstmt.myrtcshutdown.helper.L
import ru.firstmt.myrtcshutdown.helper.PowerCtl
import ru.firstmt.myrtcshutdown.helper.Scheduler

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        L.i(L.T_ALARM, "onReceive action=${intent.action} extras=${intent.extras}")
        if (intent.action == Scheduler.ACTION_SHUTDOWN_WEEKLY) {
            val ok = PowerCtl.shutdownNow()
            L.i(L.T_ALARM, "shutdown result: $ok")
        }
    }
}
