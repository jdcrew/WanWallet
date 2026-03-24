package com.jdcrew.wanwallet.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jdcrew.wanwallet.data.model.Transaction
import com.jdcrew.wanwallet.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionList(
    transactions: List<Transaction>,
    onTransactionClick: (Transaction) -> Unit
) {
    if (transactions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "📭", style = MaterialTheme.typography.displayLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "暂无交易记录", color = Color.Gray)
                Text(text = "点击右上角添加第一笔交易", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 按日期分组显示
            val groupedByDate = transactions.groupBy { 
                SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date(it.time))
            }
            
            groupedByDate.forEach { (date, dayTransactions) ->
                item {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(dayTransactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction) }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 分类图标
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "💰", style = MaterialTheme.typography.titleLarge)
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // 商户和备注
                Column {
                    Text(
                        text = transaction.merchant,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (transaction.note.isNotEmpty()) {
                        Text(
                            text = transaction.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 金额
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (transaction.type == TransactionType.EXPENSE) {
                        "-¥${String.format("%.2f", kotlin.math.abs(transaction.amount))}"
                    } else {
                        "+¥${String.format("%.2f", transaction.amount)}"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type == TransactionType.EXPENSE) {
                        MaterialTheme.colorScheme.error
                    } else {
                        Color(0xFF4CAF50)
                    }
                )
                
                // 支付渠道
                Text(
                    text = when (transaction.channel) {
                        com.jdcrew.wanwallet.data.model.PaymentChannel.WECHAT -> "微信"
                        com.jdcrew.wanwallet.data.model.PaymentChannel.ALIPAY -> "支付宝"
                        com.jdcrew.wanwallet.data.model.PaymentChannel.UNIONPAY -> "云闪付"
                        com.jdcrew.wanwallet.data.model.PaymentChannel.CASH -> "现金"
                        com.jdcrew.wanwallet.data.model.PaymentChannel.CARD -> "银行卡"
                        else -> "其他"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
