package tkachuk.minesweeper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

public class MinesweeperTest
{
    private Minesweeper game;

    @BeforeEach
    public void setUp()
    {
        game = new Minesweeper(10, 10, 10);
    }

    @Test
    public void testInitialization()
    {
        assertEquals(10, game.getRows());
        assertEquals(10, game.getCols());
        assertEquals(10, game.getNumBombs());
        assertFalse(game.isGameOver());
        assertFalse(game.isGameWon());
        assertFalse(game.isGameStarted());
    }

    @Test
    public void testBombPlacement()
    {
        int bombCount = 0;
        for (int r = 0; r < game.getRows(); r++)
        {
            for (int c = 0; c < game.getCols(); c++)
            {
                if (game.getCellValue(r, c) == -1)
                {
                    bombCount++;
                }
            }
        }
        assertEquals(10, bombCount, "Should have exactly 10 bombs");
    }

    @Test
    public void testAdjacentBombCounting()
    {
        // Find a non-bomb cell and verify its count
        for (int r = 0; r < game.getRows(); r++)
        {
            for (int c = 0; c < game.getCols(); c++)
            {
                if (game.getCellValue(r, c) != -1)
                {
                    int value = game.getCellValue(r, c);
                    assertTrue(value >= 0 && value <= 8,
                            "Cell value should be between 0 and 8");
                }
            }
        }
    }

    @Test
    public void testRevealStartsGame()
    {
        assertFalse(game.isGameStarted());

        // Find a non-bomb cell to reveal
        for (int r = 0; r < game.getRows(); r++)
        {
            for (int c = 0; c < game.getCols(); c++)
            {
                if (game.getCellValue(r, c) != -1)
                {
                    game.reveal(r, c);
                    assertTrue(game.isGameStarted());
                    return;
                }
            }
        }
    }

    @Test
    public void testRevealNonBombCell()
    {
        // Find a non-bomb cell
        for (int r = 0; r < game.getRows(); r++)
        {
            for (int c = 0; c < game.getCols(); c++)
            {
                if (game.getCellValue(r, c) != -1)
                {
                    assertTrue(game.reveal(r, c));
                    assertTrue(game.isRevealed(r, c));
                    assertFalse(game.isGameOver());
                    return;
                }
            }
        }
    }

    @Test
    public void testRevealBombCell()
    {
        // Find a bomb cell
        for (int r = 0; r < game.getRows(); r++)
        {
            for (int c = 0; c < game.getCols(); c++)
            {
                if (game.getCellValue(r, c) == -1)
                {
                    assertFalse(game.reveal(r, c));
                    assertTrue(game.isGameOver());
                    assertTrue(game.isRevealed(r, c));
                    return;
                }
            }
        }
    }

    @Test
    public void testRevealAllBombsOnGameOver()
    {
        // Reveal a bomb
        for (int r = 0; r < game.getRows(); r++)
        {
            for (int c = 0; c < game.getCols(); c++)
            {
                if (game.getCellValue(r, c) == -1)
                {
                    game.reveal(r, c);
                    break;
                }
            }
            if (game.isGameOver())
            {
                break;
            }
        }

        // Check all bombs are revealed
        for (int r = 0; r < game.getRows(); r++)
        {
            for (int c = 0; c < game.getCols(); c++)
            {
                if (game.getCellValue(r, c) == -1)
                {
                    assertTrue(game.isRevealed(r, c),
                            "All bombs should be revealed after game over");
                }
            }
        }
    }

    @Test
    public void testToggleFlag()
    {
        assertFalse(game.isFlagged(0, 0));
        game.toggleFlag(0, 0);
        assertTrue(game.isFlagged(0, 0));
        game.toggleFlag(0, 0);
        assertFalse(game.isFlagged(0, 0));
    }

    @Test
    public void testCannotFlagRevealedCell()
    {
        // Find and reveal a non-bomb cell
        for (int r = 0; r < game.getRows(); r++)
        {
            for (int c = 0; c < game.getCols(); c++)
            {
                if (game.getCellValue(r, c) != -1)
                {
                    game.reveal(r, c);
                    game.toggleFlag(r, c);
                    assertFalse(game.isFlagged(r, c),
                            "Cannot flag a revealed cell");
                    return;
                }
            }
        }
    }

