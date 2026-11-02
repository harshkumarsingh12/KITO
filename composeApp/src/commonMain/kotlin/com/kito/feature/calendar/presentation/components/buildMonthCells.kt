package com.kito.feature.calendar.presentation.components

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** Sunday-first whole weeks covering the month (4–6 rows), with no trailing week of next-month days. */
fun buildMonthCells(monthIndex: Int): List<LocalDate> {
    val first = LocalDate(monthIndex.indexYear(), monthIndex.indexMonth(), 1)
    val start = first.minus(first.dayOfWeek.isoDayNumber % 7, DateTimeUnit.DAY)
    val days = first.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).dayOfMonth
    val weeks = (first.dayOfWeek.isoDayNumber % 7 + days + 6) / 7
    return List(weeks * 7) { start.plus(it, DateTimeUnit.DAY) }
}
