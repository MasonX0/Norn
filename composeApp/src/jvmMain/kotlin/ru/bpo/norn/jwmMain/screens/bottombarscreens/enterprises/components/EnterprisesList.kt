package ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bpo.norn.commonMain.models.Enterprise
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel

/**
 * Компонент для отображения списка загруженных предприятий
 * Показывает информацию о каждом предприятии, его руководителях и типах
 * Позволяет выбрать предприятие для редактирования
 */
@Composable
fun EnterprisesList(
    enterprisesList: List<Enterprise>,
    selectedEnterprise: Enterprise?,
    viewModel: NornViewModel,
    modifier: Modifier = Modifier
) {
    if (enterprisesList.isEmpty()) {
        return // Список пуст, ничего не отображаем
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок списка
            Text(
                "📋 Список предприятий (${enterprisesList.size}):",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            
            Text(
                "Нажмите на карточку предприятия для редактирования",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            
            // Список предприятий в ограниченном контейнере
            LazyColumn(modifier = Modifier.height(400.dp)) {
                items(enterprisesList) { enterprise ->
                    EnterpriseCard(
                        enterprise = enterprise,
                        isSelected = selectedEnterprise?.name == enterprise.name,
                        onEnterpriseClick = { viewModel.selectEnterpriseForEditing(enterprise) }
                    )
                }
            }
        }
    }
}

/**
 * Компонент карточки одного предприятия
 */
@Composable
private fun EnterpriseCard(
    enterprise: Enterprise,
    isSelected: Boolean,
    onEnterpriseClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onEnterpriseClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.tertiary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Название и город предприятия
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    enterprise.name,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    enterprise.city ?: "❓",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enterprise.city != null) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }

            // Информация о руководителях практики
            if (enterprise.supervisors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "👨‍💼 Руководители практики:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                enterprise.supervisors.forEach { supervisor ->
                    Text(
                        "• ${supervisor.getDisplayText()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "👨‍💼 Нет руководителей",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Отображение типов предприятия
            val enterpriseTypes = getEnterpriseTypes(enterprise)
            if (enterpriseTypes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "🏢 Тип предприятия:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                enterpriseTypes.forEach { type ->
                    Text(
                        "• $type",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Вспомогательная функция для определения типов предприятия
 */
private fun getEnterpriseTypes(enterprise: Enterprise): List<String> {
    val types = mutableListOf<String>()
    if (enterprise.isForeign) types.add("🌍 Зарубежное")
    if (enterprise.isSoluniTyulyukInzer) types.add("🏔️ Солуни/Тюлюк/Инзер")
    if (enterprise.isDepartment) types.add("🎓 Кафедра")
    if (enterprise.isUniversitySubdivision) types.add("🏛️ Структурное подразделение")
    if (enterprise.isBaseDepartment) types.add("🏭 Базовая кафедра")
    return types
}