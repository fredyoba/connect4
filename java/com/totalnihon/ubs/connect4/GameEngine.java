package com.totalnihon.ubs.connect4;

import java.util.Arrays;

import org.springframework.context.annotation.Configuration;

import com.totalnihon.ubs.connect4.infrastructure.exception.ColumnIsFullException;
import com.totalnihon.ubs.connect4.infrastructure.exception.ColumnOutOfBoudariesException;


@Configuration
public class GameEngine {
	final public static String RED = "RED";
	final public static String YELLOW = "YELLOW";
	final public static String PLAYERA = "PLAYER A";
	final public static String PLAYERB = "PLAYER B";
	final public static String NOBODY = "NOBODY";
	
	/** it's a 2 players game, this holds which player turns it is true PLAYER A, false PLAYER B 
	 * playerA always starts */
	 private boolean turn = true;

	/** are the 3 states of a cell in the board EMPTY is the initial value, then PLAYERA or PLAYERB
	 * depending on which player turn it is */
	public enum STATE  {EMPTY, PLAYERA, PLAYERB};
	
	/** board keeps the state of the game */
	private STATE board[];
	
	/** number of columns on the board */
	final public static short COLUMNS = 7;
	
	/** number of rows in the column */
	final public static short ROWS = 6;
	
	/** number of straight disc to be aligned */
	public static int MINIMUM_ALIGN_COUNT = 4;
	
	final public static String COLUMN_BOUNDARIES = "Column value (%d) is outside boudaries only possible within [1-%d]";
	final public static String COLUMN_FULL = "Column (%d) is full";
	
	/** each disc inserted in a column, increases the row at witch next disc will be inserted */
	private int rowLevel[];
	
	/** indicate if the game has finished with a winner */
	private boolean hasWinner;
	
	/** count remaining empty disc */
	private short remainingEmptyDisc;
	
	/** hold the winner or empty if none */
	private STATE winner = STATE.EMPTY;
	
	public GameEngine() {
		startNewGame();
	}
	
	/** initialize the game engine */
	public void startNewGame() {		
		// game is obviously not finished
		hasWinner = false;		
		
		// number of empty disk, is the number of cells in the board
		remainingEmptyDisc = COLUMNS * ROWS;
		
		// init the row level for each column, it is 0 for all as no disc inserted yet
		rowLevel = new int[COLUMNS];
		Arrays.fill(rowLevel, 0);
		
		// init the board, by default all column have empty state
		board = new STATE[COLUMNS*ROWS];
		Arrays.fill(board, STATE.EMPTY);
	}

	/** gets which user turn 
	 * @return true for player A, false for player B */
	public boolean getTurn() {
		return(turn);
	}
	
	/** It's setting the next player turn */
	private void toggleTurn() {
		turn=!turn;
	}
	
	/** this is called by the player to cast his play into the board
	 * @param turn to indicate which player turn is it 
	 * @column to indicate in which column the disc has been thrown
	 * 
	 * @throws ColumnOutOfBoudariesException if the column chosen is outside boundaries
	 * @throws ColumnIsFullException if the column chosen has no room for insertion
	 * */
	public void play(boolean turn, int pColumn) throws ColumnIsFullException, ColumnOutOfBoudariesException {
		// chift column value to index column
		int column = pColumn -1;
		
		// mark the cell with the player value
		board[getAbsoluteIndex(column)] = (turn? STATE.PLAYERA:STATE.PLAYERB);
			
			
		// check if this play makes it to win
		if(checkWin(column)) {
			hasWinner = true; // there was a winner, we mark the game as won
			winner = (turn? STATE.PLAYERA:STATE.PLAYERB); // we set the winner, which is the one who played last
		}
			
		// increase the row level for this column, next disc in same column will be in higher row if not full
		rowLevel[column]++;

		// set turn of the player
		toggleTurn();
			
		// decrease number of empty disc, game finishes when it reaches 0
		remainingEmptyDisc--;
	}
	
