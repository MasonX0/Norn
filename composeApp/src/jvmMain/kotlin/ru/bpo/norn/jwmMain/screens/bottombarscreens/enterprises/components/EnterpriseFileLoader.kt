package ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Компонент для загрузки файла с предприятиями из TXT формата
 * Показывает информацию о новом формате файла и позволяет выбрать файл
 */
@Composable
fun EnterpriseFileLoader(
    viewModel: NornViewModel,
    enterprisesFile: java.io.File?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        // Информация о формате файла
        FileFormatInfo()

        // Кнопки управления файлом
        FileActionButtons(
            viewModel = viewModel,
            enterprisesFile = enterprisesFile
        )
    }
}

/**
 * Карточка с информацией о формате TXT файла
 */
@Composable
private fun FileFormatInfo() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "📋 Новый формат файла:",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "ООО Газпром Межрегионгаз Уфа, г. Уфа // ст. преподаватель ! М.А. Салихова, доц. ! А.И.Сидоров",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "• Предприятие и город разделены запятой\n• Руководители отделены '//' от предприятия\n• Должность и ФИО разделены '!'",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Кнопки для загрузки и очистки файла предприятий
 */
@Composable
private fun FileActionButtons(
    viewModel: NornViewModel,
    enterprisesFile: java.io.File?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        // Кнопка выбора файла
        Button(
            onClick = {
                loadEnterprisesFile(viewModel)
            },
            colors = ButtonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.weight(1f)
        ) {
            Text("📁 Выбрать TXT документ\n(список предприятий)")
        }

        // Кнопка очистки (показывается только если есть файл)
        if (enterprisesFile != null) {
            Button(
                onClick = {
                    viewModel.clearEnterprisesList()
                    viewModel.selectEnterprisesFile(null)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("🗑️ Очистить список")
            }
        }
    }
}

/**
 * Функция для загрузки файла предприятий
 */
private fun loadEnterprisesFile(viewModel: NornViewModel) {
    val fileChooser = JFileChooser().apply {
        currentDirectory = viewModel.getStartDirectory()
        dialogTitle = "Выберите файл с данными предприятий"
        addChoosableFileFilter(FileNameExtensionFilter("TXT файлы", "txt"))
        fileFilter = FileNameExtensionFilter("TXT файлы", "txt")
        isAcceptAllFileFilterUsed = false
    }

    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        val file = fileChooser.selectedFile
        viewModel.selectEnterprisesFile(file)
        if (file.exists()) {
            viewModel.readEnterprisesFromTxt(file)
        }
    }
}