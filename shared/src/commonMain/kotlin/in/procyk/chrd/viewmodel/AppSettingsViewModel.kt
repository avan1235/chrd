package `in`.procyk.chrd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.procyk.chrd.db.AppSettings
import `in`.procyk.chrd.db.AppSettingsRepository
import `in`.procyk.chrd.db.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppSettingsViewModel(
    private val repository: AppSettingsRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> =
        repository.settings.map { it.themeMode }
            .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings.DEFAULT.themeMode)

    val useLiquidNavigation: StateFlow<Boolean> =
        repository.settings.map { it.useLiquidNavigation }
            .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings.DEFAULT.useLiquidNavigation)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setUseLiquidNavigation(use: Boolean) {
        viewModelScope.launch { repository.setUseLiquidNavigation(use) }
    }
}