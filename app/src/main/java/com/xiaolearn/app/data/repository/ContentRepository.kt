package com.xiaolearn.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.xiaolearn.app.data.model.NoteDef
import com.xiaolearn.app.data.model.NotesFile
import com.xiaolearn.app.data.model.ReadingContent
import com.xiaolearn.app.data.model.ScoreDef
import com.xiaolearn.app.data.model.ScoresFile

class ContentRepository(private val context: Context) {
    private val gson = Gson()

    private val notesFile: NotesFile by lazy {
        readAsset("content/notes.json", NotesFile::class.java)
    }

    private val scoresFile: ScoresFile by lazy {
        readAsset("content/scores.json", ScoresFile::class.java)
    }

    val reading: ReadingContent by lazy {
        readAsset("content/reading.json", ReadingContent::class.java)
    }

    val holeLabels: List<String> get() = notesFile.holeLabels
    val notes: List<NoteDef> get() = notesFile.notes
    val scores: List<ScoreDef> get() = scoresFile.scores

    fun noteByPitch(pitch: String?): NoteDef? {
        if (pitch == null) return null
        return notes.find { it.jianpu == pitch || it.id == pitch }
    }

    fun scoreById(id: String): ScoreDef? = scores.find { it.id == id }

    private fun <T> readAsset(path: String, clazz: Class<T>): T {
        context.assets.open(path).bufferedReader().use { reader ->
            return gson.fromJson(reader, clazz)
        }
    }
}
