package com.xiaolearn.app.data.model

enum class XiaoKey(val label: String) {
    F("F 调"),
    G("G 调");

    companion object {
        fun fromStorage(value: String?): XiaoKey =
            entries.find { it.name == value } ?: F
    }
}

enum class PlaySpeed(val label: String, val msPerBeat: Long) {
    Slow("慢", 1200),
    Slower("更慢", 1800),
    Slowest("极慢", 2400)
}

data class NoteDef(
    val id: String,
    val jianpu: String,
    val holes: List<Boolean>,
    val absolute: Map<String, String>
)

data class ScoreToken(
    val pitch: String? = null,
    val rest: Boolean = false,
    val beats: Double = 1.0
)

data class ScoreDef(
    val id: String,
    val title: String,
    val difficulty: String,
    val usedNotes: List<String>,
    val lines: List<List<ScoreToken>>
) {
    val flatTokens: List<ScoreToken> by lazy { lines.flatten() }
}

data class ReadingContent(
    val howToRead: String,
    val holeLegend: String,
    val keyTip: String
)

data class NotesFile(
    val holeLabels: List<String>,
    val notes: List<NoteDef>
)

data class ScoresFile(
    val scores: List<ScoreDef>
)
