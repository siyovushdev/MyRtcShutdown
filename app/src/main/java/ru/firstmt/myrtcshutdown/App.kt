package ru.firstmt.myrtcshutdown

import android.app.Application
import com.topjohnwu.superuser.Shell
import ru.firstmt.myrtcshutdown.helper.L

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Shell.enableVerboseLogging = true
        Shell.getShell { L.i(L.T_APP, "Root shell preloaded: success=${it.isAlive}") }
        L.i(L.T_APP, "App started; build=${BuildConfig.BUILD_TYPE}")
    }
}
