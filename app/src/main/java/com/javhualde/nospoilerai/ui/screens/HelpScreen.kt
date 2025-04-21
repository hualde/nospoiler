package com.javhualde.nospoilerapk.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.javhualde.nospoilerapk.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.help)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ExpandableFAQItem(
                    title = stringResource(R.string.faq_modes_title),
                    content = stringResource(R.string.faq_modes_content)
                )
            }
            
            item {
                ExpandableFAQItem(
                    title = stringResource(R.string.faq_when_use_title),
                    content = stringResource(R.string.faq_when_use_content)
                )
            }

            item {
                ExpandableFAQItem(
                    title = stringResource(R.string.faq_search_title),
                    content = stringResource(R.string.faq_search_content)
                )
            }

            item {
                ExpandableFAQItem(
                    title = stringResource(R.string.faq_range_title),
                    content = stringResource(R.string.faq_range_content)
                )
            }

            item {
                ExpandableFAQItem(
                    title = stringResource(R.string.faq_summary_title),
                    content = stringResource(R.string.faq_summary_content)
                )
            }

            item {
                ExpandableFAQItem(
                    title = stringResource(R.string.faq_timeline_title),
                    content = stringResource(R.string.faq_timeline_content)
                )
            }
        }
    }
}

@Composable
private fun ExpandableFAQItem(
    title: String,
    content: String
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotationState)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
} 