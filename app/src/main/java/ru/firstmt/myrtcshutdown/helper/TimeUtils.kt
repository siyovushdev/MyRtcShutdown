package ru.firstmt.myrtcshutdown.helper

import java.time.ZoneId
import java.time.ZonedDateTime

fun nextDailyEpochUtc(hour: Int, minute: Int, zone: ZoneId = ZoneId.systemDefault()): Long {
    val now = ZonedDateTime.now(zone)
    var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
    if (!next.isAfter(now)) next = next.plusDays(1)
    return next.toInstant().epochSecond
}

fun nextDailyWallClockMillis(hour: Int, minute: Int): Long {
    val now = java.util.Calendar.getInstance()
    now.set(java.util.Calendar.SECOND, 0)
    now.set(java.util.Calendar.MILLISECOND, 0)
    now.set(java.util.Calendar.HOUR_OF_DAY, hour)
    now.set(java.util.Calendar.MINUTE, minute)
    val target = now.timeInMillis
    return if (target <= System.currentTimeMillis())
        target + 24 * 60 * 60 * 1000L
    else target
}
