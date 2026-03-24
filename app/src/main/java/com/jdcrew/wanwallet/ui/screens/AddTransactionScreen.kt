package com.jdcrew.wanwallet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jdcrew.wanwallet.data.model.Category
import com.jdcrew.wanwallet.data.model.PaymentChannel
import com.jdcrew.wanwallet.data.model.TransactionType

@Composable
fun AddTransactionScreen(
    onBackClick: () -> Unit,
    onSaveClick: (AddTransactionState) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var merchant by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedChannel by remember { mutableStateOf(PaymentChannel.WECHAT) }
    
    // 预设分类
    val expenseCategories = remember { 
        listOf("餐饮", "交通", "购物", "娱乐", "住房", "医疗", "教育", "其他") 
    }
    val incomeCategories = remember { 
        listOf("工资", "理财", "兼职", "其他") 
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 顶部栏
        TopAppBar(
            title = { Text("添加交易") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        onSaveClick(
                            AddTransactionState(
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                type = type,
                                categoryName = selectedCategory?.name ?: "未分类",
                                merchant = merchant,
                                note = note,
                                channel = selectedChannel
                            )
                        )
                    },
                    enabled = amount.isNotEmpty() && merchant.isNotEmpty()
                ) {
                    Icon(Icons.Default.Check, contentDescription = "保存")
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 金额输入
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(text = "金额", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0.00") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            
            // 类型选择
            Card {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = type == TransactionType.EXPENSE,
                        onClick = { type = TransactionType.EXPENSE },
                        label = { Text("支出") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == TransactionType.INCOME,
                        onClick = { type = TransactionType.INCOME },
                        label = { Text("收入") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // 分类选择
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = "分类", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categories = if (type == TransactionType.EXPENSE) expenseCategories else incomeCategories
                        categories.forEach { category ->
                            AssistChip(
                                onClick = { selectedCategory = Category(name = category, type = type, icon = "") },
                                label = { Text(category) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (selectedCategory?.name == category) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                )
                            )
                        }
                    }
                }
            }
            
            // 商户名称
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = "商户名称", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = merchant,
                        onValueChange = { merchant = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("例如：星巴克、沃尔玛") },
                        singleLine = true
                    )
                }
            }
            
            // 支付渠道
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = "支付渠道", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentChannel.values().forEach { channel ->
                            AssistChip(
                                onClick = { selectedChannel = channel },
                                label = { 
                                    Text(
                                        when (channel) {
                                            PaymentChannel.WECHAT -> "微信"
                                            PaymentChannel.ALIPAY -> "支付宝"
                                            PaymentChannel.UNIONPAY -> "云闪付"
                                            PaymentChannel.CASH -> "现金"
                                            PaymentChannel.CARD -> "银行卡"
                                            else -> "其他"
                                        }
                                    ) 
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (selectedChannel == channel) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                )
                            )
                        }
                    }
                }
            }
            
            // 备注
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = "备注 (可选)", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("添加备注") },
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }
        }
    }
}

data class AddTransactionState(
    val amount: Double,
    val type: TransactionType,
    val categoryName: String,
    val merchant: String,
    val note: String,
    val channel: PaymentChannel
)
