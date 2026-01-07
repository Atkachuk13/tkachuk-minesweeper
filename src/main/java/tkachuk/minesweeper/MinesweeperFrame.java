package tkachuk.minesweeper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MinesweeperFrame extends JFrame
{
    private Minesweeper game;
    private final JButton[][] buttons;
    private final JLabel flagLabel;
    private final JLabel timerLabel;
    private final Timer timer;
    private int seconds;

    public MinesweeperFrame()
    {
        game = new Minesweeper(9, 9, 10);
        seconds = 0;

        setTitle("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel with flag counter, timer, and reset button
        JPanel topPanel = new JPanel(new BorderLayout());

        // Left side - Flags
        flagLabel = new JLabel("Flags: " + (game.getNumBombs() - game.getFlagCount()));
        flagLabel.setFont(new Font("Arial", Font.BOLD, 18));
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(flagLabel);
        topPanel.add(leftPanel, BorderLayout.WEST);

        // Center - New Game button
        JButton resetButton = new JButton("New Game");
        resetButton.addActionListener(e -> resetGame());
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.add(resetButton);
        topPanel.add(centerPanel, BorderLayout.CENTER);

        // Right side - Timer
        timerLabel = new JLabel("Time: 0s");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(timerLabel);
        topPanel.add(rightPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Auto-play buttons panel
        JPanel autoPanel = getjPanel();

        add(autoPanel, BorderLayout.SOUTH);

        // Game board panel
        JPanel boardPanel = new JPanel(new GridLayout(9, 9));
        buttons = new JButton[9][9];

        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                final int r = row;
                final int c = col;

                JButton button = new JButton();
                button.setPreferredSize(new Dimension(50, 50));
                button.setFont(new Font("Arial", Font.BOLD, 14));
                button.setFocusPainted(false);

                // Left click - reveal
                button.addActionListener(e -> handleReveal(r, c));

                // Right click - flag
                button.addMouseListener(new MouseAdapter()
                {
                    @Override
                    public void mouseClicked(MouseEvent e)
                    {
                        if (SwingUtilities.isRightMouseButton(e))
                        {
                            handleFlag(r, c);
                        }
                    }
                });

                buttons[row][col] = button;
                boardPanel.add(button);
            }
        }

        add(boardPanel, BorderLayout.CENTER);

        // Timer
        timer = new Timer(1000, e ->
        {
            seconds++;
            timerLabel.setText("Time: " + seconds + "s");
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel getjPanel()
    {
        JButton autoFlagButton = new JButton("Auto Flag");
        autoFlagButton.setFont(new Font("Arial", Font.BOLD, 14));
        autoFlagButton.addActionListener(e -> handleAutoFlag());

        JButton autoRevealButton = new JButton("Auto Reveal");
        autoRevealButton.setFont(new Font("Arial", Font.BOLD, 14));
        autoRevealButton.addActionListener(e -> handleAutoReveal());

        JPanel autoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        autoPanel.add(autoFlagButton);
        autoPanel.add(autoRevealButton);
        return autoPanel;
    }

    private void handleReveal(int row, int col)
    {
        // Don't reveal if already revealed
        if (game.isRevealed(row, col) || game.isFlagged(row, col))
        {
            return;
        }

        if (!game.isGameStarted())
        {
            timer.start();
        }

        game.reveal(row, col);
        updateBoard();

        if (game.isGameOver())
        {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Game Over! You hit a bomb!");
        } else if (game.isGameWon())
        {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Congratulations! You won!");
        }
    }

    private void handleFlag(int row, int col)
    {
        game.toggleFlag(row, col);
        updateBoard();
    }

    private void handleAutoFlag()
    {
        if (game.isGameOver() || game.isGameWon())
        {
            return;
        }

        game.autoFlag();
        updateBoard();
    }

    private void handleAutoReveal()
    {
        if (game.isGameOver() || game.isGameWon())
        {
            return;
        }

        game.autoReveal();
        updateBoard();

        if (game.isGameOver())
        {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Game Over! You hit a bomb!");
        } else if (game.isGameWon())
        {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Congratulations! You won!");
        }
    }

    private void updateBoard()
    {
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                JButton button = buttons[row][col];

                if (game.isFlagged(row, col))
                {
                    button.setText("F");
                    button.setBackground(null);
                } else if (game.isRevealed(row, col))
                {
                    int value = game.getCellValue(row, col);
                    // Don't disable - just set enabled to true but make it unresponsive
                    button.setEnabled(true);
                    button.setFocusable(false);

                    if (value == -1)
                    {
                        button.setText("B");
                        button.setBackground(new Color(255, 182, 193));
                    } else if (value == 0)
                    {
                        button.setText("");
                        button.setBackground(Color.LIGHT_GRAY);
                    } else
                    {
                        button.setText(String.valueOf(value));
                        button.setBackground(Color.LIGHT_GRAY);
                        button.setForeground(getNumberColor(value));
                    }
                } else
                {
                    button.setText("");
                    button.setEnabled(true);
                    button.setBackground(null);
                }
            }
        }

        flagLabel.setText("Flags: " + (game.getNumBombs() - game.getFlagCount()));
    }

    private Color getNumberColor(int num)
    {
        switch (num)
        {
            case 1:
                return Color.BLUE;
            case 2:
                return Color.GREEN;
            case 3:
                return Color.RED;
            case 4:
                return new Color(128, 0, 128); // Purple
            case 5:
                return new Color(128, 0, 0); // Maroon
            case 6:
                return Color.CYAN;
            case 8:
                return Color.GRAY;
            default:
                return Color.BLACK;
        }
    }

    private void resetGame()
    {
        game = new Minesweeper(9, 9, 10);
        timer.stop();
        seconds = 0;
        timerLabel.setText("Time: 0s");
        updateBoard();
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new MinesweeperFrame());
    }
}
