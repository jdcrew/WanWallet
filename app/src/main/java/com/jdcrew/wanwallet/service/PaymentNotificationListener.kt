package com.jdcrew.wanwallet.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.jdcrew.wanwallet.data.model.PaymentChannel
import com.jdcrew.wanwallet.data.model.Transaction
import com.jdcrew.wanwallet.data.model.TransactionType
import com.jdcrew.wanwallet.data.repository.TransactionCaptureHandler
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * 支付通知监听服务
 * 
 * 监听微信、支付宝、云闪付的支付通知，自动提取交易信息
 */
@AndroidEntryPoint
class PaymentNotificationListener : NotificationListenerService() {
    
    @Inject
    lateinit var captureHandler: TransactionCaptureHandler
    
    companion object {
        private const val TAG = "PaymentNotification"
        
        // 支付应用包名
        private val WECHAT_PACKAGES = setOf("com.tencent.mm")
        private val ALIPAY_PACKAGES = setOf("com.eg.android.AlipayGphone")
        private val UNIONPAY_PACKAGES = setOf("com.unionpay")
        
        // 金额正则表达式 (匹配 ¥100.00, 100 元，$100.00 等)
        private val AMOUNT_PATTERNS = listOf(
            Pattern.compile("[¥￥\\$]([\\d,]+\\.?\\d*)"),
            Pattern.compile("([\\d,]+\\.?\\d*)\\s*元"),
            Pattern.compile("金额 [：:]\\s*([\\d,]+\\.?\\d*)"),
            Pattern.compile("([\\d,]+\\.?\\d*)")
        )
    }
    
    private val transactionCallback: ((Transaction) -> Unit)? = null
    
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val packageName = sbn.packageName
            val notification = sbn.notification
            val extras = notification.extras
            
            val title = extras.getCharSequence("android.title")?.toString() ?: ""
            val text = extras.getCharSequence("android.text")?.toString() ?: ""
            
            Log.d(TAG, "Notification from: $packageName")
            Log.d(TAG, "Title: $title")
            Log.d(TAG, "Text: $text")
            
            // 判断支付渠道
            val channel = when {
                WECHAT_PACKAGES.contains(packageName) -> PaymentChannel.WECHAT
                ALIPAY_PACKAGES.contains(packageName) -> PaymentChannel.ALIPAY
                UNIONPAY_PACKAGES.contains(packageName) -> PaymentChannel.UNIONPAY
                else -> return // 不是支付应用
            }
            
            // 提取金额
            val amount = extractAmount(text) ?: extractAmount(title) ?: return
            
            // 判断收支类型 (支付通知默认是支出)
            val type = TransactionType.EXPENSE
            
            // 提取商户名称
            val merchant = extractMerchant(title, text, channel)
            
            Log.d(TAG, "Parsed: amount=$amount, merchant=$merchant, channel=$channel")
            
            // 创建交易记录
            val transaction = Transaction(
                amount = -amount, // 支出为负数
                type = type,
                categoryId = 0, // 待分类
                merchant = merchant,
                time = System.currentTimeMillis(),
                channel = channel,
                note = "",
                isAuto = true
            )
            
            // 保存到数据库
            captureHandler.saveTransaction(transaction)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification", e)
        }
    }
    
    /**
     * 从文本中提取金额
     */
    private fun extractAmount(text: String): Double? {
        for (pattern in AMOUNT_PATTERNS) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val amountStr = matcher.group(1)?.replace(",", "") ?: continue
                try {
                    return DecimalFormat().parse(amountStr)?.toDouble()
                } catch (e: Exception) {
                    continue
                }
            }
        }
        return null
    }
    
    /**
     * 提取商户名称
     */
    private fun extractMerchant(title: String, text: String, channel: PaymentChannel): String {
        return when (channel) {
            PaymentChannel.WECHAT -> {
                // 微信格式："微信支付 - 星巴克" 或 "你已成功支付 ¥100.00 给星巴克"
                val wechatPattern = Pattern.compile("支付 [¥￥\\$]?[\\d,]+\\.?\\d*\\s*(?:给 | 到)?(.+?)$")
                val matcher = wechatPattern.matcher(text)
                if (matcher.find()) {
                    matcher.group(1)?.trim() ?: "未知商户"
                } else {
                    "微信支付"
                }
            }
            PaymentChannel.ALIPAY -> {
                // 支付宝格式："成功支付 ¥100.00" 或 "向星巴克付款 ¥100.00"
                if (text.contains("向") && text.contains("付款")) {
                    val alipayPattern = Pattern.compile("向 (.+?) 付款")
                    val matcher = alipayPattern.matcher(text)
                    if (matcher.find()) {
                        matcher.group(1)?.trim() ?: "未知商户"
                    } else {
                        "支付宝支付"
                    }
                } else {
                    "支付宝支付"
                }
            }
            PaymentChannel.UNIONPAY -> {
                // 云闪付格式
                "云闪付支付"
            }
            else -> "未知商户"
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // 通知移除时的处理（通常不需要）
    }
}
