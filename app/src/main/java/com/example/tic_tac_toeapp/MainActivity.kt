package com.example.tic_tac_toeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tic_tac_toeapp.ui.theme.TicTacToeAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TicTacToeAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TicTacToeGame()
                }
            }
        }
    }
}

@Composable
fun TicTacToeGame() {
    var board by remember { mutableStateOf(List(9) { "" }) }
    var currentPlayer by remember { mutableStateOf("X") }
    var winner by remember { mutableStateOf<String?>(null) }
    var singlePlayer by remember { mutableStateOf(true) }
    var difficulty by remember { mutableStateOf("Easy") }
    var winningCells by remember { mutableStateOf(listOf<Int>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "✨ Tic Tac Toe ✨",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // Mode Selection
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Button(onClick = { singlePlayer = true }) {
                Text("Vs Computer")
            }
            Button(onClick = { singlePlayer = false }) {
                Text("Vs Friend")
            }
        }

        // Difficulty for computer
        if (singlePlayer) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { difficulty = "Easy" }) { Text("Easy") }
                Button(onClick = { difficulty = "Medium" }) { Text("Medium") }
                Button(onClick = { difficulty = "Hard" }) { Text("Hard") }
            }
            Text("Difficulty: $difficulty")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Game Board
        for (i in 0..2) {
            Row {
                for (j in 0..2) {
                    val index = i * 3 + j
                    val cellValue = board[index]

                    // Animate cell background
                    val bgColor by animateColorAsState(
                        targetValue = when {
                            index in winningCells -> Color(0xFF81C784) // Green for winning
                            cellValue == "X" -> Color(0xFF64B5F6) // Blue for X
                            cellValue == "O" -> Color(0xFFE57373) // Red for O
                            else -> Color.LightGray
                        }, label = ""
                    )

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .padding(4.dp)
                            .background(bgColor, RoundedCornerShape(12.dp))
                            .clickable(enabled = board[index].isEmpty() && winner == null) {
                                if (board[index].isEmpty()) {
                                    board = board.toMutableList().also { it[index] = currentPlayer }
                                    val result = checkWinner(board)
                                    winner = result.first
                                    winningCells = result.second

                                    if (winner == null) {
                                        currentPlayer = if (currentPlayer == "X") "O" else "X"
                                        if (singlePlayer && currentPlayer == "O") {
                                            val move = computerMove(board, difficulty)
                                            if (move != -1) {
                                                board = board.toMutableList().also { it[move] = "O" }
                                                val result2 = checkWinner(board)
                                                winner = result2.first
                                                winningCells = result2.second
                                                currentPlayer = "X"
                                            }
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cellValue,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reset Button
        Button(onClick = {
            board = List(9) { "" }
            currentPlayer = "X"
            winner = null
            winningCells = emptyList()
        }) {
            Text("🔄 Reset Game")
        }

        // Winner Popup
        if (winner != null || board.all { it.isNotEmpty() }) {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text(
                        text = when (winner) {
                            "X" -> "🎉 Player X Wins!"
                            "O" -> if (singlePlayer) "🤖 Computer Wins!" else "🎉 Player O Wins!"
                            else -> "🤝 It's a Draw!"
                        }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        board = List(9) { "" }
                        currentPlayer = "X"
                        winner = null
                        winningCells = emptyList()
                    }) {
                        Text("Play Again")
                    }
                }
            )
        }
    }
}

fun checkWinner(board: List<String>): Pair<String?, List<Int>> {
    val wins = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // rows
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // cols
        listOf(0, 4, 8), listOf(2, 4, 6)                   // diagonals
    )

    for (w in wins) {
        val a = w[0]
        val b = w[1]
        val c = w[2]
        if (board[a].isNotEmpty() && board[a] == board[b] && board[b] == board[c]) {
            return board[a] to w
        }
    }
    return null to emptyList()
}

// Computer AI same as before
fun computerMove(board: List<String>, difficulty: String): Int {
    val empty = board.mapIndexed { i, v -> if (v.isEmpty()) i else -1 }.filter { it != -1 }
    if (empty.isEmpty()) return -1

    return when (difficulty) {
        "Easy" -> empty.random()
        "Medium" -> {
            for (i in empty) {
                val copy = board.toMutableList()
                copy[i] = "O"
                if (checkWinner(copy).first == "O") return i
            }
            for (i in empty) {
                val copy = board.toMutableList()
                copy[i] = "X"
                if (checkWinner(copy).first == "X") return i
            }
            empty.random()
        }
        "Hard" -> minimax(board, true).second
        else -> empty.random()
    }
}

fun minimax(board: List<String>, isMax: Boolean): Pair<Int, Int> {
    val result = checkWinner(board)
    val winner = result.first
    if (winner == "O") return 10 to -1
    if (winner == "X") return -10 to -1
    if (board.all { it.isNotEmpty() }) return 0 to -1

    val empty = board.mapIndexed { i, v -> if (v.isEmpty()) i else -1 }.filter { it != -1 }
    var bestScore = if (isMax) Int.MIN_VALUE else Int.MAX_VALUE
    var bestMove = -1

    for (i in empty) {
        val copy = board.toMutableList()
        copy[i] = if (isMax) "O" else "X"
        val score = minimax(copy, !isMax).first
        if (isMax) {
            if (score > bestScore) {
                bestScore = score
                bestMove = i
            }
        } else {
            if (score < bestScore) {
                bestScore = score
                bestMove = i
            }
        }
    }
    return bestScore to bestMove
}

@Preview(showBackground = true)
@Composable
fun GamePreview() {
    TicTacToeAppTheme {
        TicTacToeGame()
    }
}
