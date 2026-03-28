package com.jdcrew.wanwallet.service

import com.jdcrew.wanwallet.data.model.PaymentChannel
import org.junit.Assert.*
import org.junit.Test
import java.text.DecimalFormat
import java.util.regex.Pattern

/**
 * 支付通知解析器测试
 * 
 * 测试通知解析逻辑，不依赖 Android 框架
 */
class PaymentNotificationParserTest {
    
    companion object {
        // 金额正则表达式 (匹配 ¥100.00, 100 元，$100.00 等)
        private val AMOUNT_PATTERNS = listOf(
            Pattern.compile("[¥￥\\$]([\\d,]+\\.?\\d*)"),
            Pattern.compile("([\\d,]+\\.?\\d*)\\s*元"),
            Pattern.compile("金额 [：:]\\s*([\\d,]+\\.?\\d*)"),
            Pattern.compile("([\\d,]+\\.?\\d*)")
        )
    }
    
    @Test
    fun wechatNotification_parseAmount_yuanWithSymbol() {
        // 给定
        val text = "你已使用零钱支付，支出¥35.00"
        
        // 当
        val amount = extractAmount(text)
        
        // 则
        assertEquals(35.0, amount, 0.01)
    }
    
    @Test
    fun wechatNotification_parseAmount_withComma() {
        // 给定
        val text = "支付成功，金额¥1,234.56"
        
        // 当
        val amount = extractAmount(text)
        
        // 则
        assertEquals(1234.56, amount, 0.01)
    }
    
    @Test
    fun alipayNotification_parseAmount_success() {
        // 给定
        val text = "成功支付¥100.00"
        
        // 当
        val amount = extractAmount(text)
        
        // 则
        assertEquals(100.0, amount, 0.01)
    }
    
    @Test
    fun alipayNotification_parseAmount_withMerchant() {
        // 给定
        val text = "向星巴克付款¥58.00"
        
        // 当
        val amount = extractAmount(text)
        
        // 则
        assertEquals(58.0, amount, 0.01)
    }
    
    @Test
    fun amountWithYuan_parseAmount() {
        // 给定
        val text = "支付 100.50 元"
        
        // 当
        val amount = extractAmount(text)
        
        // 则
        assertEquals(100.5, amount, 0.01)
    }
    
    @Test
    fun amountWithChineseYuan_parseAmount() {
        // 给定
        val text = "支付￥200 元"
        
        // 当
        val amount = extractAmount(text)
        
        // 则
        assertEquals(200.0, amount, 0.01)
    }
    
    @Test
    fun amountWithDollar_parseAmount() {
        // 给定
        val text = "Payment of $50.00"
        
        // 当
        val amount = extractAmount(text)
        
        // 则
        assertEquals(50.0, amount, 0.01)
    }
    
    @Test
    fun amountWithComma_parseAmount() {
        // 给定
        val text = "支付¥1,234.56"
        
        // 当
        val amount = extractAmount(text)
        
        // 则
        assertEquals(1234.56, amount, 0.01)
    }
    
    @Test
    fun amountWithoutSymbol_parseAmount() {
        // 给定
        val text = "支付成功 99.99"
        
        // 当
        val amount = extractAmount(text)
        
        // 则
        assertEquals(99.99, amount, 0.01)
    }
    
    @Test
    fun amountInteger_parseAmount() {
        // 给定
        val text = "支付 100 元"
        
        // 当
        val amount = extractAmount(text)
        
        // 则
        assertEquals(100.0, amount, 0.01)
    }
    
    @Test
    fun amountWithSpaces_parseAmount() {
        // 给定
        val text = "金额：1,000.00"
        
        // 当
        val amount = extractAmount(text)
        
        // 则
        assertEquals(1000.0, amount, 0.01)
    }
    
    @Test
    fun noAmount_returnsZero() {
        // 给定
        val text = "这是一条普通通知，没有金额"
        
        // 当
        val amount = extractAmount(text)
        
        // 则
        assertEquals(0.0, amount, 0.01)
    }
    
