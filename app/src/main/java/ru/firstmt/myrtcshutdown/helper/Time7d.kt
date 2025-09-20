package ru.firstmt.myrtcshutdown.helper

import java.time.*
import java.time.temporal.TemporalAdjusters
import java.util.*

/** Следующее наступление H:M для заданного dayOfWeek (Java Time) → millis (локальные часы) */
fun nextWeeklyWallClockMillis(day: DayOfWeek, hour: Int, minute: Int, zone: ZoneId = ZoneId.systemDefault()): Long {
    val now = ZonedDateTime.now(zone)
    var next = now.with(TemporalAdjusters.nextOrSame(day)).withHour(hour).withMinute(minute).withSecond(0).withNano(0)
    if (!next.isAfter(now)) next = next.plusWeeks(1)
    return next.toInstant().toEpochMilli()
}

/** Следующее наступление H:M для заданного dayOfWeek → UTC epoch seconds (для RTC wakealarm) */
fun nextWeeklyEpochUtc(day: DayOfWeek, hour: Int, minute: Int, zone: ZoneId = ZoneId.systemDefault()): Long {
    val millis = nextWeeklyWallClockMillis(day, hour, minute, zone)
    return millis / 1000
}

/** Маппинг нашего WeekDay к java.time.DayOfWeek */
fun WeekDay.toJavaDay(): DayOfWeek = when (this) {
    WeekDay.MON -> DayOfWeek.MONDAY
    WeekDay.TUE -> DayOfWeek.TUESDAY
    WeekDay.WED -> DayOfWeek.WEDNESDAY
    WeekDay.THU -> DayOfWeek.THURSDAY
    WeekDay.FRI -> DayOfWeek.FRIDAY
    WeekDay.SAT -> DayOfWeek.SATURDAY
    WeekDay.SUN -> DayOfWeek.SUNDAY
}
