# knight_moves

**Important**

To run the app you will need:
* Kotlin 1.4.30 (exactly)
* Android Studio Arctic Fox | 2020.3.1 Canary 8 (at least)
* Android SDK (up to date)

I used `Kotlin`/Coroutines`, `Composable`/`ViewModel` approach without any DI or complicated architectural approaches.
The choice was made based on the project size and complexity.
There are plenty of stuff that can be improved in this app but the main tasks are achieved.
The app is focused on the Knight moves solely.

Usage:
Enter *Size* in range [6,16] default 6
Enter *Moves* in range [2,10] default 3
Place *Knight* chess piece at the board
Place *King* chess piece at the board

The calculation begins after you place *King* chess piece or after you change *Moves* if the *King* chess piece is present at the board.

To reset the board - just change the *Size* of it or tap onto the board - in case both *Knight* and *King* chess pieces are present on the board, it is cleared and new *Knight* chess piece is placed.
Continue as described above.

Thanks.