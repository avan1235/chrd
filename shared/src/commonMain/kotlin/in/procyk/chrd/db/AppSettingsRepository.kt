package `in`.procyk.chrd.db

import `in`.procyk.chrd.useLiquidNavigationDefault
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AppSettingsRepository(private val dao: AppSettingsDao) {
    val settings: Flow<AppSettings> =
        dao
            .observe()
            .map { it?.toModel() ?: AppSettings() }

    suspend fun setThemeMode(mode: ThemeMode) {
        val current = currentEntity()
        dao.upsert(current.copy(themeMode = mode.name))
    }

    suspend fun setUseLiquidNavigation(use: Boolean) {
        val current = currentEntity()
        dao.upsert(current.copy(useLiquidNavigation = use))
    }

    suspend fun current(): AppSettings = settings.first()

    private suspend fun currentEntity(): AppSettingsEntity {
        val current = settings.first()
        return AppSettingsEntity(
            themeMode = current.themeMode.name,
            useLiquidNavigation = current.useLiquidNavigation,
        )
    }
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useLiquidNavigation: Boolean = useLiquidNavigationDefault,
) {
    companion object {
        val DEFAULT: AppSettings = AppSettings()
    }
}

private fun AppSettingsEntity.toModel(): AppSettings =
    AppSettings(
        themeMode = runCatching { ThemeMode.valueOf(themeMode) }.getOrDefault(ThemeMode.SYSTEM),
        useLiquidNavigation = this@toModel.useLiquidNavigation,
    )
