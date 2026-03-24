package com.jdcrew.wanwallet.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 环形图组件
 * 
 * 用于展示分类占比
 */
@Composable
fun PieChart(
    data: List<PieChartSlice>,
    modifier: Modifier = Modifier,
    size: Int = 200
) {
    var total = 0.0
    data.forEach { total += it.value }
    
    if (total == 0.0 || data.isEmpty()) {
        Box(
            modifier = modifier.size(size.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("无数据", color = Color.Gray)
        }
        return
    }
    
    var startAngle = 0f
    
    Canvas(modifier = modifier.size(size.dp)) {
        val canvasSize = size.dp.toPx()
        val radius = canvasSize / 2
        
        data.forEach { slice ->
            val sweepAngle = (slice.value / total * 360).toFloat()
            
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(canvasSize, canvasSize)
            )
            
            startAngle += sweepAngle
        }
        
        // 绘制中心空白 (环形图)
        drawCircle(
            color = MaterialTheme.colorScheme.background,
            radius = radius * 0.6f
        )
    }
}

data class PieChartSlice(
    val label: String,
    val value: Double,
    val color: Color
)

/**
 * 图例组件
 */
@Composable
fun ChartLegend(
    slices: List<PieChartSlice>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        slices.forEach { slice ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp),
                            color = slice.color
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = slice.label,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = "¥${String.format("%.2f", slice.value)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
