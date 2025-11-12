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
 * Компонент для загрузки PDF файлов с заявлениями
 * Позволяет выбрать PDF документ для последующей обработки
 */
@Composable
fun StatementsPDFLoader(
    viewModel: NornViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        // Информационное сообщение
        Text(
            "В РАЗРАБОТКЕ",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge
        )

        // Кнопка выбора PDF файла
        Button(
            onClick = {
                loadPDFFile(viewModel)
            },
            colors = ButtonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text("Выбрать PDF документ\n(ведомости)")
        }
    }
}

/**
 * Функция для загрузки PDF файла через диалог выбора файлов
 */
private fun loadPDFFile(viewModel: NornViewModel) {
    val fileChooser = JFileChooser().apply {
        currentDirectory = viewModel.getStartDirectory()
        dialogTitle = "Выберите PDF документ"
        addChoosableFileFilter(FileNameExtensionFilter("PDF документы (*.pdf)", "pdf"))
        fileFilter = FileNameExtensionFilter("PDF документы", "pdf")
        // Запрещаем выбор других типов файлов
        isAcceptAllFileFilterUsed = false
    }

    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        viewModel.selectStatementsFile(fileChooser.selectedFile)
    }
}