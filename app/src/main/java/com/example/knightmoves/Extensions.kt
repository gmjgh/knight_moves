package com.example.knightmoves

import android.content.Context

import com.google.gson.Gson
import android.util.DisplayMetrics
import kotlin.math.abs
import kotlin.math.max

const val alphabet = "abcdefghijklmnopqrstuvwxyz"

fun Any.toJson(): String = Gson().toJson(this)

fun Any.toModifiedString(vararg modifiers: StringModifier): String = Gson().toJson(this).run {
    var result = this
    modifiers.forEach {
        result = result.replace(it.source, it.modifier)
    }
    result
}

inline fun <reified T> String.toObject(): T =
    Gson().fromJson(this, T::class.java)

fun Number.pixelsToDp(context: Context): Float {
    return this.toFloat() / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

typealias StringModifier = Pair<String, String>

val StringModifier.source: String
    get() = first.toString()

val StringModifier.modifier: String
    get() = second.toString()

typealias ChessBoardMove = Pair<Int, Int>

val ChessBoardMove.x: Int
    get() = first.toInt()

val ChessBoardMove.y: Int
    get() = second.toInt()

infix fun ChessBoardMove.distanceTo(other: ChessBoardMove): Int =
    abs(max(this.x - other.x, this.y - other.y))

infix fun ChessBoardMove.hasSteppedOn(other: ChessBoardMove): Boolean =
    this.x == other.x && this.y == other.y

//https://en.wikipedia.org/wiki/Algebraic_notation_(chess)#Notation_for_a_series_of_moves
fun ChessBoardMove.algebraicNotation(
    currentMove: Int,
    initialPosition: ChessBoardMove,
    boardSize: Int
): String =
    "$currentMove. ${initialPosition.notated(boardSize)} ${this.notated(boardSize)}"

fun ChessBoardMove.move(x: Int, y: Int): ChessBoardMove =
    (this.x + x) to (this.y + y)

fun ChessBoardMove.getKnightPossibleMoves(
    boardSize: Int
): MutableList<ChessBoardMove> = listOfKnightMoves(
    boardSize,
    move(-1, -2),
    move(-2, -1),
    move(-2, 1),
    move(1, -2),
    move(-1, 2),
    move(2, -1),
    move(1, 2),
    move(2, 1)
)

fun <T> MutableList<T>.removeFirstOrNullIf(predicate: (T) -> Boolean): T? {
    val each = iterator()
    while (each.hasNext()) {
        val element = each.next()
        if (predicate(element)) {
            each.remove()
            return element
        }
    }
    return null
}

private fun ChessBoardMove.notated(boardSize: Int): String = "${boardSize - x}${alphabet[y]}"

private fun listOfKnightMoves(boardSize: Int, vararg moves: ChessBoardMove): MutableList<ChessBoardMove> {
    fun isValid(move: ChessBoardMove): Boolean =
        move.x in 0..boardSize && move.y in 0..boardSize
    return moves.filter(::isValid).toMutableList()
}