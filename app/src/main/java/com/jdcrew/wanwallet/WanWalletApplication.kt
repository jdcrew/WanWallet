package com.jdcrew.wanwallet

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WanWalletApplication : Application() {
    // Application 初始化
    // Hilt 会自动生成所需的依赖注入组件
}
