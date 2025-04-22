package com.javhualde.nospoilerapk.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.javhualde.nospoilerapk.R
import kotlinx.coroutines.delay

@Composable
fun LoadingDialog(
    isTimeline: Boolean = false
) {
    var currentMessageIndex by remember { mutableStateOf(0) }
    
    // Mensajes para resumen
    val summaryMessages = listOf(
        R.string.generating_summary,
        R.string.analyzing_episodes,
        R.string.identifying_key_moments,
        R.string.writing_story,
        R.string.avoiding_spoilers,
        R.string.almost_ready
    )
    
    // Mensajes para timeline
    val timelineMessages = listOf(
        R.string.generating_timeline,
        R.string.organizing_events,
        R.string.creating_sections,
        R.string.connecting_key_points,
        R.string.final_touches,
        R.string.almost_ready
    )
    
    // Seleccionar los mensajes según el tipo
    val messages = if (isTimeline) timelineMessages else summaryMessages
    
    // Efecto para cambiar los mensajes cada 3 segundos
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            currentMessageIndex = (currentMessageIndex + 1) % messages.size
        }
    }

    Dialog(
        onDismissRequest = { /* No se puede cerrar */ }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                
                Text(
                    text = stringResource(messages[currentMessageIndex]),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
} 