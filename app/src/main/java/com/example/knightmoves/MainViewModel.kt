package com.example.knightmoves

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application,
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val mainLocalRepo: MainLocalRepo
) : AndroidViewModel(application) {

    private val _knightPosition =
        MutableLiveData<ChessBoardMove>(mainLocalRepo.knightPosition)
    val knightPosition: LiveData<ChessBoardMove?> get() = _knightPosition

    private val _kingPosition =
        MutableLiveData<ChessBoardMove>(mainLocalRepo.kingPosition)
    val kingPosition: LiveData<ChessBoardMove?> get() = _kingPosition

    private val _size = MutableLiveData(mainLocalRepo.size)
    val size: LiveData<Int?> get() = _size

    private val _moves = MutableLiveData(mainLocalRepo.moves)
    val moves: LiveData<Int?> get() = _moves

    private val _result = MutableLiveData<List<String>?>(mainLocalRepo.result)
    val result: LiveData<List<String>?> get() = _result

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        job = null
        super.onCleared()
    }

    fun placeKnight(row: Int = -1, column: Int = -1) {
        if (row <= 0 || column <= 0) {
            _knightPosition.value = null
            mainLocalRepo.knightPosition = null
        } else if (row <= 16 && column <= 16) {
            _knightPosition.value = row to column
            mainLocalRepo.knightPosition = _knightPosition.value
        }
    }

    fun placeKing(row: Int = -1, column: Int = -1) {
        if (row <= 0 || column <= 0) {
            _kingPosition.value = null
            mainLocalRepo.kingPosition = null
        } else if (row <= 16 && column <= 16) {
            _kingPosition.value = row to column
            mainLocalRepo.kingPosition = _kingPosition.value
        }
    }

    fun setSize(size: String) {
        _size.value = size.toIntOrNull()
        if (_size.value in 6..16)
            mainLocalRepo.size = _size.value
    }

    fun setMoves(moves: String) {
        _moves.value = moves.toIntOrNull()
        if (_moves.value in 2..10)
            mainLocalRepo.moves = _moves.value
    }

    fun cleanBoard() {
        setResult()
        placeKing()
        placeKnight()
        _isLoading.value = false
    }

    fun calculateMoves() {
        job?.cancel()
        setResult()
        val maxMoves = moves.value
        val boardSize = size.value
        val knightPositionValue = knightPosition.value?.move(-1, -1)
        val kingPositionValue = kingPosition.value?.move(-1, -1)
        if (knightPositionValue != null
            && kingPositionValue != null
            && boardSize != null && boardSize in 6..16
            && maxMoves != null && maxMoves in 2..10
        ) {
            _isLoading.value = true
            job = viewModelScope.launch(defaultCoroutineDispatcher) {
                val result: MutableList<List<String>> = mutableListOf()
                calculatePath(
                    knightPositionValue,
                    kingPositionValue,
                    boardSize,
                    maxMoves,
                    maxMoves,
                    mutableListOf(),
                    result
                )
                result.map {
                    it.toModifiedString(
                        "\"" to "",
                        "," to " ",
                        "[" to "",
                        "]" to "",
                    )
                }.apply {
                    if (isNotEmpty())
                        setResult(this)
                    else
                        setResult(listOf(getApplication<Application>().getString(R.string.message_no_result)))
                    _isLoading.postValue(false)
                }
            }
        }
    }

    private fun setResult(value: List<String>? = null) {
        mainLocalRepo.result = value
        _result.postValue(value)
    }

    private fun calculatePath(
        knightPosition: ChessBoardMove,
        kingPosition: ChessBoardMove,
        boardSize: Int,
        maxMoves: Int,
        currentMoves: Int,
        currentPath: MutableList<String>,
        possiblePaths: MutableList<List<String>>
    ) {
        //exit condition
        if (currentMoves > 0) {
            //optimization not to calculate obviously out of range variants
            val optimizerFactor = currentMoves * 2 + 1
            if (knightPosition distanceTo kingPosition < optimizerFactor) {
                //all the possible knight moves from current tile
                knightPosition.getKnightPossibleMoves(boardSize).apply {
                    //remove from list and save if knight has stepped on tile with king
                    removeFirstOrNullIf { it hasSteppedOn kingPosition }?.also {
                        possiblePaths.add(ArrayList(currentPath).apply {
                            add(
                                it.algebraicNotation(
                                    maxMoves - currentMoves + 1,
                                    knightPosition,
                                    boardSize
                                )
                            )
                        })
                    }
                    //recursive call for all the other possible moves
                    forEach {
                        calculatePath(
                            it,
                            kingPosition,
                            boardSize,
                            maxMoves,
                            currentMoves - 1,
                            //to have previous tiles of the path
                            ArrayList(currentPath).apply {
                                add(
                                    it.algebraicNotation(
                                        maxMoves - currentMoves + 1,
                                        knightPosition,
                                        boardSize
                                    )
                                )
                            },
                            possiblePaths
                        )
                    }
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(
            application,
            Dispatchers.Default,
            MainLocalRepo(application.applicationContext)
        ) as T
    }
}