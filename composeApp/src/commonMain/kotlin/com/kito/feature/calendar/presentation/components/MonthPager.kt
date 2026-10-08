package com.kito.feature.calendar.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kito.core.designsystem.UIColors
import com.kito.feature.calendar.domain.model.CalendarEntry
import kotlinx.datetime.LocalDate

/** Months reachable on either side of today. */
private const val PAGE_SPAN = 120

/** Swipeable month grid inside an app-styled gradient card. */
@Composable
fun MonthPager(
    today: LocalDate,
    displayedMonth: Int,
    months: Map<Int, Map<LocalDate, List<CalendarEntry>>>,
    onMonthSettled: (Int) -> Unit,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiColors = UIColors()
    val firstIndex = today.monthIndex() - PAGE_SPAN
    val pagerState = rememberPagerState(
        initialPage = (displayedMonth - firstIndex).coerceIn(0, PAGE_SPAN * 2)
    ) { PAGE_SPAN * 2 + 1 }
    val latestOnSettled = rememberUpdatedState(onMonthSettled)

    // Top bar arrows and the Today button move the pager; swipes report back via settledPage.
    LaunchedEffect(displayedMonth) {
        val target = (displayedMonth - firstIndex).coerceIn(0, PAGE_SPAN * 2)
        if (pagerState.currentPage != target) pagerState.animateScrollToPage(target)
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { latestOnSettled.value(firstIndex + it) }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(uiColors.cardBackground, Color(0xFF2F222F), Color(0xFF2F222F), uiColors.cardBackgroundHigh)
                )
            )
            .padding(horizontal = 6.dp, vertical = 12.dp)
    ) {
        WeekdayHeaderRow()
        Spacer(Modifier.height(6.dp))
        HorizontalPager(state = pagerState, beyondViewportPageCount = 1) { page ->
            val index = firstIndex + page
            MonthGrid(
                monthIndex = index,
                entriesByDate = months[index].orEmpty(),
                today = today,
                onDayClick = onDayClick,
            )
        }
    }
}
