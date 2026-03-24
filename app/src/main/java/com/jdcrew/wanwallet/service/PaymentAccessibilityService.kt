package com.jdcrew.wanwallet.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.jdcrew.wanwallet.data.model.PaymentChannel
import com.jdcrew.wanwallet.data.model.Transaction
import com.jdcrew.wanwallet.data.model.TransactionType

/**
 * 支付无障碍服务
 * 
 * 监听支付成功页面，自动提取交易信息
 * 作为通知监听的补充，提高捕获准确率
 */
class PaymentAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val TAG = "PaymentAccessibility"
        
        // 支付应用包名
        private val WECHAT_PACKAGES = setOf("com.tencent.mm")
        private val ALIPAY_PACKAGES = setOf("com.eg.android.AlipayGphone")
        
        // 支付成功页面关键词
        private val PAYMENT_SUCCESS_KEYWORDS = listOf(
            "支付成功",
            "付款成功",
            "交易成功",
            "支付完成",
            "付款完成"
        )
    }
    
    private var isListening = false
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.DEFAULT or
                   AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                   AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }
        
        serviceInfo = info
        isListening = true
        
        Log.d(TAG, "AccessibilityService connected")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !isListening) return
        
        // 检查是否是支付成功页面
        if (!isPaymentSuccessPage(event)) return
        
        // 提取交易信息
        val transaction = extractTransactionInfo(event)
        if (transaction != null) {
            Log.d(TAG, "Transaction captured: $transaction")
            // TODO: 保存到数据库
        }
    }
    
    override fun onInterrupt() {
        isListening = false
        Log.d(TAG, "AccessibilityService interrupted")
    }
    
    /**
     * 判断是否是支付成功页面
     */
    private fun isPaymentSuccessPage(event: AccessibilityEvent): Boolean {
        val text = event.text?.toString() ?: return false
        
        return PAYMENT_SUCCESS_KEYWORDS.any { keyword ->
            text.contains(keyword, ignoreCase = true)
        }
    }
    
    /**
     * 从页面提取交易信息
     */
    private fun extractTransactionInfo(event: AccessibilityEvent): Transaction? {
        val rootNode = rootInActiveWindow ?: return null
        
        try {
            // 查找金额
            val amount = findAmountInNode(rootNode) ?: return null
            
            // 查找商户
            val merchant = findMerchantInNode(rootNode) ?: "未知商户"
            
            // 判断支付渠道
            val channel = when {
                WECHAT_PACKAGES.contains(event.packageName) -> PaymentChannel.WECHAT
                ALIPAY_PACKAGES.contains(event.packageName) -> PaymentChannel.ALIPAY
                else -> PaymentChannel.OTHER
            }
            
            return Transaction(
                amount = -amount,
                type = TransactionType.EXPENSE,
                categoryId = 0,
                merchant = merchant,
                time = System.currentTimeMillis(),
                channel = channel,
                note = "",
                isAuto = true
            )
        } finally {
            rootNode.recycle()
        }
    }
    
    /**
     * 在节点树中查找金额
     */
    private fun findAmountInNode(node: AccessibilityNodeInfo): Double? {
        // 尝试从当前节点查找
        val text = node.text?.toString()
        if (text != null) {
            val amount = parseAmount(text)
            if (amount != null) return amount
        }
        
        // 递归查找子节点
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            try {
                val amount = findAmountInNode(child)
                if (amount != null) return amount
            } finally {
                child.recycle()
            }
        }
        
        return null
    }
    
    /**
     * 在节点树中查找商户名称
     */
    private fun findMerchantInNode(node: AccessibilityNodeInfo): String? {
        val text = node.text?.toString()
        if (text != null && text.length in 2..50) {
            // 排除常见非商户文本
            if (!isNonMerchantText(text)) {
                return text
            }
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            try {
                val merchant = findMerchantInNode(child)
                if (merchant != null) return merchant
            } finally {
                child.recycle()
            }
        }
        
        return null
    }
    
    /**
     * 从文本中解析金额
     */
    private fun parseAmount(text: String): Double? {
        val patterns = listOf(
            "[¥￥\\$]([\\d,]+\\.?\\d*)".toRegex(),
            "([\\d,]+\\.?\\d*)\\s*元".toRegex(),
            "金额 [：:]\\s*([\\d,]+\\.?\\d*)".toRegex()
        )
        
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val amountStr = match.groupValues[1].replace(",", "")
                try {
                    return amountStr.toDouble()
                } catch (e: NumberFormatException) {
                    continue
                }
            }
        }
        
        return null
    }
    
    /**
     * 判断是否是非商户文本
     */
    private fun isNonMerchantText(text: String): Boolean {
        val nonMerchantKeywords = listOf(
            "支付成功", "付款成功", "确定", "完成", "取消",
            "元", "¥", "￥", "$", "查看", "详情"
        )
        
        return nonMerchantKeywords.any { keyword ->
            text.equals(keyword, ignoreCase = true)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isListening = false
        Log.d(TAG, "AccessibilityService destroyed")
    }
}
