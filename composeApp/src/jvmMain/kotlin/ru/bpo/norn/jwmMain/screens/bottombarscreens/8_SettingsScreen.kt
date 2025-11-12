package ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import java.awt.Desktop
import java.io.File
import java.net.URI
import javax.swing.JFileChooser


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
        Text(
            "Настройки приложения",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall
        )

        // Секция управления базовой директорией
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "📁 Базовая директория",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    "Установите папку, откуда будут начинаться поиски файлов во всех экранах",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Отображение текущей директории
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Текущая базовая директория:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            baseDirectory?.absolutePath
                                ?: "Не установлена (используется домашняя папка)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val fileChooser = JFileChooser().apply {
                                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                                dialogTitle = "Выберите базовую директорию"
                                currentDirectory =
                                    baseDirectory ?: File(System.getProperty("user.home"))
                            }

                            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                viewModel.selectBaseDirectory(fileChooser.selectedFile)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📂 Выбрать папку")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.selectBaseDirectory(null)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Text("🗑️ Сбросить")
                    }
                }
            }
        }

        // Секция управления темой
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "🎨 Внешний вид",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = { viewModel.switchTheme() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("Сменить тему")
                }
            }
        }

        // Секция информации о проекте
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "📚 Информация о проекте",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    "1) Весь код + примеры входных файлов находятся на GitHub:",
                    style = MaterialTheme.typography.bodyMedium
                )

                // GitHub ссылка
                Text(
                    text = "https://github.com/MasonX0/Norn",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1976D2),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        try {
                            Desktop.getDesktop().browse(URI("https://github.com/MasonX0/Norn"))
                        } catch (e: Exception) {
                            println("❌ Ошибка открытия ссылки: ${e.message}")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Контакты:",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Telegram ссылка
                Text(
                    text = "Telegram: @masonrb",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF0088CC),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        try {
                            Desktop.getDesktop().browse(URI("https://t.me/masonrb"))
                        } catch (e: Exception) {
                            println("❌ Ошибка открытия ссылки: ${e.message}")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Дополнительная информация
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "💡 Полезная информация:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "• В репозитории находятся примеры входных файлов Excel",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "• Документация по форматам данных",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "• Инструкция по настройке и использованию",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "• Исходный код всех модулей приложения",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Секция информации о приложении
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "ℹ️ О приложении",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    "Norn - система управления практикой студентов",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Версия: 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    "Технологии: Kotlin, Compose Multiplatform, Apache POI",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
