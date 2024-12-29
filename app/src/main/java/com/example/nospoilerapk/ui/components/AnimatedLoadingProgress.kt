package com.example.nospoilerapk.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.nospoilerapk.R
import kotlinx.coroutines.delay

@Composable
fun AnimatedLoadingProgress(
    isTimeline: Boolean = false,
    isComplete: Boolean = false
) {
    var progress by remember { mutableFloatStateOf(0f) }
    val initialProgress = remember { 0f }
    
    LaunchedEffect(isComplete) {
        if (isComplete) {
            // Si está completo, llenar rápidamente hasta el final
            progress = 1f
        } else {
            // Si no está completo, incrementar gradualmente hasta 0.85
            progress = initialProgress
            while (progress < 0.85f && !isComplete) {
                delay(150)  // Aumentamos el delay de 100 a 150ms
                progress += 0.005f  // Reducimos el incremento de 0.01f a 0.005f
                if (progress > 0.85f) progress = 0.85f
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(
                if (isTimeline) R.string.generating_timeline 
                else R.string.generating_summary
            ),
            style = MaterialTheme.typography.bodyLarge
        )
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            color = MaterialTheme.colorScheme.primary
        )
    }
} 