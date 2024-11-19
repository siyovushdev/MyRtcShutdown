package ru.firstmt.myrtcshutdown

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive---------->")
        //	PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
//	pm.shutdown(false,null,false);
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            try {
                val method = pm.javaClass.getMethod(
                    "shutdown",
                    Boolean::class.java,
                    String::class.java,
                    Boolean::class.java
                )
                method.invoke(pm, false, null, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = AlarmReceiver::class.java.simpleName
    }
}