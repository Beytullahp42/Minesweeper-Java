import java.util.Scanner;

class Minesweeper {
    public void PlayTheGame() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Welcome to Minesweeper! \uD83D\uDEA9");
            System.out.println("Are you able to see emojis? (y/n): ");
            char emojiChar = scanner.next().charAt(0);
            boolean emoji = emojiChar == 'y';
            System.out.println("Select difficulty:");
            System.out.println("1. Easy (9x9, 10 mines)");
            System.out.println("2. Medium (16x16, 40 mines)");
            System.out.println("3. Hard (16x30, 99 mines)");
            System.out.println("4. Custom");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            if (choice == 1) {
                new Board(9, 9, 10, emoji, scanner);
            } else if (choice == 2) {
                new Board(16, 16, 40, emoji, scanner);
            } else if (choice == 3) {
                new Board(16, 30, 99, emoji, scanner);
            } else if (choice == 4) {
                System.out.print("Enter number of rows: (min 8, max 30) ");
                int rows = scanner.nextInt();
                if (rows > 30) {
                    rows = 30;
                }
                if (rows < 8) {
                    rows = 8;
                }
                System.out.print("Enter number of columns: (max 30) ");
                int cols = scanner.nextInt();
                if (cols > 30) {
                    cols = 30;
                }
                if (cols < 8) {
                    cols = 8;
                }
                System.out.print("Enter number of mines: min " + rows * cols / 10 + ", max " + rows * cols * 7 / 10 + " ");
                int mines = scanner.nextInt();
                if (mines > rows * cols * 7 / 10) {
                    mines = rows * cols * 7 / 10;
                }
                if (mines < rows * cols / 10) {
                    mines = rows * cols / 10;
                }
                new Board(rows, cols, mines, emoji, scanner);
            } else {
                System.out.println("Invalid choice");
            }
            System.out.print("Do you want to play again? (y/n): ");
            char playAgain = scanner.next().charAt(0);
            if (playAgain == 'n') {
                break;
            }
        }
        System.out.println("Thanks for playing! \uD83D\uDEA9");
    }


    static class Tile {
        boolean isMine;
        boolean isFlagged;
        boolean isRevealed;
        int minesNearby;

        Tile() {
            isMine = false;
            isFlagged = false;
            isRevealed = false;
            minesNearby = 0;
        }

        char getTileChar(boolean isGame) {
            if (isGame) {
                if (isRevealed) {
                    if (isMine) {
                        return '*';
                    } else if (minesNearby == 0) {
                        return ' ';
                    } else {
                        return (char) ('0' + minesNearby);
                    }
                } else {
                    if (isFlagged) {
                        return 'F';
                    } else {
                        return '#';
                    }
                }
            } else {
                if (isMine && isFlagged) {
                    return 'F';
                } else if (!isMine && isFlagged) {
                    return 'X';
                } else if (isMine && !isRevealed) {
                    return '*';
                } else if (isRevealed) {
                    if (isMine) {
                        return '*';
                    } else if (minesNearby == 0) {
                        return ' ';
                    } else {
                        return (char) ('0' + minesNearby);
                    }
                } else {
                    return '#';
                }
            }
        }

        String EmojiTheme(boolean isAble, char c) {
            String symbol;
            if (!isAble) {
                symbol = " " + c + " ";
            } else {
                symbol = switch (c) {
                    case 'F' -> "\uD83D\uDEA9"; // flag
                    case 'X' -> "❌"; // wrong flag
                    case '*' -> "\uD83D\uDCA3"; // mine
                    case '#' -> "\uD83D\uDFE6"; // hidden
                    default -> String.valueOf(c);
                };
            }
            return String.format("%-3s", String.format("%2s", symbol));
        }
    }

    static class Board {
        Tile[][] board;
        int rows;
        int cols;
        int mines;
        boolean emoji;
        int flags;


        Board(int rows, int cols, int mines, boolean emoji, Scanner scanner) {
            this.rows = rows;
            this.cols = cols;
            this.mines = mines;
            this.emoji = emoji;
            this.flags = 0;
            board = new Tile[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    board[i][j] = new Tile();
                }
            }
            printBoard(false);
            // Make player do the first move
            System.out.println("First move is always safe");
            System.out.print("Enter row (a-" + ((char) ('a' + rows - 1)) + "): ");
            char rowChar = scanner.next().charAt(0);
            int row = rowChar - 'a';
            System.out.print("Enter column: ");
            int col = scanner.nextInt();
            placeMines(mines, row, col);
            revealTile(row, col);
            playGame(scanner);
        }


        void playGame(Scanner scanner) {
            while (true) {
                printBoard(false);
                System.out.println("Remaining mines: " + (mines - flags));
                System.out.print("Enter row (a-" + ((char) ('a' + rows - 1)) + "): ");
                char rowChar = scanner.next().charAt(0);
                int row = rowChar - 'a';
                System.out.print("Enter column: ");
                int col;
                try {
                    col = scanner.nextInt();
                } catch (Exception e) {
                    System.out.println("Invalid input");
                    continue;
                }
                System.out.print("Enter action (r - reveal, f - flag, random key - cancel): ");
                char action = scanner.next().charAt(0);
                if (action == 'r') {
                    clickTile(row, col);
                    if (isGameWon()) {
                        printBoard(true);
                        System.out.println("You won!");
                        break;
                    }
                    if (isGameLost()) {
                        printBoard(true);
                        System.out.println("You lost!");
                        break;
                    }
                } else if (action == 'f') {
                    flagTile(row, col);
                }
            }
        }


        void printBoard(boolean isGameOver) {
            System.out.print("   ");
            for (int i = 0; i < cols; i++) {
                System.out.printf("|%2d ", i);
            }
            System.out.println("|");
            for (int i = 0; i < rows; i++) {
                System.out.printf("%2c ", (char) ('a' + i));
                for (int j = 0; j < cols; j++) {
                    System.out.printf("|%s", board[i][j].EmojiTheme(this.emoji, board[i][j].getTileChar(!isGameOver)));
                }
                System.out.println("|");
                for (int k = 0; k < cols + 1; k++) {
                    System.out.print("---+");
                }
                System.out.println();
            }
        }

        boolean isValid(int row, int col, int randRow, int randCol) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (randRow + i == row && randCol + j == col) {
                        return false;
                    }
                }
            }
            return true;
        }

        void placeMines(int mines, int row, int col) {
            int minesPlaced = 0;
            while (minesPlaced < mines) {
                int randRow = (int) (Math.random() * rows);
                int randCol = (int) (Math.random() * cols);
                if (isValid(row, col, randRow, randCol) && !board[randRow][randCol].isMine) {
                    board[randRow][randCol].isMine = true;
                    minesPlaced++;
                    board[randRow][randCol].minesNearby = -1;
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            if (randRow + i >= 0 && randRow + i < rows && randCol + j >= 0 && randCol + j < cols) {
                                board[randRow + i][randCol + j].minesNearby++;
                            }
                        }
                    }
                }
            }
        }

        void clickTile(int row, int col) {
            int nearbyFlags = 0;
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (row + i >= 0 && row + i < rows && col + j >= 0 && col + j < cols) {
                        if (board[row + i][col + j].isFlagged) {
                            nearbyFlags++;
                        }
                    }
                }
            }
            if (nearbyFlags == board[row][col].minesNearby && board[row][col].isRevealed) {
                revealSurroundingTiles(row, col);
            } else if (!board[row][col].isRevealed) {
                revealTile(row, col);
            } else {
                System.out.println("Not enough flags nearby");
            }
        }

        void revealTile(int row, int col) {
            if (row < 0 || row >= rows || col < 0 || col >= cols) {
                return;
            }
            if (board[row][col].isRevealed || board[row][col].isFlagged) {
                return;
            }
            board[row][col].isRevealed = true;
            if (board[row][col].minesNearby == 0) {
                revealSurroundingTiles(row, col);
            }
        }

        private void revealSurroundingTiles(int row, int col) {
            revealTile(row - 1, col - 1);
            revealTile(row - 1, col);
            revealTile(row - 1, col + 1);
            revealTile(row, col - 1);
            revealTile(row, col + 1);
            revealTile(row + 1, col - 1);
            revealTile(row + 1, col);
            revealTile(row + 1, col + 1);
        }

        void flagTile(int row, int col) {
            if (row < 0 || row >= rows || col < 0 || col >= cols) {
                return;
            }
            if (board[row][col].isRevealed) {
                return;
            }
            if (board[row][col].isFlagged) {
                board[row][col].isFlagged = false;
                flags--;
            } else {
                board[row][col].isFlagged = true;
                flags++;
            }
        }

        boolean isGameWon() {
            int unrevealedTiles = 0;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (!board[i][j].isRevealed && !board[i][j].isMine) {
                        unrevealedTiles++;
                    }
                }
            }
            return unrevealedTiles == 0;
        }

        boolean isGameLost() {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (board[i][j].isRevealed && board[i][j].isMine) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
