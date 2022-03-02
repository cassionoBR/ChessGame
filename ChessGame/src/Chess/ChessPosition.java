package Chess;

import BoardGame.Position;

public class ChessPosition {

	private char column;
	private int row;
	
	public ChessPosition(char column, int row) {
		if(column < 'a' || column > 'h' || row < 1 || row > 8) {
			throw new ChessException("Error instatiating chessPosition. Valid values are from A, 1 to H, 8");
		}
		this.column = column;
		this.row = row;
	}

	public char getColumn() {
		return column;
	}

	public int getRow() {
		return row;
	}
	
	protected Position toPosition() {
		int matrix_column = column - 'a';
		int matrix_row = 8 - row;
		return new Position(matrix_row, matrix_column);
	}

	protected static ChessPosition fromPosition(Position position) {
		return new ChessPosition((char)('a' + position.getColumn()),8 - position.getRow());
	}
	
	@Override
	public String toString() {
		return "" + column + row;
	}
	
}
