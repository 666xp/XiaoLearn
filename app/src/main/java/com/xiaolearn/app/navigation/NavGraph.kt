package com.xiaolearn.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.xiaolearn.app.data.prefs.UserPrefs
import com.xiaolearn.app.data.repository.ContentRepository
import com.xiaolearn.app.ui.chart.ChartScreen
import com.xiaolearn.app.ui.list.ListScreen
import com.xiaolearn.app.ui.practice.PracticeScreen

object Routes {
    const val LIST = "list"
    const val CHART = "chart"
    const val PRACTICE = "practice/{scoreId}"
    fun practice(scoreId: String) = "practice/$scoreId"
}

@Composable
fun XiaoNavGraph(repo: ContentRepository, prefs: UserPrefs) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            ListScreen(
                repo = repo,
                prefs = prefs,
                onOpenScore = { nav.navigate(Routes.practice(it)) },
                onOpenChart = { nav.navigate(Routes.CHART) }
            )
        }
        composable(Routes.CHART) {
            ChartScreen(
                repo = repo,
                prefs = prefs,
                onBack = { nav.popBackStack() }
            )
        }
        composable(
            route = Routes.PRACTICE,
            arguments = listOf(navArgument("scoreId") { type = NavType.StringType })
        ) { entry ->
            val scoreId = entry.arguments?.getString("scoreId").orEmpty()
            PracticeScreen(
                scoreId = scoreId,
                repo = repo,
                prefs = prefs,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
