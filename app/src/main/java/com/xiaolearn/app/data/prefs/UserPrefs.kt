package com.xiaolearn.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.xiaolearn.app.data.model.PlaySpeed
import com.xiaolearn.app.data.model.XiaoKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "xiaolearn_prefs")

class UserPrefs(private val context: Context) {
    private val keyXiao = stringPreferencesKey("xiao_key")
    private val keyGuide = booleanPreferencesKey("guide_expanded")
    private val keySpeed = stringPreferencesKey("play_speed")

    val xiaoKey: Flow<XiaoKey> = context.dataStore.data.map {
        XiaoKey.fromStorage(it[keyXiao])
    }

    val guideExpanded: Flow<Boolean> = context.dataStore.data.map {
        it[keyGuide] ?: true
    }

    val playSpeed: Flow<PlaySpeed> = context.dataStore.data.map { prefs ->
        PlaySpeed.entries.find { it.name == prefs[keySpeed] } ?: PlaySpeed.Slower
    }

    fun progressFlow(scoreId: String): Flow<Int> {
        val key = intPreferencesKey("progress_$scoreId")
        return context.dataStore.data.map { it[key] ?: 0 }
    }

    suspend fun setXiaoKey(key: XiaoKey) {
        context.dataStore.edit { it[keyXiao] = key.name }
    }

    suspend fun setGuideExpanded(expanded: Boolean) {
        context.dataStore.edit { it[keyGuide] = expanded }
    }

    suspend fun setPlaySpeed(speed: PlaySpeed) {
        context.dataStore.edit { it[keySpeed] = speed.name }
    }

    suspend fun setProgress(scoreId: String, index: Int) {
        val key = intPreferencesKey("progress_$scoreId")
        context.dataStore.edit { it[key] = index }
    }
}
