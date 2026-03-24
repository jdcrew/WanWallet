package com.jdcrew.wanwallet.data.repository

import org.junit.Assert.*
import org.junit.Test

class MerchantProcessorTest {
    
    private val processor = MerchantProcessor()
    
    @Test
    fun cleanMerchant_removeSuffix() {
        assertEquals("星巴克", processor.cleanMerchant("星巴克店"))
        assertEquals("肯德基", processor.cleanMerchant("肯德基店铺"))
        assertEquals("沃尔玛", processor.cleanMerchant("沃尔玛超市"))
    }
    
    @Test
    fun normalizeMerchant_standardize() {
        assertEquals("星巴克", processor.normalizeMerchant("Starbucks"))
        assertEquals("肯德基", processor.normalizeMerchant("KFC"))
        assertEquals("麦当劳", processor.normalizeMerchant("McDonald"))
    }
    
    @Test
    fun normalizeMerchant_keepOriginal() {
        assertEquals("海底捞", processor.normalizeMerchant("海底捞"))
        assertEquals("小商户", processor.normalizeMerchant("小商户"))
    }
    
    @Test
    fun extractCoreName_withLocation() {
        assertEquals("星巴克", processor.extractCoreName("星巴克北京路店"))
        assertEquals("肯德基", processor.extractCoreName("肯德基上海广场店"))
    }
    
    @Test
    fun processMerchants_batch() {
        val merchants = listOf("星巴克店", "KFC", "沃尔玛超市")
        val result = processor.processMerchants(merchants)
        
        assertEquals("星巴克", result["星巴克店"])
        assertEquals("肯德基", result["KFC"])
        assertEquals("沃尔玛", result["沃尔玛超市"])
    }
}
