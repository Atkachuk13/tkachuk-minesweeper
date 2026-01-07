package tkachuk.minesweeper;

import java.util.Arrays;
import java.util.Random;

public class Minesweeper
{
    private final int rows;
    private final int cols;
    private final int numBombs;
    private final int[][] board;
    private final boolean[][] revealed;
    private final boolean[][] flagged;
    private boolean gameOver;
    private boolean gameWon;
    private boolean gameStarted;

    public Minesweeper(int rows, int cols, int numBombs)
    {
        this.rows = rows;
        this.cols = cols;
        this.numBombs = numBombs;
        this.board = new int[rows][cols];
        this.revealed = new boolean[rows][cols];
        this.flagged = new boolean[rows][cols];
        this.gameOver = false;
        this.gameWon = false;
        this.gameStarted = false;
        initializeBoard();
    }

    private void initializeBoard()
    {
        // Place bombs randomly
        Random rand = new Random();
        int bombsPlaced = 0;

        while (bombsPlaced < numBombs)
        {
            int row = rand.nextInt(rows);
            int col = rand.nextInt(cols);

            if (board[row][col] != -1)
            {
                board[row][col] = -1; // -1 represents a bomb
                bombsPlaced++;
            }
        }

        // Calculate numbers for non-bomb cells
        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < cols; c++)
            {
                if (board[r][c] != -1)
                {
                    board[r][c] = countAdjacentBombs(r, c);
                }
            }
        }
    }

    private int countAdjacentBombs(int row, int col)
    {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++)
        {
            for (int dc = -1; dc <= 1; dc++)
            {
                if (dr == 0 && dc == 0)
                {
                    continue;
                }
                int nr = row + dr;
                int nc = col + dc;
                if (isValid(nr, nc) && board[nr][nc] == -1)
                {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isValid(int row, int col)
    {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public boolean reveal(int row, int col)
    {
        if (gameOver || gameWon)
        {
            return false;
        }
        if (!isValid(row, col))
        {
            return false;
        }
        if (revealed[row][col])
        {
            return false;
        }
        if (flagged[row][col])
        {
            return false;
        }

        // Start the game on first reveal
        if (!gameStarted)
        {
            gameStarted = true;
        }

        revealed[row][col] = true;

        // Hit a bomb
        if (board[row][col] == -1)
        {
            gameOver = true;
            revealAllBombs();
            return false;
        }

        // If empty cell, reveal adjacent cells
        if (board[row][col] == 0)
        {
            for (int dr = -1; dr <= 1; dr++)
            {
                for (int dc = -1; dc <= 1; dc++)
                {
                    if (dr == 0 && dc == 0)
                    {
                        continue;
                    }
                    int nr = row + dr;
                    int nc = col + dc;
                    if (isValid(nr, nc) && !revealed[nr][nc])
                    {
                        reveal(nr, nc);
                    }
                }
            }
        }

        checkWin();
        return true;
    }

    public void toggleFlag(int row, int col)
    {
        if (gameOver || gameWon)
        {
            return;
        }
        if (!isValid(row, col))
        {
            return;
        }
        if (revealed[row][col])
        {
            return;
        }

        flagged[row][col] = !flagged[row][col];
    }

    private void revealAllBombs()
    {
        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < cols; c++)
            {
                if (board[r][c] == -1)
                {
                    revealed[r][c] = true;
                }
            }
        }
    }

    private void checkWin()
    {
        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < cols; c++)
            {
                if (board[r][c] != -1 && !revealed[r][c])
                {
                    return;
                }
            }
        }
        gameWon = true;
    }

    public int getFlagCount()
    {
        int count = 0;
        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < cols; c++)
            {
                if (flagged[r][c])
                {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * AutoFlag - Place flags on all cells that we KNOW are bombs.
     * Iterates through all cells and flags hidden neighbors of satisfied numbered cells.
     */
    public void autoFlag()
    {
        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < cols; c++)
            {
                // Skip if cell is not revealed or is not a number
                if (!revealed[r][c] || board[r][c] <= 0)
                {
                    continue;
                }

                int cellValue = board[r][c];
                int hiddenCount = 0;
                int flaggedCount = 0;

                // Count hidden and flagged neighbors
                for (int dr = -1; dr <= 1; dr++)
                {
                    for (int dc = -1; dc <= 1; dc++)
                    {
                        if (dr == 0 && dc == 0)
                        {
                            continue;
                        }
                        int nr = r + dr;
                        int nc = c + dc;

                        if (isValid(nr, nc))
                        {
                            if (flagged[nr][nc])
                            {
                                flaggedCount++;
                            } else if (!revealed[nr][nc])
                            {
                                hiddenCount++;
                            }
                        }
                    }
                }

                // If cell is satisfied, flag all hidden neighbors
                if (hiddenCount + flaggedCount == cellValue)
                {
                    for (int dr = -1; dr <= 1; dr++)
                    {
                        for (int dc = -1; dc <= 1; dc++)
                        {
                            if (dr == 0 && dc == 0)
                            {
                                continue;
                            }
                            int nr = r + dr;
                            int nc = c + dc;

                            if (isValid(nr, nc) && !revealed[nr][nc] && !flagged[nr][nc])
                            {
                                flagged[nr][nc] = true;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * AutoReveal - Reveal cells that we KNOW are not bombs.
     * Iterates through all cells and reveals hidden neighbors when all bombs are flagged.
     */
    public void autoReveal()
    {
        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < cols; c++)
            {
                // Skip if cell is not revealed or is not a number
                if (!revealed[r][c] || board[r][c] <= 0)
                {
                    continue;
                }

                int cellValue = board[r][c];
                int flaggedCount = 0;

                // Count flagged neighbors
                for (int dr = -1; dr <= 1; dr++)
                {
                    for (int dc = -1; dc <= 1; dc++)
                    {
                        if (dr == 0 && dc == 0)
                        {
                            continue;
                        }
                        int nr = r + dr;
                        int nc = c + dc;

                        if (isValid(nr, nc) && flagged[nr][nc])
                        {
                            flaggedCount++;
                        }
                    }
                }

                // If all bombs are flagged, reveal all hidden neighbors
                if (flaggedCount == cellValue)
                {
                    for (int dr = -1; dr <= 1; dr++)
                    {
                        for (int dc = -1; dc <= 1; dc++)
                        {
                            if (dr == 0 && dc == 0)
                            {
                                continue;
                            }
                            int nr = r + dr;
                            int nc = c + dc;

                            if (isValid(nr, nc) && !revealed[nr][nc] && !flagged[nr][nc])
                            {
                                reveal(nr, nc);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Converts the board to a 1D array of doubles for neural network input.
     * Size = rows * cols (81 for a 9x9 board)
     * <p>
     * Values:
     * - Flagged cell: 1.0
     * - Revealed cell with number N: (N + 1) / 10.0 (0.1 for 0, 0.2 for 1, ..., 0.9 for 8)
     * - Hidden cell: 0.0
     */
    public double[] toInput()
    {
        double[] input = new double[rows * cols];
        int index = 0;

        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < cols; c++)
            {
                if (flagged[r][c])
                {
                    input[index] = 1.0;
                } else if (revealed[r][c])
                {
                    int cellValue = board[r][c];
                    // Map 0->0.1, 1->0.2, 2->0.3, ..., 8->0.9
                    if (cellValue >= 0 && cellValue <= 8)
                    {
                        input[index] = (cellValue + 1) / 10.0;
                    } else
                    {
                        input[index] = 0.0; // Bomb or invalid
                    }
                } else
                {
                    input[index] = 0.0;
                }
                index++;
            }
        }

        return input;
    }

    /**
     * Converts the board to a 1D array of doubles representing expected output.
     * Size = rows * cols (81 for a 9x9 board)
     * <p>
     * Values:
     * - Cell with bomb: 1.0
     * - Cell without bomb: 0.0
     */
    public double[] toOutput()
    {
        double[] output = new double[rows * cols];
        int index = 0;

        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < cols; c++)
            {
                if (board[r][c] == -1)
                {
                    output[index] = 1.0;
                } else
                {
                    output[index] = 0.0;
                }
                index++;
            }
        }

        return output;
    }

    /**
     * Creates a deep copy of the current Minesweeper game state.
     */
    public Minesweeper deepCopy()
    {
        Minesweeper copy = new Minesweeper(rows, cols, numBombs);

        // Copy board
        for (int r = 0; r < rows; r++)
        {
            copy.board[r] = Arrays.copyOf(this.board[r], cols);
        }

        // Copy revealed
        for (int r = 0; r < rows; r++)
        {
            copy.revealed[r] = Arrays.copyOf(this.revealed[r], cols);
        }

        // Copy flagged
        for (int r = 0; r < rows; r++)
        {
            copy.flagged[r] = Arrays.copyOf(this.flagged[r], cols);
        }

        // Copy game state
        copy.gameOver = this.gameOver;
        copy.gameWon = this.gameWon;
        copy.gameStarted = this.gameStarted;

        return copy;
    }

    // Getters
    public int getRows()
    {
        return rows;
    }

    public int getCols()
    {
        return cols;
    }

    public int getNumBombs()
    {
        return numBombs;
    }

    public int getCellValue(int row, int col)
    {
        return board[row][col];
    }

    public boolean isRevealed(int row, int col)
    {
        return revealed[row][col];
    }

    public boolean isFlagged(int row, int col)
    {
        return flagged[row][col];
    }

    public boolean isGameOver()
    {
        return gameOver;
    }

    public boolean isGameWon()
    {
        return gameWon;
    }

    public boolean isGameStarted()
    {
        return gameStarted;
    }
}