    @Test
    public void testCannotRevealFlaggedCell()
    {
        game.toggleFlag(0, 0);
        boolean revealed = game.reveal(0, 0);
        assertFalse(game.isRevealed(0, 0),
                "Flagged cell should not be revealed");
    }

    @Test
    public void testFlagCount()
    {
        assertEquals(0, game.getFlagCount());
        game.toggleFlag(0, 0);
        game.toggleFlag(1, 1);
        game.toggleFlag(2, 2);
        assertEquals(3, game.getFlagCount());
        game.toggleFlag(0, 0);
        assertEquals(2, game.getFlagCount());
    }

    @Test
    public void testWinCondition()
    {
        // Create a small predictable game
        Minesweeper smallGame = new Minesweeper(3, 3, 1);

        // Reveal all non-bomb cells
        for (int r = 0; r < smallGame.getRows(); r++)
        {
            for (int c = 0; c < smallGame.getCols(); c++)
            {
                if (smallGame.getCellValue(r, c) != -1)
                {
                    smallGame.reveal(r, c);
                }
            }
        }

        assertTrue(smallGame.isGameWon(), "Game should be won");
    }

    @Test
    public void testCannotPlayAfterGameOver()
    {
        // Find and reveal a bomb
        for (int r = 0; r < game.getRows(); r++)
        {
            for (int c = 0; c < game.getCols(); c++)
            {
                if (game.getCellValue(r, c) == -1)
                {
                    game.reveal(r, c);
                    assertTrue(game.isGameOver());

                    // Try to make more moves
                    assertFalse(game.reveal(0, 0));
                    game.toggleFlag(1, 1);
                    assertFalse(game.isFlagged(1, 1));
                    return;
                }
            }
        }
    }

    @Test
    public void testCannotPlayAfterWin()
    {
        Minesweeper smallGame = new Minesweeper(3, 3, 1);

        // Win the game
        for (int r = 0; r < smallGame.getRows(); r++)
        {
            for (int c = 0; c < smallGame.getCols(); c++)
            {
                if (smallGame.getCellValue(r, c) != -1)
                {
                    smallGame.reveal(r, c);
                }
            }
        }

        assertTrue(smallGame.isGameWon());

        // Try to make more moves
        assertFalse(smallGame.reveal(0, 0));
        smallGame.toggleFlag(1, 1);
        assertFalse(smallGame.isFlagged(1, 1));
    }

    @Test
    public void testInvalidCellOperations()
    {
        assertFalse(game.reveal(-1, 0));
        assertFalse(game.reveal(0, -1));
        assertFalse(game.reveal(100, 0));
        assertFalse(game.reveal(0, 100));

        // toggleFlag doesn't return a value, so just ensure no exception
        game.toggleFlag(-1, 0);
        game.toggleFlag(100, 100);
    }

    @Test
    public void testEmptyCellCascadeReveal()
    {
        // This test verifies that revealing a cell with 0 adjacent bombs
        // cascades to adjacent cells
        Minesweeper testGame = new Minesweeper(5, 5, 1);

        // Find a cell with value 0
        boolean foundEmpty = false;
        for (int r = 0; r < testGame.getRows(); r++)
        {
            for (int c = 0; c < testGame.getCols(); c++)
            {
                if (testGame.getCellValue(r, c) == 0)
                {
                    testGame.reveal(r, c);
                    foundEmpty = true;

                    // Check that adjacent cells were also revealed
                    int revealedCount = 0;
                    for (int i = 0; i < testGame.getRows(); i++)
                    {
                        for (int j = 0; j < testGame.getCols(); j++)
                        {
                            if (testGame.isRevealed(i, j))
                            {
                                revealedCount++;
                            }
                        }
                    }

                    assertTrue(revealedCount > 1,
                            "Revealing empty cell should cascade to adjacent cells");
                    return;
                }
            }
        }
    }
}