	/** compute absolute index from the column index. In theory we also need the row number, but this information 
	 * can be deducted from the rowlLevel array */
	private int getAbsoluteIndex(int column) throws ColumnIsFullException, ColumnOutOfBoudariesException {
		int row;
		// we check we are within boundaries
		if(!(column >= 0 && column < COLUMNS)) throw new ColumnOutOfBoudariesException(String.format(COLUMN_BOUNDARIES, column+1, COLUMNS));
		
		// get the current row level for this column, which is the last play disc position
		row = rowLevel[column];
		
		// same check the disc is within boundaries
		if(!(row >= 0 && row < ROWS)) throw new ColumnIsFullException(String.format(COLUMN_FULL, column+1));

		return(row * COLUMNS + column);
	}
	
	
	/** check if there is a win 
	 * Since it's a turn by turn game, we can check if a win has happened after a disc was casted.
	 * In this case, the check can start from the last disc inserted position on the board, then we count the number of consecutive
	 * disc of same colors in lines, that means
	 * - vertically : so we count from the disc upward, then downward, there is a win if the number of disc are above 3
	 * - horizontally : similar to above but the direction is left and right
	 * - diagonals: bottom-left and top-right direction
	 * - diagonals: top-left and bottom-right 
	 * 
	 *  @param column in which the disk was inserted
	 *  @return true yes it's a win, false no, the game continue
	 *  */
	private boolean checkWin(int column) {
		int consecutiveDiscs = 0;
		int row;
		STATE value;
		
		// we check we are within boundaries
		if(!(column >= 0 && column < COLUMNS)) return(false);
		
		// get the current row level for this column, which is the last play disc position
		row = rowLevel[column];
		
		// same check the disc is within boundaries
		if(!(row >= 0 && row < ROWS)) return(false);
		
		// get the value of the cell
		value = board[column + COLUMNS*row];
		
		// count horizontal alignment
		consecutiveDiscs = 1;
		for(int i=column-1; i >= 0 && board[i + COLUMNS*row] == value; --i) consecutiveDiscs++;
		for(int i=column+1; i < COLUMNS && board[i + COLUMNS*row] == value; ++i) consecutiveDiscs++;
		if(consecutiveDiscs >=MINIMUM_ALIGN_COUNT) return(true);

		// count vertical alignment
		consecutiveDiscs = 1;
		for(int i=row-1; i >= 0 && board[column + COLUMNS*i] == value; --i) consecutiveDiscs++;
		if(consecutiveDiscs >=MINIMUM_ALIGN_COUNT) return(true);

		// count diagonal bottom-left to top-right
		consecutiveDiscs = 1;
		for(int i=column-1,j=row-1; i >= 0 && j >= 0 && board[i + COLUMNS*j] == value; --i,--j) consecutiveDiscs++;
		for(int i=column+1,j=row+1; i < COLUMNS && j < ROWS && board[i + COLUMNS*j] == value; ++i,++j) consecutiveDiscs++;
		if(consecutiveDiscs >=MINIMUM_ALIGN_COUNT) return(true);

		// count diagonal top-left to bottom-right
		consecutiveDiscs = 1;
		for(int i=column-1,j=row+1; i >= 0 && j < ROWS && board[i + COLUMNS*j] == value; --i,++j) consecutiveDiscs++;
		for(int i=column+1,j=row-1; i < COLUMNS && j >= 0 && board[i + COLUMNS*j] == value; ++i,--j) consecutiveDiscs++;
		if(consecutiveDiscs >=MINIMUM_ALIGN_COUNT) return(true);

		return(false);
	}
	
	/** call this function to know if the game is finished, a game is finished if no more room to place a disc
	 * or there have bee a winner already.
	 * @return true if game finished, false otherwise */
	public boolean isGameFinished() {
		return(remainingEmptyDisc <= 0 || hasWinner);
	}
	
	/** handy procedure to display text of the outcome of the game, call this function to know current state of the game
	 * whether player A wins or player B wins or nobody */
	public String getWinner() {
		return((winner == STATE.PLAYERA? PLAYERA:winner == STATE.PLAYERB? PLAYERB: NOBODY) + " wins");
	}
	
	/** traverse the board from end to beginning to display the content */
	public void printBoard() {
		int column, row;
		STATE state;

		// we could just display matter of choice, we put all in a stringbuilder
		StringBuilder sb = new StringBuilder();
		for(int i=0; i < board.length/ROWS; i++)
			sb.append(" ").append(i+1);
		
		sb.append("\n");
		
		for(int i=0; i < board.length; ++i) {
			// because we display from the top of the board to the bottom, we need to shift the index to start from top row 
			row = ROWS - (i / COLUMNS) - 1;
			
			// column from left to right, regardless of the index i, the column falls between 0..COLUMNS-1
			// the index i is a multiple of ROWS + column. ie: row x COLUMNS + column
			column = i%COLUMNS;
		
			// we gat the value in that cell
			state = board[row*COLUMNS+column];
			
			// and append in the buffer
			sb.append("|").append(state == STATE.PLAYERA? 'R':state == STATE.PLAYERB? 'Y': ' ');
			
			// handle the case, we reach the end of the ROW, we break the line
			if((i+1)%COLUMNS == 0) 
				sb.append("|\n");
		}
		
		// finally print all
		System.out.println(sb.toString());
	}
}
