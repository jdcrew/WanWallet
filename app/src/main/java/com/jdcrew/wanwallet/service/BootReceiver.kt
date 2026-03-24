package com.jdcrew.wanwallet.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 开机自启动广播接收器
 * 
 * 设备重启后自动启动通知监听服务
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, starting notification listener service")
            
            // 启动前台服务
            val serviceIntent = Intent(context, NotificationListenerForegroundService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
