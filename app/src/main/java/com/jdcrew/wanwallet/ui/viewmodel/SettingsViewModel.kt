package com.jdcrew.wanwallet.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jdcrew.wanwallet.data.repository.TransactionCaptureHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isNotificationListenerEnabled: Boolean = false,
    val isAccessibilityServiceEnabled: Boolean = false,
    val autoCaptureEnabled: Boolean = true,
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val captureHandler: TransactionCaptureHandler
) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        checkPermissions()
    }
    
    fun checkPermissions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val notificationEnabled = isNotificationListenerEnabled()
            val accessibilityEnabled = isAccessibilityServiceEnabled()
            
            _uiState.value = SettingsUiState(
                isNotificationListenerEnabled = notificationEnabled,
                isAccessibilityServiceEnabled = accessibilityEnabled,
                autoCaptureEnabled = notificationEnabled || accessibilityEnabled,
                isLoading = false
            )
        }
    }
    
    private fun isNotificationListenerEnabled(): Boolean {
        val context = getApplication<Application>()
        val packageName = context.packageName
        val flat = android.provider.Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        
        return flat.split(":").any { it == packageName }
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        val context = getApplication<Application>()
        val serviceName = "${context.packageName}/.service.PaymentAccessibilityService"
        val flat = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        
        return flat.split(":").any { it == serviceName }
    }
}
