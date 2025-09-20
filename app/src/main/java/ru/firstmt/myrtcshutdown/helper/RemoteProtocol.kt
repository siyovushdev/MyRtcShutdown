package ru.firstmt.myrtcshutdown.helper

object RemoteProtocol {
    const val ACTION_SET_SCHEDULE = "ru.firstmt.myrtcshutdown.SET_SCHEDULE"
    const val ACTION_TEST_CYCLE   = "ru.firstmt.myrtcshutdown.TEST_CYCLE"
    const val ACTION_RESULT       = "ru.firstmt.myrtcshutdown.SCHEDULE_RESULT"

    const val EXTRA_FORMAT  = "FORMAT"
    const val EXTRA_PAYLOAD = "PAYLOAD"
    const val FORMAT_JSON   = "json"

    const val EXTRA_OFF_SEC = "OFF_SEC"
    const val EXTRA_ON_SEC  = "ON_SEC"

    const val ACTION_INIT = "ru.firstmt.myrtcshutdown.INIT"

}
