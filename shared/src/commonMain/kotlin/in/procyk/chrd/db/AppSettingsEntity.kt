package `in`.procyk.chrd.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val themeMode: String = ThemeMode.SYSTEM.name,
    val useLiquidNavigation: Boolean = `in`.procyk.chrd.useLiquidNavigation,
) {
    companion object {
        const val SINGLETON_ID: Int = 0
    }
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}
