package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun DoughnutChart(
    profit: Double,
    loss: Double,
    modifier: Modifier = Modifier
) {
    val cleanProfit = if (profit.isNaN() || profit.isInfinite() || profit < 0.0) 0.0 else profit
    val cleanLoss = if (loss.isNaN() || loss.isInfinite() || loss < 0.0) 0.0 else loss
    val total = cleanProfit + cleanLoss
    val profitPercentage = if (total > 0.0) (cleanProfit / total).toFloat() else 0.5f
    val lossPercentage = if (total > 0.0) (cleanLoss / total).toFloat() else 0.5f

    val animateSweep by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000),
        label = "sweep"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 22.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val chartSize = Size(diameter, diameter)
            val topLeft = Offset(
                (size.width - diameter) / 2,
                (size.height - diameter) / 2
            )

            if (profit == 0.0 && loss == 0.0) {
                // Draw empty circular gray track
                drawArc(
                    color = Color(0xFF1E293B),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = chartSize,
                    style = Stroke(width = strokeWidth)
                )
            } else {
                val profitSweep = 360f * profitPercentage * animateSweep
                val lossSweep = 360f * lossPercentage * animateSweep

                // Draw profit arc (Emerald)
                drawArc(
                    color = Color(0xFF10B981),
                    startAngle = -90f,
                    sweepAngle = profitSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = chartSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Draw loss arc (Rose)
                drawArc(
                    color = Color(0xFFEF4444),
                    startAngle = -90f + profitSweep,
                    sweepAngle = lossSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = chartSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val displayPct = if (total > 0.0) (cleanProfit / total * 100).toInt() else 100
            Text(
                text = "$displayPct%",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "Profit Ratio",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PerformanceBarChart(
    profitDays: Int,
    lossDays: Int,
    modifier: Modifier = Modifier
) {
    val maxDays = maxOf(profitDays, lossDays, 1).toFloat()
    
    val pAnimateHeight by animateFloatAsState(
        targetValue = profitDays / maxDays,
        animationSpec = tween(durationMillis = 800),
        label = "profit"
    )
    val lAnimateHeight by animateFloatAsState(
        targetValue = lossDays / maxDays,
        animationSpec = tween(durationMillis = 800),
        label = "loss"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // Profit Days Bar Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.fillMaxHeight().weight(1f)
        ) {
            Text(
                text = "$profitDays d",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF10B981)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.7f * pAnimateHeight.coerceIn(0.05f, 1f))
                    .width(32.dp)
                    .drawBehindGradient(Color(0xFF10B981), Color(0xFF047857))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Profit Days",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
        }

        // Loss Days Bar Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.fillMaxHeight().weight(1f)
        ) {
            Text(
                text = "$lossDays d",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFEF4444)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.7f * lAnimateHeight.coerceIn(0.05f, 1f))
                    .width(32.dp)
                    .drawBehindGradient(Color(0xFFEF4444), Color(0xFFB91C1C))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Loss Days",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

// A simple canvas drawing helper to get nice gradients with rounded corners
fun Modifier.drawBehindGradient(topColor: Color, bottomColor: Color): Modifier = this.drawWithCache {
    onDrawBehind {
        val cornerRadius = 6.dp.toPx()
        val brush = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(topColor, bottomColor),
            start = Offset(0f, 0f),
            end = Offset(0f, size.height)
        )
        drawRoundRect(
            brush = brush,
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
        )
    }
}
