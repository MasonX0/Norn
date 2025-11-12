package ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel


@Composable
fun SettingsScreen(viewModel:NornViewModel) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Column (verticalArrangement = Arrangement.spacedBy(15.dp)) {
            Text(
            "Экран настроек",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge
        )
            Button(
                onClick = {viewModel.switchTheme() }, content = { Text("Сменить тему") }, colors = ButtonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            )
        }

    }


}
