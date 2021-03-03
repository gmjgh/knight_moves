package com.example.knightmoves

import android.content.Context
import android.content.SharedPreferences

class MainLocalRepo(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var size: Int?
        get() = prefs.getInt(PREF_SIZE, 6)
        set(value) = value.checkNull(PREF_SIZE) {
            prefs.edit().putInt(PREF_SIZE, it).apply()
        }

    var moves: Int?
        get() = prefs.getInt(PREF_MOVES, 3)
        set(value) = value.checkNull(PREF_MOVES) {
            prefs.edit().putInt(PREF_MOVES, it).apply()
        }

    var knightPosition: ChessBoardMove?
        get() = prefs.getString(PREF_KNIGHT_POSITION, null)?.toObject()
        set(value) = value.checkNull(PREF_KNIGHT_POSITION) {
            prefs.edit().putString(PREF_KNIGHT_POSITION, it.toJson()).apply()
        }

    var kingPosition: ChessBoardMove?
        get() = prefs.getString(PREF_KING_POSITION, null)?.toObject()
        set(value) = value.checkNull(PREF_KING_POSITION) {
            prefs.edit().putString(PREF_KING_POSITION, it.toJson()).apply()
        }

    var result: List<String>?
        get() = prefs.getString(PREF_RESULT, null)?.toObject()
        set(value) = value.checkNull(PREF_RESULT) {
            prefs.edit().putString(PREF_RESULT, it.toJson()).apply()
        }

    private fun <T> T?.checkNull(key: String, notNull: (T) -> Unit) {
        if (this == null && prefs.contains(key))
            prefs.edit().remove(key).apply()
        else if (this != null)
            notNull(this)
    }

    companion object {
        private const val PREFS_NAME = "knights_moves.prefs"
        private const val PREF_SIZE = "PREF_SIZE"
        private const val PREF_MOVES = "PREF_MOVES"
        private const val PREF_RESULT = "PREF_RESULT"
        private const val PREF_KNIGHT_POSITION = "PREF_KNIGHT_POSITION"
        private const val PREF_KING_POSITION = "PREF_KING_POSITION"
    }
}