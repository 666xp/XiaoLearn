package com.xiaolearn.app.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiaolearn.app.data.model.ScoreDef
import com.xiaolearn.app.data.model.XiaoKey
import com.xiaolearn.app.data.prefs.UserPrefs
import com.xiaolearn.app.data.repository.ContentRepository
import com.xiaolearn.app.ui.components.KeySwitcher
import com.xiaolearn.app.ui.theme.Ink
import com.xiaolearn.app.ui.theme.InkMute
import com.xiaolearn.app.ui.theme.Paper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    repo: ContentRepository,
    prefs: UserPrefs,
    onOpenScore: (String) -> Unit,
    onOpenChart: () -> Unit
) {
    val key by prefs.xiaoKey.collectAsState(initial = XiaoKey.F)
    val guideExpanded by prefs.guideExpanded.collectAsState(initial = true)
    val scope = rememberCoroutineScope()
    val scores = repo.scores
    val reading = repo.reading

    Scaffold(
        containerColor = Paper,
        topBar = {
            TopAppBar(
                title = {
                    Text("箫学", style = MaterialTheme.typography.headlineMedium)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = reading.keyTip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkMute
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("简谱怎么读", style = MaterialTheme.typography.titleMedium, color = Ink)
                    TextButton(onClick = {
                        scope.launch { prefs.setGuideExpanded(!guideExpanded) }
                    }) {
                        Text(if (guideExpanded) "收起" else "展开")
                    }
                }
                if (guideExpanded) {
                    Text(
                        text = reading.howToRead + "\n" + reading.holeLegend,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Ink
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onOpenChart) {
                    Text("指法一览")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("练习谱", style = MaterialTheme.typography.titleMedium, color = Ink)
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(scores, key = { it.id }) { score ->
                ScoreRow(score = score, onClick = { onOpenScore(score.id) })
                HorizontalDivider(color = InkMute.copy(alpha = 0.2f))
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ScoreRow(score: ScoreDef, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp)
    ) {
        Text(score.title, style = MaterialTheme.typography.titleMedium, color = Ink)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${score.difficulty} · 用音 ${score.usedNotes.joinToString(" ")} · ${score.lines.size} 句",
            style = MaterialTheme.typography.bodyMedium,
            color = InkMute
        )
    }
}
