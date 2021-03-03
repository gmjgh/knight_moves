package com.example.knightmoves

import android.app.Application
import android.graphics.fonts.FontStyle
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.knightmoves.ui.theme.KnightMovesTheme
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        calcScreenSize()
        setContent {
            KnightMovesTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Column {
                        Input()
                        Table()
                        Result()
                    }

                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun calcScreenSize() {
        DisplayMetrics().apply {
            windowManager.defaultDisplay.getMetrics(this)
            screenSize =
                Size(
                    widthPixels.toFloat(),
                    heightPixels.toFloat()
                )
        }
    }

    companion object {
        var screenSize: Size? = null
    }
}

@Composable
fun Input() {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val mainViewModel: MainViewModel =
        viewModel(factory = MainViewModelFactory(context.applicationContext as Application))

    val currentSize by mainViewModel.size.observeAsState()
    val currentMoves by mainViewModel.moves.observeAsState()

    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
        @Composable
        fun inputField(currentValue: Int?, label: String, onChange: (String) -> Unit) {
            return TextField(
                value = currentValue?.toString() ?: "",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions { focusManager.clearFocus() },
                maxLines = 1,
                onValueChange = onChange,
                modifier = Modifier
                    .padding(all = 4.dp)
                    .weight(1f),
                label = { Text(text = label) })
        }
        inputField(currentSize, context.getString(R.string.label_size)) {
            mainViewModel.cleanBoard()
            mainViewModel.setSize(it)
        }
        inputField(currentMoves, context.getString(R.string.label_moves)) {
            mainViewModel.setMoves(it)
            mainViewModel.calculateMoves()
        }
    }
}

@Composable
fun Table() {
    val context = LocalContext.current
    val mainViewModel: MainViewModel =
        viewModel(factory = MainViewModelFactory(context.applicationContext as Application))

    val currentSize by mainViewModel.size.observeAsState()
    if (currentSize !in 6..16)
        return

    val knightPosition by mainViewModel.knightPosition.observeAsState()
    val isKnightPlaced = knightPosition != null
    val kingPosition by mainViewModel.kingPosition.observeAsState()
    val isKingPlaced = kingPosition != null

    val finalListSize = (currentSize ?: 6) + 2
    val generalBoxSize =
        MainActivity.screenSize?.width?.minus(finalListSize)?.div(finalListSize)
            ?.pixelsToDp(LocalContext.current)?.dp
            ?: 50.dp

    fun isCornerTile(rowIndex: Int, columnIndex: Int): Boolean {
        return rowIndex == 0 && columnIndex == 0
                || rowIndex == finalListSize - 1 && columnIndex == finalListSize - 1
                || rowIndex == finalListSize - 1 && columnIndex == 0
                || rowIndex == 0 && columnIndex == finalListSize - 1
    }

    fun isLetterRow(rowIndex: Int): Boolean {
        return rowIndex == 0 || rowIndex == finalListSize - 1
    }

    fun isNumberColumn(columnIndex: Int): Boolean {
        return columnIndex == 0 || columnIndex == finalListSize - 1
    }

    @Composable
    fun textBox(text: String, boxSize: Dp) {
        return Box(
            Modifier
                .background(Color.Black)
                .size(boxSize)
                .border(1.dp, Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = Color.White)
        }
    }

    LazyColumn {
        items(finalListSize, itemContent = { rowIndex ->
            LazyRow {
                items(finalListSize, itemContent = { columnIndex ->
                    val rowEven = (rowIndex) % 2 == 0
                    val columnEven = (columnIndex) % 2 == 0
                    val primaryColor = if (rowEven) Color.Gray else Color.White
                    val secondaryColor = if (rowEven) Color.White else Color.Gray

                    when {
                        isCornerTile(rowIndex, columnIndex) -> textBox("", generalBoxSize)
                        isLetterRow(rowIndex) -> textBox(
                            alphabet[columnIndex - 1].toString(),
                            generalBoxSize
                        )
                        isNumberColumn(columnIndex) -> textBox(
                            (abs(finalListSize - rowIndex - 1)).toString(),
                            generalBoxSize
                        )
                        else -> {
                            val isKnight = isKnightPlaced
                                    && knightPosition?.x == rowIndex
                                    && knightPosition?.y == columnIndex

                            val isKing = isKingPlaced
                                    && kingPosition?.x == rowIndex
                                    && kingPosition?.y == columnIndex

                            val boxColor = if (columnEven) primaryColor else secondaryColor

                            fun onTileClick() {
                                if (isKnightPlaced && isKingPlaced) {
                                    mainViewModel.cleanBoard()
                                    mainViewModel.placeKnight(rowIndex, columnIndex)
                                } else if (isKnightPlaced && !isKnight) {
                                    mainViewModel.placeKing(rowIndex, columnIndex)
                                    mainViewModel.calculateMoves()
                                } else {
                                    mainViewModel.placeKnight(rowIndex, columnIndex)
                                }
                            }

                            Box(
                                Modifier
                                    .size(generalBoxSize)
                                    .clickable(role = Role.Button) {
                                        onTileClick()
                                    }
                                    .background(boxColor)
                                    .border(1.dp, Color.Black)
                            ) {
                                if (isKnight)
                                    Image(
                                        painter = painterResource(R.drawable.white_knight),
                                        contentDescription = "Knight"
                                    )

                                if (isKing)
                                    Image(
                                        painter = painterResource(R.drawable.black_king),
                                        contentDescription = "King"
                                    )
                            }
                        }
                    }
                })
            }
        })
    }
}

@Composable
fun Result() {
    val context = LocalContext.current
    val mainViewModel: MainViewModel =
        viewModel(factory = MainViewModelFactory(context.applicationContext as Application))

    val currentResult by mainViewModel.result.observeAsState()
    val isLoading by mainViewModel.isLoading.observeAsState()
    val size = currentResult?.size ?: 0
    Surface {
        LazyColumn {
            items(count = size) {
                Row{
                    if (size > 1)
                        Text(
                            it.toString(),
                            maxLines = 1,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(8.dp)
                        )
                    Text(
                        currentResult?.get(it) ?: "",
                        maxLines = 100,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
        if (isLoading == true)
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    KnightMovesTheme {
        Column {
            Input()
            Table()
            Result()
        }
    }
}