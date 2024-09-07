import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Welcome to Minesweeper!");
            System.out.println("Select difficulty:");
            System.out.println("1. Easy (9x9, 10 mines)");
            System.out.println("2. Medium (16x16, 40 mines)");
            System.out.println("3. Hard (16x30, 99 mines)");
            System.out.println("4. Custom");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            if (choice == 1) {
                new Board(9, 9, 10, scanner);
            } else if (choice == 2) {
                new Board(16, 16, 40, scanner);
            } else if (choice == 3) {
                new Board(16, 30, 99, scanner);
            } else if (choice == 4) { System.out.print("Enter number of rows: ");
                int rows = scanner.nextInt();
                System.out.print("Enter number of columns: ");
                int cols = scanner.nextInt();
                System.out.print("Enter number of mines: ");
                int mines = scanner.nextInt();
                new Board(rows, cols, mines, scanner);
            } else {
                System.out.println("Invalid choice");
            }
            System.out.print("Do you want to play again? (y/n): ");
            char playAgain = scanner.next().charAt(0);
            if (playAgain == 'n') {
                break;
            }
        }

    }
}

class Tile {
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
}

class Board {
    Tile[][] board;
    int rows;
    int cols;
    int mines;


    Board(int rows, int cols, int mines, Scanner scanner) {
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
        board = new Tile[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j] = new Tile();
            }
        }
        printBoard();
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
            printBoard();
            System.out.print("Enter row (a-" + ((char) ('a' + rows - 1)) + "): ");
            char rowChar = scanner.next().charAt(0);
            int row = rowChar - 'a';
            System.out.print("Enter column: ");
            int col = scanner.nextInt();
            System.out.print("Enter action (r - reveal, f - flag): ");
            char action = scanner.next().charAt(0);
            if (action == 'r') {
                clickTile(row, col);
                if (isGameWon()) {
                    endGameBoard();
                    System.out.println("You won!");
                    break;
                }
                if (isGameLost()) {
                    endGameBoard();
                    System.out.println("You lost!");
                    break;
                }
            } else if (action == 'f') {
                flagTile(row, col);
            }
        }
    }

    void endGameBoard() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j].isRevealed = true;
            }
            printBoard();
        }
    }

    void printBoard() {
        System.out.print("  ");
        for (int i = 0; i < cols; i++) {
            System.out.print(i + " ");
        }
        System.out.println();
        for (int i = 0; i < rows; i++) {
            System.out.print((char) ('a' + i) + " ");
            for (int j = 0; j < cols; j++) {
                if (board[i][j].isRevealed) {
                    if (board[i][j].isMine) {
                        System.out.print("X ");
                    } else {
                        if (board[i][j].minesNearby == 0) {
                            System.out.print("  ");
                        } else {
                            System.out.print(board[i][j].minesNearby + " ");
                        }
                    }
                } else if (board[i][j].isFlagged) {
                    System.out.print("F ");
                } else {
                    System.out.print("# ");
                }
            }
            System.out.println();
        }
    }

    void placeMines(int mines, int row, int col) {
        int minesPlaced = 0;
        while (minesPlaced < mines) {
            int randRow = (int) (Math.random() * rows);
            int randCol = (int) (Math.random() * cols);
            if (!(randRow == row && randCol == col) && !board[randRow][randCol].isMine) {
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
        board[row][col].isFlagged = !board[row][col].isFlagged;
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