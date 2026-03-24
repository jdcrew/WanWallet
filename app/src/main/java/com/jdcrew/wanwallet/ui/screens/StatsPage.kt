package com.jdcrew.wanwallet.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jdcrew.wanwallet.ui.components.PieChart
import com.jdcrew.wanwallet.ui.components.ChartLegend
import com.jdcrew.wanwallet.ui.viewmodel.StatsViewModel
import com.jdcrew.wanwallet.ui.viewmodel.StatsPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun StatsPage() {
    val viewModel: StatsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 时间选择器
        TimePeriodSelector(
            selectedPeriod = uiState.selectedPeriod,
            onPeriodChanged = { viewModel.updatePeriod(it) }
        )
        
        // 总支出/收入概览
        SummaryCards(
            income = uiState.totalIncome,
            expense = uiState.totalExpense,
            balance = uiState.balance
        )
        
        // 分类占比图表
        CategoryChart(categoryData = uiState.categoryData)
        
        // 趋势图表
        TrendChart(trendData = uiState.dailyTrend)
        
        // 详细统计列表
        StatsDetailList()
    }
}

@Composable
fun TimePeriodSelector(
    selectedPeriod: StatsPeriod,
    onPeriodChanged: (StatsPeriod) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatsPeriod.values().forEach { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodChanged(period) },
                label = { 
                    Text(
                        when (period) {
                            StatsPeriod.WEEK -> "本周"
                            StatsPeriod.MONTH -> "本月"
                            StatsPeriod.YEAR -> "本年"
                        }
                    ) 
                }
            )
        }
    }
}

@Composable
fun SummaryCards(
    income: Double,
    expense: Double,
    balance: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 支出卡片
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "支出", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "¥${String.format("%.2f", expense)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        // 收入卡片
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "收入", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "¥${String.format("%.2f", income)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun CategoryChart(categoryData: List<PieChartSlice>) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "分类占比", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (categoryData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "暂无数据", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "开始记账后查看分析", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PieChart(data = categoryData, size = 150)
                    Spacer(modifier = Modifier.width(16.dp))
                    ChartLegend(slices = categoryData, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun TrendChart(trendData: List<DailyTrendItem>) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "支出趋势", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (trendData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "暂无趋势数据", color = Color.Gray)
                }
            } else {
                // 简化版本：显示列表
                trendData.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = item.date, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "支出¥${String.format("%.2f", item.expense)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsDetailList() {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "详细统计", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            // 统计项列表
            listOf("日均支出", "最高支出日", "主要消费类别", "交易笔数").forEach { stat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stat, style = MaterialTheme.typography.bodyMedium)
                    Text(text = "-", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
