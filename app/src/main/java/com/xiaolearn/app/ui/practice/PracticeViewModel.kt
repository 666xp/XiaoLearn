package com.xiaolearn.app.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.xiaolearn.app.data.model.NoteDef
import com.xiaolearn.app.data.model.PlaySpeed
import com.xiaolearn.app.data.model.ScoreDef
import com.xiaolearn.app.data.model.ScoreToken
import com.xiaolearn.app.data.model.XiaoKey
import com.xiaolearn.app.data.prefs.UserPrefs
import com.xiaolearn.app.data.repository.ContentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PracticeUiState(
    val score: ScoreDef? = null,
    val tokens: List<ScoreToken> = emptyList(),
    val index: Int = 0,
    val isPlaying: Boolean = false,
    val finished: Boolean = false,
    val speed: PlaySpeed = PlaySpeed.Slower,
    val key: XiaoKey = XiaoKey.F,
    val holeLabels: List<String> = emptyList(),
    val currentNote: NoteDef? = null,
    val isRest: Boolean = false
)

class PracticeViewModel(
    private val scoreId: String,
    private val repo: ContentRepository,
    private val prefs: UserPrefs
) : ViewModel() {

    private val _state = MutableStateFlow(PracticeUiState())
    val state: StateFlow<PracticeUiState> = _state.asStateFlow()

    private var playJob: Job? = null

    init {
        viewModelScope.launch {
            val score = repo.scoreById(scoreId) ?: return@launch
            val savedIndex = prefs.progressFlow(scoreId).first().coerceIn(0, score.flatTokens.lastIndex.coerceAtLeast(0))
            val key = prefs.xiaoKey.first()
            val speed = prefs.playSpeed.first()
            _state.value = PracticeUiState(
                score = score,
                tokens = score.flatTokens,
                index = savedIndex,
                speed = speed,
                key = key,
                holeLabels = repo.holeLabels
            )
            refreshCurrent()
        }

        viewModelScope.launch {
            prefs.xiaoKey.collect { key ->
                _state.update { it.copy(key = key) }
            }
        }
    }

    fun setKey(key: XiaoKey) {
        viewModelScope.launch { prefs.setXiaoKey(key) }
    }

    fun setSpeed(speed: PlaySpeed) {
        _state.update { it.copy(speed = speed) }
        viewModelScope.launch { prefs.setPlaySpeed(speed) }
        if (_state.value.isPlaying) {
            pause()
            play()
        }
    }

    fun selectIndex(index: Int, pauseIfPlaying: Boolean = true) {
        val tokens = _state.value.tokens
        if (tokens.isEmpty()) return
        val safe = index.coerceIn(0, tokens.lastIndex)
        if (pauseIfPlaying && _state.value.isPlaying) {
            pause()
        }
        _state.update {
            it.copy(index = safe, finished = false)
        }
        refreshCurrent()
        persistProgress()
    }

    fun previous() {
        selectIndex(_state.value.index - 1)
    }

    fun next() {
        val s = _state.value
        if (s.index >= s.tokens.lastIndex) {
            pause()
            _state.update { it.copy(finished = true, isPlaying = false) }
            return
        }
        selectIndex(s.index + 1)
    }

    fun togglePlay() {
        if (_state.value.isPlaying) pause() else play()
    }

    fun replay() {
        pause()
        selectIndex(0, pauseIfPlaying = false)
        play()
    }

    fun play() {
        val s = _state.value
        if (s.tokens.isEmpty()) return
        if (s.finished) {
            selectIndex(0, pauseIfPlaying = false)
        }
        _state.update { it.copy(isPlaying = true, finished = false) }
        playJob?.cancel()
        playJob = viewModelScope.launch {
            while (true) {
                val current = _state.value
                if (!current.isPlaying) break
                val token = current.tokens.getOrNull(current.index) ?: break
                val wait = (token.beats * current.speed.msPerBeat).toLong().coerceAtLeast(300L)
                delay(wait)
                val after = _state.value
                if (!after.isPlaying) break
                if (after.index >= after.tokens.lastIndex) {
                    _state.update { it.copy(isPlaying = false, finished = true) }
                    break
                }
                val newIndex = after.index + 1
                _state.update { it.copy(index = newIndex) }
                refreshCurrent()
                persistProgress()
            }
        }
    }

    fun pause() {
        playJob?.cancel()
        playJob = null
        _state.update { it.copy(isPlaying = false) }
        persistProgress()
    }

    private fun refreshCurrent() {
        val s = _state.value
        val token = s.tokens.getOrNull(s.index)
        val rest = token?.rest == true
        val note = if (rest) null else repo.noteByPitch(token?.pitch)
        _state.update { it.copy(currentNote = note, isRest = rest) }
    }

    private fun persistProgress() {
        val s = _state.value
        val id = s.score?.id ?: return
        viewModelScope.launch { prefs.setProgress(id, s.index) }
    }

    override fun onCleared() {
        playJob?.cancel()
        super.onCleared()
    }

    class Factory(
        private val scoreId: String,
        private val repo: ContentRepository,
        private val prefs: UserPrefs
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PracticeViewModel(scoreId, repo, prefs) as T
        }
    }
}
