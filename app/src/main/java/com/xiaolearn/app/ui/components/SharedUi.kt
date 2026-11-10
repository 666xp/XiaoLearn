package com.xiaolearn.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiaolearn.app.data.model.XiaoKey
import com.xiaolearn.app.ui.theme.Accent
import com.xiaolearn.app.ui.theme.AccentSoft
import com.xiaolearn.app.ui.theme.HoleClosed
import com.xiaolearn.app.ui.theme.HoleOpen
import com.xiaolearn.app.ui.theme.Ink
import com.xiaolearn.app.ui.theme.InkMute

@Composable
fun KeySwitcher(
    selected: XiaoKey,
    onSelect: (XiaoKey) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(AccentSoft, RoundedCornerShape(20.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        XiaoKey.entries.forEach { key ->
            val active = key == selected
            Text(
                text = key.label,
                modifier = Modifier
                    .background(
                        if (active) Accent else Color.Transparent,
                        RoundedCornerShape(17.dp)
                    )
                    .clickable { onSelect(key) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                color = if (active) Color.White else InkMute,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun HoleDiagram(
    holes: List<Boolean>?,
    labels: List<String>,
    isRest: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isRest || holes == null) {
            Text(
                text = if (isRest) "休止" else "选择一个音",
                style = MaterialTheme.typography.titleMedium,
                color = InkMute
            )
            return
        }

        Canvas(
            modifier = Modifier
                .width(72.dp)
                .height(220.dp)
        ) {
            val bodyWidth = size.width * 0.42f
            val left = (size.width - bodyWidth) / 2f
            drawRoundRect(
                color = Accent.copy(alpha = 0.35f),
                topLeft = Offset(left, 0f),
                size = Size(bodyWidth, size.height),
                cornerRadius = CornerRadius(bodyWidth / 2f, bodyWidth / 2f)
            )
            val count = holes.size.coerceAtMost(8)
            val startY = size.height * 0.12f
            val endY = size.height * 0.92f
            val step = (endY - startY) / (count - 1).coerceAtLeast(1)
            holes.take(count).forEachIndexed { index, closed ->
                val cy = startY + step * index
                val radius = 11.dp.toPx()
                if (closed) {
                    drawCircle(color = HoleClosed, radius = radius, center = Offset(size.width / 2f, cy))
                } else {
                    drawCircle(color = HoleOpen, radius = radius, center = Offset(size.width / 2f, cy))
                    drawCircle(
                        color = HoleClosed,
                        radius = radius,
                        center = Offset(size.width / 2f, cy),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendDot(closed = true, text = "闭")
            LegendDot(closed = false, text = "开")
        }
        if (labels.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = labels.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = InkMute,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun LegendDot(closed: Boolean, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(if (closed) HoleClosed else HoleOpen, CircleShape)
                .then(
                    if (!closed) Modifier.border(1.dp, HoleClosed, CircleShape) else Modifier
                )
        )
        Spacer(Modifier.width(4.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = InkMute)
    }
}

@Composable
fun JianpuChip(
    text: String,
    selected: Boolean,
    passed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = when {
        selected -> Accent
        passed -> AccentSoft.copy(alpha = 0.55f)
        else -> Color.Transparent
    }
    val fg = when {
        selected -> Color.White
        passed -> InkMute
        else -> Ink
    }
    Text(
        text = text,
        modifier = modifier
            .background(bg, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        color = fg,
        fontSize = if (selected) 22.sp else 18.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
    )
}

fun tokenLabel(pitch: String?, rest: Boolean, beats: Double): String {
    if (rest) return "空"
    val base = pitch ?: "?"
    val mark = when {
        beats >= 4 -> "——"
        beats >= 2 -> "—"
        beats <= 0.5 -> "·"
        else -> ""
    }
    return base + mark
}
