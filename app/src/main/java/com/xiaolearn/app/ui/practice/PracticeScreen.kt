package com.xiaolearn.app.ui.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xiaolearn.app.data.model.PlaySpeed
import com.xiaolearn.app.data.prefs.UserPrefs
import com.xiaolearn.app.data.repository.ContentRepository
import com.xiaolearn.app.ui.components.HoleDiagram
import com.xiaolearn.app.ui.components.JianpuChip
import com.xiaolearn.app.ui.components.KeySwitcher
import com.xiaolearn.app.ui.components.tokenLabel
import com.xiaolearn.app.ui.theme.Accent
import com.xiaolearn.app.ui.theme.Ink
import com.xiaolearn.app.ui.theme.InkMute
import com.xiaolearn.app.ui.theme.Paper
import com.xiaolearn.app.ui.theme.Panel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PracticeScreen(
    scoreId: String,
    repo: ContentRepository,
    prefs: UserPrefs,
    onBack: () -> Unit
) {
    val vm: PracticeViewModel = viewModel(
        factory = PracticeViewModel.Factory(scoreId, repo, prefs)
    )
    val state by vm.state.collectAsState()
    val scroll = rememberScrollState()

    val lineRanges = remember(state.score) {
        val score = state.score ?: return@remember emptyList()
        var cursor = 0
        score.lines.map { line ->
            val start = cursor
            cursor += line.size
            start until cursor
        }
    }

    LaunchedEffect(state.index, state.isPlaying) {
        // Keep current token roughly in view by scrolling mid-way when near edges.
        val approx = (state.index * 48).coerceAtLeast(0)
        if (kotlin.math.abs(scroll.value - approx) > 120) {
            scroll.animateScrollTo(approx.coerceAtMost(scroll.maxValue))
        }
    }

    Scaffold(
        containerColor = Paper,
        topBar = {
            TopAppBar(
                title = {
                    Text(state.score?.title ?: "练谱", style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        vm.pause()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    KeySwitcher(selected = state.key, onSelect = vm::setKey)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scroll)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "当前按 ${state.key.label}箫对照",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkMute
                )
                lineRanges.forEachIndexed { lineIndex, range ->
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        range.forEach { tokenIndex ->
                            val token = state.tokens[tokenIndex]
                            JianpuChip(
                                text = tokenLabel(token.pitch, token.rest, token.beats),
                                selected = tokenIndex == state.index,
                                passed = tokenIndex < state.index,
                                onClick = { vm.selectIndex(tokenIndex) }
                            )
                        }
                    }
                    if (lineIndex < lineRanges.lastIndex) {
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Panel, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.finished) {
                    Text("本曲结束", style = MaterialTheme.typography.titleMedium, color = Ink)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = vm::replay,
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                    ) {
                        Text("再来一遍")
                    }
                } else {
                    val jianpu = if (state.isRest) "空" else (state.currentNote?.jianpu ?: "—")
                    val abs = state.currentNote?.absolute?.get(state.key.name) ?: "—"
                    Text(
                        text = "$jianpu  ·  ${state.key.label} $abs",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 26.sp,
                        color = Ink
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HoleDiagram(
                        holes = state.currentNote?.holes,
                        labels = state.holeLabels,
                        isRest = state.isRest,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = vm::previous) { Text("上一音") }
                    Button(
                        onClick = vm::togglePlay,
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text(if (state.isPlaying) "暂停" else "开始", fontSize = 18.sp)
                    }
                    OutlinedButton(onClick = vm::next) { Text("下一音") }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    PlaySpeed.entries.forEach { speed ->
                        val selected = speed == state.speed
                        TextButton(onClick = { vm.setSpeed(speed) }) {
                            Text(
                                text = speed.label,
                                color = if (selected) Accent else InkMute,
                                fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold
                                else androidx.compose.ui.text.font.FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
