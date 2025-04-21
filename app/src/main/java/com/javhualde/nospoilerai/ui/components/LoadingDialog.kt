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

@Composable
fun LoadingDialog(
    isTimeline: Boolean = false
) {
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
                    text = stringResource(
                        if (isTimeline) R.string.generating_timeline 
                        else R.string.generating_summary
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
} 