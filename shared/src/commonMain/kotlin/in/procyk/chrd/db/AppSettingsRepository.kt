package `in`.procyk.chrd.db

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

    suspend fun current(): AppSettings = settings.first()

    private suspend fun currentEntity(): AppSettingsEntity {
        val current = settings.first()
        return AppSettingsEntity(
            themeMode = current.themeMode.name,
        )
    }
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

private fun AppSettingsEntity.toModel(): AppSettings =
    AppSettings(
        themeMode = runCatching { ThemeMode.valueOf(themeMode) }.getOrDefault(ThemeMode.SYSTEM),
    )
