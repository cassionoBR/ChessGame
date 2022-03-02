package Chess.pieces;

import BoardGame.Board;
import BoardGame.Position;
import Chess.ChessMatch;
import Chess.ChessPiece;
import Chess.Color;

public class Pawn extends ChessPiece {

	private ChessMatch chessMatch;
	
	public Pawn(Board board, Color color, ChessMatch chessMatch) {
		super(board, color);
		this.chessMatch = chessMatch;
	}
	
	@Override
	public String toString() {
		return "P";
	}

	@Override
	public boolean[][] possibleMoves() {
		boolean[][] mat = new boolean[getBoard().getRows()][getBoard().getColumns()];
		Position p = new Position(0, 0);
		
		if(getColor() == Color.WHITE) {
			p.setValues(position.getRow() - 1, position.getColumn());
			if(getBoard().PositionExists(p) && !getBoard().thereIsAPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}
			p.setValues(position.getRow() - 2, position.getColumn());
			Position p2 = new Position(position.getRow() - 1, position.getColumn());
			if(getBoard().PositionExists(p) 
					&& !getBoard().thereIsAPiece(p)
					&& getBoard().PositionExists(p2) 
					&& !getBoard().thereIsAPiece(p2)
					&& getMoveCount() == 0) {
				mat[p.getRow()][p.getColumn()] = true;
			}
			p.setValues(position.getRow() - 1, position.getColumn() - 1);
			if(getBoard().PositionExists(p) && isThereOponnetPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}
			p.setValues(position.getRow() - 1, position.getColumn() + 1);
			if(getBoard().PositionExists(p) && isThereOponnetPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}
			// SPECIALMOVE EL PASSANT white
			if(position.getRow() == 3) {
				Position left = new Position(position.getRow(), position.getColumn() - 1);
				if(getBoard().PositionExists(left) && isThereOponnetPiece(left) && getBoard().piece(left) == chessMatch.getEnPassantVulnerable()) {
					mat[left.getRow() - 1][left.getColumn()] = true;
				}
				Position right = new Position(position.getRow(), position.getColumn() + 1);
				if(getBoard().PositionExists(right) && isThereOponnetPiece(right) && getBoard().piece(right) == chessMatch.getEnPassantVulnerable()) {
					mat[right.getRow() - 1][right.getColumn()] = true;
				}
			}
			
		}
		else {
			p.setValues(position.getRow() + 1, position.getColumn());
			if(getBoard().PositionExists(p) && !getBoard().thereIsAPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}
			p.setValues(position.getRow() + 2, position.getColumn());
			Position p2 = new Position(position.getRow() + 1, position.getColumn());
			if(getBoard().PositionExists(p) && !getBoard().thereIsAPiece(p) && getBoard().PositionExists(p2) && !getBoard().thereIsAPiece(p2) && getMoveCount() == 0) {
				mat[p.getRow()][p.getColumn()] = true;
			}
			p.setValues(position.getRow() + 1, position.getColumn() - 1);
			if(getBoard().PositionExists(p) && isThereOponnetPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}
			p.setValues(position.getRow() + 1, position.getColumn() + 1);
			if(getBoard().PositionExists(p) && isThereOponnetPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}
			// SPECIALMOVE EL PASSANT black
			if(position.getRow() == 4) {
				Position left = new Position(position.getRow(), position.getColumn() - 1);
				if(getBoard().PositionExists(left) && isThereOponnetPiece(left) && getBoard().piece(left) == chessMatch.getEnPassantVulnerable()) {
					mat[left.getRow() + 1][left.getColumn()] = true;
				}
				Position right = new Position(position.getRow(), position.getColumn() + 1);
				if(getBoard().PositionExists(right) && isThereOponnetPiece(right) && getBoard().piece(right) == chessMatch.getEnPassantVulnerable()) {
					mat[left.getRow() + 1][left.getColumn()] = true;
				}
			}
			
		}
		return mat;
	}
}
	