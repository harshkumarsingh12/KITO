package com.kito.feature.calendar

import com.kito.feature.calendar.presentation.components.buildMonthCells
import com.kito.feature.calendar.presentation.components.monthIndex
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildMonthCellsTest {
    private fun cells(year: Int, month: Int) = buildMonthCells(LocalDate(year, month, 1).monthIndex())

    @Test
    fun november2026_fiveWeeks_noTrailingNextMonthWeek() {
        val c = cells(2026, 11) // starts Sunday, 30 days
        assertEquals(35, c.size)
        assertEquals(LocalDate(2026, 11, 1), c.first())
        assertEquals(LocalDate(2026, 12, 5), c.last())
    }

    @Test
    fun january2027_sixWeeks() {
        val c = cells(2027, 1) // starts Friday, 31 days
        assertEquals(42, c.size)
        assertEquals(LocalDate(2026, 12, 27), c.first())
        assertEquals(LocalDate(2027, 2, 6), c.last())
    }

    @Test
    fun february2026_fourWeeks() = assertEquals(28, cells(2026, 2).size) // starts Sunday, 28 days
}
