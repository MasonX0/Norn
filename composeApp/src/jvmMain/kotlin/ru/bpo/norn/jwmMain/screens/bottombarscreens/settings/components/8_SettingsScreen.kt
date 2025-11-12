package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import ui.components.BaseDirectorySection
import ui.components.ThemeSection
import ui.components.ProjectInfoSection
import ui.components.AppInfoSection

/**
 * Основной экран настроек приложения
 * Позволяет управлять базовой директорией, темой,
 * а также содержит информацию о проекте и приложении
 */
@Composable
fun SettingsScreen(viewModel: NornViewModel) {
    val baseDirectory by viewModel.baseDirectory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        // Заголовок экрана
        Text(
            "Настройки приложения",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall
        )

        // Секция управления базовой директорией
        BaseDirectorySection(
            viewModel = viewModel,
            baseDirectory = baseDirectory
        )

        // Секция управления темой
        ThemeSection(viewModel = viewModel)

        // Секция информации о проекте
        ProjectInfoSection()

        // Секция информации о приложении
        AppInfoSection()
    }
}
