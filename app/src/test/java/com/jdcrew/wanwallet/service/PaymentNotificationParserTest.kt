package com.jdcrew.wanwallet.service

import org.junit.Assert.*
import org.junit.Test

class PaymentNotificationParserTest {
    
    @Test
    fun wechatNotification_parseAmount() {
        val text = "你已使用零钱支付，支出¥35.00"
        val amount = extractAmount(text)
        assertEquals(35.0, amount, 0.01)
    }
    
    @Test
    fun alipayNotification_parseAmount() {
        val text = "成功支付¥100.00"
        val amount = extractAmount(text)
        assertEquals(100.0, amount, 0.01)
    }
    
    @Test
    fun amountWithYuan_parseAmount() {
        val text = "支付 100.50 元"
        val amount = extractAmount(text)
        assertEquals(100.5, amount, 0.01)
    }
    
    @Test
    fun amountWithComma_parseAmount() {
        val text = "支付¥1,234.56"
        val amount = extractAmount(text)
        assertEquals(1234.56, amount, 0.01)
    }
    
    private fun extractAmount(text: String): Double {
        val patterns = listOf(
            "[¥￥\\$]([\\d,]+\\.?\\d*)".toRegex(),
            "([\\d,]+\\.?\\d*)\\s*元".toRegex()
        )
        
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].replace(",", "").toDouble()
            }
        }
        
        return 0.0
    }
}
