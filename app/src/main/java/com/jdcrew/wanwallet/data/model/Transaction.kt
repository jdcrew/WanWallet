package com.jdcrew.wanwallet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 交易记录实体
 * 
 * @property id 交易 ID (自动生成)
 * @property amount 金额 (正数表示收入，负数表示支出)
 * @property type 交易类型 (INCOME/EXPENSE)
 * @property category 分类 ID
 * @property merchant 商户名称
 * @property time 交易时间
 * @property channel 支付渠道 (WECHAT/ALIPAY/UNIONPAY/CASH/CARD)
 * @property note 备注
 * @property isAuto 是否自动记录
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val amount: Double,
    
    val type: TransactionType,
    
    val categoryId: Long,
    
    val merchant: String,
    
    val time: Long, // Unix timestamp
    
    val channel: PaymentChannel,
    
    val note: String = "",
    
    val isAuto: Boolean = true,
    
    val createdAt: Long = System.currentTimeMillis(),
    
    val updatedAt: Long = System.currentTimeMillis()
)

enum class TransactionType {
    INCOME, // 收入
    EXPENSE // 支出
}

enum class PaymentChannel {
    WECHAT,     // 微信支付
    ALIPAY,     // 支付宝
    UNIONPAY,   // 云闪付
    CASH,       // 现金
    CARD,       // 银行卡
    OTHER       // 其他
}
