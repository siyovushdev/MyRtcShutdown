package ru.firstmt.myrtcshutdown.helper

enum class WeekDay(val calDay: Int) {
    MON(2), TUE(3), WED(4), THU(5), FRI(6), SAT(7), SUN(1); // Calendar: 1=Sunday
    companion object { val all = values().toList() }
}

data class DayPlan(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int
)
