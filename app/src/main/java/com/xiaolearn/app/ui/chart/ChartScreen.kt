package com.xiaolearn.app.ui.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiaolearn.app.data.model.XiaoKey
import com.xiaolearn.app.data.prefs.UserPrefs
import com.xiaolearn.app.data.repository.ContentRepository
import com.xiaolearn.app.ui.components.HoleDiagram
import com.xiaolearn.app.ui.components.JianpuChip
import com.xiaolearn.app.ui.components.KeySwitcher
import com.xiaolearn.app.ui.theme.Ink
import com.xiaolearn.app.ui.theme.InkMute
import com.xiaolearn.app.ui.theme.Paper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChartScreen(
    repo: ContentRepository,
    prefs: UserPrefs,
    onBack: () -> Unit
) {
    val key by prefs.xiaoKey.collectAsState(initial = XiaoKey.F)
    val scope = rememberCoroutineScope()
    val notes = repo.notes
    var selectedId by remember { mutableStateOf(notes.firstOrNull()?.id) }
    val selected = notes.find { it.id == selectedId }

    Scaffold(
        containerColor = Paper,
        topBar = {
            TopAppBar(
                title = { Text("指法一览", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    KeySwitcher(
                        selected = key,
                        onSelect = { scope.launch { prefs.setXiaoKey(it) } }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "点音看孔。${key.label}与另一调开闭相同，音名不同。",
                style = MaterialTheme.typography.bodyMedium,
                color = InkMute
            )
            Spacer(modifier = Modifier.height(16.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                notes.forEach { note ->
                    JianpuChip(
                        text = note.jianpu,
                        selected = note.id == selectedId,
                        passed = false,
                        onClick = { selectedId = note.id }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            if (selected != null) {
                Text(
                    text = "${selected.jianpu}  ·  ${key.label} ${selected.absolute[key.name]}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink
                )
                Spacer(modifier = Modifier.height(12.dp))
                HoleDiagram(
                    holes = selected.holes,
                    labels = repo.holeLabels,
                    isRest = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
