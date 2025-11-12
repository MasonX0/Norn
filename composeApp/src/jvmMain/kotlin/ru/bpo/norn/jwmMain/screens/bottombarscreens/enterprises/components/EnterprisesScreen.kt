package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import ui.components.EnterpriseFileLoader
import ui.components.EnterprisesList
import ui.components.EnterpriseEditDialog
import ui.common.FileStatusCard
import ui.common.countLinesInFile

/**
 * Основной экран управления предприятиями
 * Позволяет загружать TXT файлы с предприятиями, просматривать и редактировать их данные
 */
@Composable
fun EnterprisesScreen(viewModel: NornViewModel) {
    val enterprisesFile by viewModel.enterprisesFile.collectAsState()
    val enterprisesList by viewModel.enterprisesList.collectAsState()
    val selectedEnterprise by viewModel.selectedEnterprise.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        // Заголовок экрана
        Text(
            "Чтение списка предприятий из TXT файла с руководителями",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium
        )

        // Компонент загрузки файла
        EnterpriseFileLoader(
            viewModel = viewModel,
            enterprisesFile = enterprisesFile
        )

        // Список предприятий
        EnterprisesList(
            enterprisesList = enterprisesList,
            selectedEnterprise = selectedEnterprise,
            viewModel = viewModel
        )

        // Информация о загруженном файле
        FileStatusCard(
            file = enterprisesFile,
            fileTypeName = "TXT документ",
            additionalInfo = enterprisesFile?.let { file ->
                "Строк в файле: ${countLinesInFile(file)}"
            }
        )
    }

    // Диалог редактирования предприятия
    EnterpriseEditDialog(
        enterprise = selectedEnterprise,
        showDialog = showEditDialog,
        onSave = { updatedEnterprise ->
            viewModel.updateEnterpriseData(updatedEnterprise)
        },
        onDismiss = { viewModel.closeEditDialog() }
    )
}