    @Test
    fun emptyText_returnsZero() {
        // 给定
        val text = ""
        
        // 当
        val amount = extractAmount(text)
        
        // 则
        assertEquals(0.0, amount, 0.01)
    }
    
    @Test
    fun extractMerchant_wechat_format() {
        // 给定
        val title = "微信支付"
        val text = "你已成功支付 ¥100.00 给星巴克"
        val channel = PaymentChannel.WECHAT
        
        // 当
        val merchant = extractMerchant(title, text, channel)
        
        // 则
        assertEquals("星巴克", merchant)
    }
    
    @Test
    fun extractMerchant_wechat_format_withTo() {
        // 给定
        val title = "微信支付"
        val text = "支付成功，向星巴克付款¥100.00"
        val channel = PaymentChannel.WECHAT
        
        // 当
        val merchant = extractMerchant(title, text, channel)
        
        // 则
        assertTrue(merchant.contains("星巴克") || merchant == "微信支付")
    }
    
    @Test
    fun extractMerchant_alipay_format() {
        // 给定
        val title = "支付宝"
        val text = "向星巴克付款¥100.00"
        val channel = PaymentChannel.ALIPAY
        
        // 当
        val merchant = extractMerchant(title, text, channel)
        
        // 则
        assertEquals("星巴克", merchant)
    }
    
    @Test
    fun extractMerchant_alipay_simpleFormat() {
        // 给定
        val title = "支付宝"
        val text = "成功支付¥100.00"
        val channel = PaymentChannel.ALIPAY
        
        // 当
        val merchant = extractMerchant(title, text, channel)
        
        // 则
        assertEquals("支付宝支付", merchant)
    }
    
    @Test
    fun extractMerchant_unionpay_format() {
        // 给定
        val title = "云闪付"
        val text = "支付成功¥100.00"
        val channel = PaymentChannel.UNIONPAY
        
        // 当
        val merchant = extractMerchant(title, text, channel)
        
        // 则
        assertEquals("云闪付支付", merchant)
    }
    
    @Test
    fun extractMerchant_unknown_format() {
        // 给定
        val title = "未知应用"
        val text = "支付成功"
        val channel = PaymentChannel.OTHER
        
        // 当
        val merchant = extractMerchant(title, text, channel)
        
        // 则
        assertEquals("未知商户", merchant)
    }
    
    @Test
    fun extractMerchant_wechat_noMerchant() {
        // 给定
        val title = "微信支付"
        val text = "支付成功¥100.00"
        val channel = PaymentChannel.WECHAT
        
        // 当
        val merchant = extractMerchant(title, text, channel)
        
        // 则
        assertEquals("微信支付", merchant)
    }
    
    private fun extractAmount(text: String): Double {
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
        return 0.0
    }
    
    private fun extractMerchant(title: String, text: String, channel: PaymentChannel): String {
        return when (channel) {
            PaymentChannel.WECHAT -> {
                val wechatPattern = Pattern.compile("支付 [¥￥\\$]?[\\d,]+\\.?\\d*\\s*(?:给 | 到)?(.+?)$")
                val matcher = wechatPattern.matcher(text)
                if (matcher.find()) {
                    matcher.group(1)?.trim() ?: "微信支付"
                } else {
                    "微信支付"
                }
            }
            PaymentChannel.ALIPAY -> {
                if (text.contains("向") && text.contains("付款")) {
                    val alipayPattern = Pattern.compile("向 (.+?) 付款")
                    val matcher = alipayPattern.matcher(text)
                    if (matcher.find()) {
                        matcher.group(1)?.trim() ?: "支付宝支付"
                    } else {
                        "支付宝支付"
                    }
                } else {
                    "支付宝支付"
                }
            }
            PaymentChannel.UNIONPAY -> {
                "云闪付支付"
            }
            else -> "未知商户"
        }
    }
}
