package ru.firstmt.myrtcshutdown

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.firstmt.myrtcshutdown.helper.L
import ru.firstmt.myrtcshutdown.helper.Scheduler

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        L.i(L.T_BOOT, "BOOT_COMPLETED received, rescheduling all")
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            Scheduler(context).rescheduleAll()
        }
    }
}
