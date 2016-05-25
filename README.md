# jotto_solver
Solver for the game jotto.

Java implementation; compile to a jar file and execute under the JRE.

## Game rules
Jotto is a game between two players. One person begins by thinking of a secret word and telling their opponent the number of letters in the word. The secret word must be a word that would be expected to appear in a dictionary.

The other player then guesses a word that fits the constraints known so far. At this point, the only constraint is that the word is N letters long, so the player must guess an N-letter word.

With this guess, the player with the secret word responds with the number of letters that the guessed word and the secret word share.
That is,

```
response = secret_word.where(letter => guessed_word.contains(letter)).count()
```

With this information, the guesser is able to reduce the set of possible solutions. With repeated guesses and responses, the guesser should be able to narrow in on the actual answer.

The goal is for the guesser to achieve this with the fewest possible guesses.

## The solver

The solver program uses an algorithm that determines the best next guess that maximizes information entropy. 

Let's play through an example game. Player 1 thinks of the secret word - **BROCCOLI**, and tells Player 2 that their secret word is 8 letters long. (Note that playing this game with 8 letters is _extremely_ difficult for Player 2.)

Player 2 enters 8 as the number of letters and asks the program for the next best guess, which is **POSTURED**. Note that there are about 28,000 possible answers at this point.

// TODO: img

The number of letters in BROCCOLI that also appear in POSTURED is 3 (R, O, O), so Player 1 responds "3".

Player 2 enters this new information into the solver and asks for the next best guess, which is **GUNSMITH**.

// TODO: img

Player 1 responds with "1" (I). Player 2 again updates the solver with the new information, and now guesses the next best word: **BREEZILY**.

// TODO: img

Player 1 responds with "4" (B, R, I, L). Player 2 guesses **SCABBLED**. Frankly, that is not likely the word that Player 1 chose (because who thinks of the word "scabbled"?), but it _is_ the one which narrows down the possible result set as much as possible.

// TODO: img

Player 1 responds with "4" (B, C, C, L). Player 2 again updates the solver with the new information, and now guesses the next best word: **BLOWHOLE**. And hey! There's BROCCOLI off in the list on the right side! We're getting close...

// TODO: img

Player 1 responds with "4" (B, O, O, L). Player 2 updates things again, and the list is now down to 5 words! At this point, you could probably guess the _actual_ word because only one of them is really a common word. But hey - who knows how tricky the other player is being! Player 2 guesses **AWFULLER** as the solver suggests.

// TODO: img

Player 1 responds with "2" (R, L). Player 2 updates things, and has now officially eliminated all possible words other than **BROCCOLI**! Success!

