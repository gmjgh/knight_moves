package com.example.knightmoves

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
open class MainViewModelUnitTest {

    private lateinit var mainViewModel: MainViewModel
    private val testDispatcher = TestCoroutineDispatcher()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var application: Application

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var sharedPreferences: SharedPreferences

    @Mock
    lateinit var editor: SharedPreferences.Editor

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        stubSharedPrefs()
        mainViewModel = MainViewModel(application, testDispatcher, MainLocalRepo(context))
    }

    private fun stubSharedPrefs() {
        `when`(application.applicationContext).thenReturn(context)
        `when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences)
        `when`(sharedPreferences.edit()).thenReturn(editor)
        `when`(sharedPreferences.getInt(anyString(), anyInt())).thenReturn(7)
        `when`(sharedPreferences.contains(anyString())).thenReturn(true)
        `when`(sharedPreferences.getString(anyString(), anyString())).thenReturn("No result")
        `when`(editor.putInt(anyString(), anyInt())).thenReturn(editor)
        `when`(editor.putString(anyString(), anyString())).thenReturn(editor)
        `when`(editor.remove(anyString())).thenReturn(editor)
        doNothing().`when`(editor).apply()
    }

    @Test
    fun setSize_success() {
        (6..16).forEachIndexed { index, i ->
            mainViewModel.setSize(i.toString())
            assertEquals(mainViewModel.size.value, i)
            verify(sharedPreferences, times(index + 1)).edit()
            verify(editor, times(1)).putInt(anyString(), eq(i))
        }
    }

    @Test
    fun setSize_failure() {
        (-1..5).forEach {
            mainViewModel.setSize(it.toString())
            assertEquals(mainViewModel.size.value, it)
            verify(sharedPreferences, times(0)).edit()
            verify(editor, times(0)).putInt(anyString(), anyInt())
        }
        (17..20).forEach {
            mainViewModel.setSize(it.toString())
            assertEquals(mainViewModel.size.value, it)
            verify(sharedPreferences, times(0)).edit()
            verify(editor, times(0)).putInt(anyString(), anyInt())
        }
    }

    @Test
    fun setMoves_success() {
        (2..10).forEachIndexed { index, i ->
            mainViewModel.setMoves(i.toString())
            assertEquals(mainViewModel.moves.value, i)
            verify(sharedPreferences, times(index + 1)).edit()
            verify(editor, times(1)).putInt(anyString(), eq(i))
        }
    }

    @Test
    fun setMoves_failure() {
        (-3..1).forEach {
            mainViewModel.setMoves(it.toString())
            assertEquals(mainViewModel.moves.value, it)
            verify(sharedPreferences, times(0)).edit()
            verify(editor, times(0)).putInt(anyString(), anyInt())
        }
        (11..15).forEach {
            mainViewModel.setMoves(it.toString())
            assertEquals(mainViewModel.moves.value, it)
            verify(sharedPreferences, times(0)).edit()
            verify(editor, times(0)).putInt(anyString(), anyInt())
        }
    }

    @Test
    fun placeKnight_success() {
        mainViewModel.placeKnight(2, 2)
        assertEquals(mainViewModel.knightPosition.value, ChessBoardMove(2, 2))
        verify(sharedPreferences, times(1)).edit()
        verify(editor, times(1)).putString(anyString(), anyString())
    }

    @Test
    fun placeKnight_failure() {
        mainViewModel.placeKnight(17, 17)
        assertNotEquals(mainViewModel.knightPosition.value, ChessBoardMove(17, 17))
        verify(sharedPreferences, times(0)).edit()
        verify(editor, times(0)).putString(anyString(), anyString())
    }

    @Test
    fun removeKnight_success() {
        mainViewModel.placeKnight()
        assertEquals(mainViewModel.knightPosition.value, null)

        mainViewModel.placeKnight(-100, -100)
        assertEquals(mainViewModel.knightPosition.value, null)

        verify(sharedPreferences, times(2)).edit()
        verify(editor, times(2)).remove(anyString())
    }

    @Test
    fun placeKing_success() {
        mainViewModel.placeKing(3, 3)
        assertEquals(mainViewModel.kingPosition.value, ChessBoardMove(3, 3))
        verify(sharedPreferences, times(1)).edit()
        verify(editor, times(1)).putString(anyString(), anyString())
    }

    @Test
    fun placeKing_failure() {
        mainViewModel.placeKing(17, 17)
        assertNotEquals(mainViewModel.kingPosition.value, ChessBoardMove(17, 17))
        verify(sharedPreferences, times(0)).edit()
        verify(editor, times(0)).putString(anyString(), anyString())
    }

    @Test
    fun removeKing_success() {
        mainViewModel.placeKing()
        assertEquals(mainViewModel.kingPosition.value, null)

        mainViewModel.placeKing(-100, -100)
        assertEquals(mainViewModel.kingPosition.value, null)
        verify(sharedPreferences, times(2)).edit()
        verify(editor, times(2)).remove(anyString())
    }

    @Test
    fun calculateMoves_success_noResult() {
        `when`(application.getString(anyInt())).thenReturn(SUCCESS_RESULT_FOR_NO_RESULT)
        mainViewModel.setSize("10")
        mainViewModel.setMoves("2")
        mainViewModel.placeKnight(1, 1)
        mainViewModel.placeKing(9, 9)

        mainViewModel.calculateMoves()

        assertEquals(mainViewModel.result.value, listOf(SUCCESS_RESULT_FOR_NO_RESULT))
        verify(sharedPreferences, times(6)).edit()
        verify(editor, times(1)).remove(anyString())
        verify(editor, times(2)).putInt(anyString(), anyInt())
        verify(editor, times(3)).putString(anyString(), anyString())
    }

    @Test
    fun calculateMoves_success() {
        mainViewModel.setSize("6")
        mainViewModel.setMoves("3")
        mainViewModel.placeKnight(2, 3)
        mainViewModel.placeKing(4, 4)

        mainViewModel.calculateMoves()

        assertEquals(mainViewModel.result.value, SUCCESS_RESULT_FOR_23_44)
        verify(sharedPreferences, times(6)).edit()
        verify(editor, times(1)).remove(anyString())
        verify(editor, times(2)).putInt(anyString(), anyInt())
        verify(editor, times(3)).putString(anyString(), anyString())
    }

    @Test
    fun calculateMoves_failure() {
        mainViewModel.setSize("3")
        mainViewModel.setMoves("12")
        mainViewModel.placeKnight(2, 3)
        mainViewModel.placeKing(4, 4)

        mainViewModel.calculateMoves()

        assertEquals(mainViewModel.result.value, null)
        verify(sharedPreferences, times(3)).edit()
        verify(editor, times(1)).remove(anyString())
        verify(editor, times(2)).putString(anyString(), anyString())
    }

    @Test
    fun clearBoard_success() {
        mainViewModel.cleanBoard()
        assertNull(mainViewModel.knightPosition.value)
        assertNull(mainViewModel.kingPosition.value)
        assertNull(mainViewModel.result.value)
        assertTrue(mainViewModel.isLoading.value == false)

        verify(sharedPreferences, times(3)).edit()
        verify(editor, times(3)).remove(anyString())
    }

    companion object {
        const val SUCCESS_RESULT_FOR_NO_RESULT = "No solution has been found"
        val SUCCESS_RESULT_FOR_23_44 = listOf(
            "1. 5c 3d",
            "1. 5c 6a 2. 6a 5c 3. 5c 3d",
            "1. 5c 6a 2. 6a 4b 3. 4b 3d",
            "1. 5c 4a 2. 4a 5c 3. 5c 3d",
            "1. 5c 4a 2. 4a 2b 3. 2b 3d",
            "1. 5c 6e 2. 6e 5c 3. 5c 3d",
            "1. 5c 6e 2. 6e 4f 3. 4f 3d",
            "1. 5c 3b 2. 3b 5c 3. 5c 3d",
            "1. 5c 3b 2. 3b 1c 3. 1c 3d",
            "1. 5c 4e 2. 4e 5c 3. 5c 3d",
            "1. 5c 4e 2. 4e 2f 3. 2f 3d"
        )
    }

}