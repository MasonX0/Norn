package ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Общие компоненты для кнопок действий
 * Обеспечивают единообразный стиль кнопок по всему приложению
 */

/**
 * Основная кнопка с иконкой
 */
@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Text(text)
    }
}

/**
 * Кнопка удаления/очистки (красная)
 */
@Composable
fun DestructiveActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        ),
        modifier = modifier
    ) {
        Text(text, color = Color.White)
    }
}

/**
 * Второстепенная кнопка (outlined)
 */
@Composable
fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    borderColor: Color = MaterialTheme.colorScheme.tertiary
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(width = 1.dp, color = borderColor),
        modifier = modifier
    ) {
        Text(text)
    }
}

/**
 * Группа кнопок действий в горизонтальном ряду
 */
@Composable
fun ActionButtonsRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        content = content
    )
}