package ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Компонент для загрузки Excel файлов со студентами
 * Позволяет выбрать один или несколько Excel файлов
 * и загрузить их в группы студентов
 */
@Composable
fun StudentsFileLoader(
    viewModel: NornViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        // Информационное сообщение
        Text(
            "ℹ️ Студенты загружаются непосредственно в группы",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )

        // Кнопка выбора файлов
        Button(
            onClick = {
                loadExcelFiles(viewModel)
            },
            colors = ButtonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text("📁 Загрузить Excel файл(ы) в группы")
        }
    }
}

/**
 * Функция для открытия диалога выбора файлов и загрузки Excel файлов
 */
private fun loadExcelFiles(viewModel: NornViewModel) {
    val fileChooser = JFileChooser().apply {
        currentDirectory = viewModel.getStartDirectory()
        dialogTitle = "Выберите Excel файл со студентами"
        addChoosableFileFilter(
            FileNameExtensionFilter(
                "Excel файлы (*.xlsx, *.xls)",
                "xlsx",
                "xls"
            )
        )
        fileFilter = FileNameExtensionFilter("Excel файлы", "xlsx", "xls")
        isAcceptAllFileFilterUsed = false
        isMultiSelectionEnabled = true // Разрешаем множественный выбор
    }

    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        val selectedFiles = fileChooser.selectedFiles
        selectedFiles.forEach { file ->
            if (file.exists()) {
                viewModel.loadStudentsFromExcel(file)
            }
        }
    }
}