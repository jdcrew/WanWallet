package com.jdcrew.wanwallet.data.model

import org.junit.Assert.*
import org.junit.Test

class TransactionTest {
    
    @Test
    fun transactionCreation_defaultValues() {
        val transaction = Transaction(
            amount = 100.0,
            type = TransactionType.EXPENSE,
            categoryId = 1,
            merchant = "Test Merchant",
            time = System.currentTimeMillis(),
            channel = PaymentChannel.WECHAT
        )
        
        assertEquals(100.0, transaction.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertEquals(1, transaction.categoryId)
        assertEquals("Test Merchant", transaction.merchant)
        assertEquals(PaymentChannel.WECHAT, transaction.channel)
        assertTrue(transaction.isAuto)
        assertEquals("", transaction.note)
    }
    
    @Test
    fun transactionWithNote() {
        val transaction = Transaction(
            amount = 50.0,
            type = TransactionType.INCOME,
            categoryId = 2,
            merchant = "Salary",
            time = System.currentTimeMillis(),
            channel = PaymentChannel.CARD,
            note = "Monthly salary"
        )
        
        assertEquals("Monthly salary", transaction.note)
        assertEquals(TransactionType.INCOME, transaction.type)
        assertEquals(PaymentChannel.CARD, transaction.channel)
    }
    
    @Test
    fun transactionType_values() {
        assertEquals(2, TransactionType.values().size)
        assertTrue(TransactionType.values().contains(TransactionType.INCOME))
        assertTrue(TransactionType.values().contains(TransactionType.EXPENSE))
    }
    
    @Test
    fun paymentChannel_values() {
        val channels = PaymentChannel.values()
        assertTrue(channels.size >= 5)
        assertTrue(channels.contains(PaymentChannel.WECHAT))
        assertTrue(channels.contains(PaymentChannel.ALIPAY))
        assertTrue(channels.contains(PaymentChannel.CASH))
    }
}
