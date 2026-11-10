package com.xiaolearn.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.xiaolearn.app.data.prefs.UserPrefs
import com.xiaolearn.app.data.repository.ContentRepository
import com.xiaolearn.app.navigation.XiaoNavGraph
import com.xiaolearn.app.ui.theme.Paper
import com.xiaolearn.app.ui.theme.XiaoLearnTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val repo = remember { ContentRepository(applicationContext) }
            val prefs = remember { UserPrefs(applicationContext) }
            XiaoLearnTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Paper) {
                    XiaoNavGraph(repo = repo, prefs = prefs)
                }
            }
        }
    }
}
