package com.kito.feature.calendar.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kito.core.designsystem.UIColors
import com.kito.feature.calendar.domain.model.CalendarEntry
import com.kito.feature.calendar.domain.model.CalendarEntryType
import kotlinx.datetime.LocalDate

private const val MAX_CHIPS = 2

@Composable
fun DayCell(
    date: LocalDate,
    entries: List<CalendarEntry>,
    isToday: Boolean,
    inMonth: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiColors = UIColors()
    val isHoliday = entries.any { it.type == CalendarEntryType.HOLIDAY }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .height(66.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isHoliday && inMonth) Color(0xFF3FB68B).copy(alpha = 0.08f) else Color.Transparent)
            .clickable(onClick = onClick)
            .alpha(if (inMonth) 1f else 0.3f)
            .padding(horizontal = 2.dp, vertical = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .then(
                    if (isToday) Modifier.background(
                        Brush.verticalGradient(listOf(uiColors.accentOrangeStart, uiColors.accentOrangeEnd))
                    ) else Modifier
                )
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                style = MaterialTheme.typography.labelMedium,
                color = if (isToday) Color.Black else uiColors.textPrimary,
            )
        }
        if (inMonth) {
            entries.take(MAX_CHIPS).forEach { EntryChip(it) }
            if (entries.size > MAX_CHIPS) {
                Text(
                    text = "+${entries.size - MAX_CHIPS}",
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    color = uiColors.textSecondary,
                    modifier = Modifier.fillMaxWidth().padding(start = 3.dp)
                )
            }
        }
    }
}
