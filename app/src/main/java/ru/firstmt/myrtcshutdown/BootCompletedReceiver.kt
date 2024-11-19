package ru.firstmt.myrtcshutdown

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val intent1 = Intent(context, BootActivity::class.java)
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent1)
        }
    }
}