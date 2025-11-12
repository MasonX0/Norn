package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import ui.components.StatementsPDFLoader
import ui.components.DocumentGenerator
import ui.common.FileStatusCard

/**
 * Основной экран управления заявлениями
 * Позволяет загружать PDF файлы с заявлениями и генерировать документы
 * В настоящее время находится в разработке
 */
@Composable
fun StatementsScreenNew(viewModel: NornViewModel) {
    val statementsFile by viewModel.statementsFile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        // Компонент загрузки PDF файлов
        StatementsPDFLoader(viewModel = viewModel)

        // Компонент генерации документов
        DocumentGenerator(
            viewModel = viewModel,
            statementsFile = statementsFile
        )

        // Информация о состоянии приложения
        FileStatusCard(
            file = statementsFile,
            fileTypeName = "PDF документ"
        )
    }
}