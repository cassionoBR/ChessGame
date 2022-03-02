package Chess;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import BoardGame.Board;
import BoardGame.Piece;
import BoardGame.Position;
import Chess.pieces.Bishop;
import Chess.pieces.King;
import Chess.pieces.Knight;
import Chess.pieces.Pawn;
import Chess.pieces.Queen;
import Chess.pieces.Rook;

public class ChessMatch {
	
	private int turn;
	private Color currentPlayer;
	private Board board;
	private boolean check;
	private boolean checkMate;
	private ChessPiece enPassantVulnerable;
	private ChessPiece promoted;
	private List<Piece> piecesOnBoard = new ArrayList<>();
	private List<Piece> capturedPieces = new ArrayList<>();
	
	public int getTurn() {
		return turn;
	}
	
	public Color getCurrentPlayer() {
		return currentPlayer;
	}
	
	public ChessMatch() {
		board =  new Board(8, 8);
		turn = 1;
		currentPlayer = Color.WHITE;
		InitialSetup();
	}
	
	public boolean getCheck() {
		return check;
	}
	
	public boolean getCheckMate() {
		return checkMate;
	}
	
	public ChessPiece[][] getPieces() {
		ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];
		for(int i = 0; i < board.getRows(); i++) {
			for(int j = 0; j < board.getColumns(); j++) {
				mat[i][j] = (ChessPiece) board.piece(i, j);
			}
		}
		return mat;
	}
	
	public ChessPiece getEnPassantVulnerable() {
		return enPassantVulnerable;
	}
	
	public ChessPiece getPromoted() {
		return promoted;
	}
	
	public boolean[][] possibleMoves(ChessPosition sourcePosition){
		Position position = sourcePosition.toPosition();
		validateSourcePosition(position);
		return board.piece(position).possibleMoves();
	}
	
	public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
		Position source = sourcePosition.toPosition();
		Position target = targetPosition.toPosition();
		validateSourcePosition(source);
		validateTargetPosition(source, target);
		Piece capturedPiece = makeMove(source, target);
		
		if (testCheck(currentPlayer)) {
			undoMove(source, target, capturedPiece);
			throw new ChessException("You can't put yourself in check");
		}
		
		ChessPiece movedPiece = (ChessPiece)board.piece(target);
		//ESPECIALMOVE PROMOTION
		promoted = null;
		if(movedPiece instanceof Pawn) {
			if(movedPiece.getColor() == Color.WHITE && target.getRow() == 0 || movedPiece.getColor() == Color.BLACK && target.getRow() == 7) {
				promoted = (ChessPiece)board.piece(target);
				promoted = replacePromotedPiece("Q");
			}
		}
		
		check = (testCheck(opponent(currentPlayer))) ? true : false;
		
		if(testCheckMate(opponent(currentPlayer))) {
			checkMate = true;
		}
		else {
			nextTurn();
		}
		
		// SPECIALMOVE EN PASSANT
		if(movedPiece instanceof Pawn && (target.getRow() == source.getRow() - 2 || target.getRow() == source.getRow() + 2 )) {
			enPassantVulnerable = movedPiece;
		}
		else {
			enPassantVulnerable = null;
		}
		
			return (ChessPiece) capturedPiece;
	}
	
	public ChessPiece replacePromotedPiece(String type) {
		if(promoted == null) {
			throw new IllegalStateException("There is no piece to be promoted");
		}
		if(!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q")) {
			throw new InvalidParameterException("Invalid type for promotion.");
		}
		Position pos = promoted.getChessPosition().toPosition();
		Piece p = board.RemovePiece(pos);
		piecesOnBoard.remove(p);
		
		ChessPiece newPiece = newPiece(type, promoted.getColor());
		board.PlacePiece(newPiece, pos);
		piecesOnBoard.add(newPiece);
		return newPiece;
	}
	
	private ChessPiece newPiece(String type, Color color) {
		if(type.equals("B")) return new Bishop(board, color);
		if(type.equals("N")) return new Knight(board, color);
		if(type.equals("Q")) return new Queen(board, color);
		return new Rook(board, color);
	}
	
	private Piece makeMove(Position source, Position target) {
		ChessPiece p = (ChessPiece)board.RemovePiece(source);
		p.increaseMoveCount();
		Piece capturedPiece = board.RemovePiece(target);
		board.PlacePiece(p, target);
		if(capturedPiece != null) {
			piecesOnBoard.remove(capturedPiece);
			capturedPieces.add(capturedPiece);
		}
		
		//#SPECIALMOVE CASTLING KINGSIDE ROOK
		if(p instanceof King && target.getColumn() == source.getColumn() + 2) {
			Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
			Position targetT = new Position(source.getRow(), source.getColumn() + 1);
			ChessPiece rook = (ChessPiece)board.RemovePiece(sourceT);
			board.PlacePiece(rook, targetT);
			rook.increaseMoveCount();
		}
		
		//#SPECIALMOVE CASTLING QUEENSIDE ROOK
		if(p instanceof King && target.getColumn() == source.getColumn() - 2) {
			Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
			Position targetT = new Position(source.getRow(), source.getColumn() - 1);
			ChessPiece rook = (ChessPiece)board.RemovePiece(sourceT);
			board.PlacePiece(rook, targetT);
			rook.increaseMoveCount();
		}
		
		//ESPECIALMOVE EL PASSANT
		if(p instanceof Pawn) {
			if(source.getColumn() != target.getColumn() && capturedPiece == null) {
				Position pawnPosition;
				if(p.getColor() == Color.WHITE) {
					pawnPosition = new Position(target.getRow() + 1, target.getColumn());
				}
				else {
					pawnPosition = new Position(target.getRow() - 1, target.getColumn());
				}
				capturedPiece = board.RemovePiece(pawnPosition);
				capturedPieces.add(capturedPiece);
				piecesOnBoard.remove(capturedPiece);
			}
		}
		
		return capturedPiece;
		
	}
	
	private void undoMove(Position source, Position target, Piece capturedPiece) {
		ChessPiece p = (ChessPiece)board.RemovePiece(target);
		p.decreaseMoveCount();
		board.PlacePiece(p, source);
		if(capturedPiece != null) {
			board.PlacePiece(capturedPiece, target);
			capturedPieces.remove(capturedPiece);
			piecesOnBoard.add(capturedPiece);
		} 
		
		//#SPECIALMOVE CASTLING KINGSIDE ROOK
		if(p instanceof King && target.getColumn() == source.getColumn() + 2) {
			Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
			Position targetT = new Position(source.getRow(), source.getColumn() + 1);
			ChessPiece rook = (ChessPiece)board.RemovePiece(targetT);
			board.PlacePiece(rook, sourceT);
			rook.decreaseMoveCount();
		}
		
		//#SPECIALMOVE CASTLING QUEENSIDE ROOK
		if(p instanceof King && target.getColumn() == source.getColumn() - 2) {
			Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
			Position targetT = new Position(source.getRow(), source.getColumn() - 1);
			ChessPiece rook = (ChessPiece)board.RemovePiece(targetT);
			board.PlacePiece(rook, sourceT);
			rook.decreaseMoveCount();
		}
		
		//ESPECIALMOVE EL PASSANT
		if(p instanceof Pawn) {
			if(source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable) {
				ChessPiece pawn = (ChessPiece)board.RemovePiece(target);
				Position pawnPosition;
				if(p.getColor() == Color.WHITE) {
					pawnPosition = new Position(3, target.getColumn());
				}
				else {
					pawnPosition = new Position(4, target.getColumn());
				}
				board.PlacePiece(pawn, pawnPosition);
			}
		}
		
	}
	
	private void validateSourcePosition(Position position) {
		if (!board.thereIsAPiece(position)) {
			throw new ChessException("There is no piece on source position");
		}
		
		if (currentPlayer != ((ChessPiece)board.piece(position)).getColor()) {
			throw new ChessException("The chosen piece is not yours");
		}
		
		if (!board.piece(position).isThereAnyPossibleMove()) {
			throw new ChessException("There is no possible moves for the chosen piece");
		}
	}
	
	private void validateTargetPosition(Position source, Position target) {
		if (!board.piece(source).possibleMove(target)) {
			throw new ChessException("The chosen piece can't move to target position");
		}
	}
	
	public void nextTurn() {
		turn++;
		currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
	}
	
	private Color opponent(Color color) {
		return ((color == Color.WHITE) ? Color.BLACK : Color.WHITE);
	}
	
	private ChessPiece king(Color color) {
		List<Piece> list = piecesOnBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
		for(Piece p : list) {
			if(p instanceof King) {
				return (ChessPiece) p;
			}
		}
		throw new IllegalStateException("There is no " + color + "king on the board");
	}
	
	private boolean testCheck(Color color) {
		Position kingPosition = king(color).getChessPosition().toPosition();
		List<Piece> opponentPieces = piecesOnBoard.stream().filter(x -> ((ChessPiece)x).getColor() == opponent(color)).collect(Collectors.toList());
		for (Piece p : opponentPieces) {
			boolean[][] mat = p.possibleMoves();
			if (mat[kingPosition.getRow()][kingPosition.getColumn()]) {
				return true;
			}
		}
		return false;
	}
	
	private boolean testCheckMate(Color color) {
		if(!testCheck(color)) {
			return false;
		}
		
		List<Piece> list = piecesOnBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
		for (Piece p : list) {
			boolean[][] mat = p.possibleMoves(); 
			for(int i = 0; i < board.getRows(); i++) {
				for(int h = 0; h < board.getColumns(); h++) {
					if(mat[i][h]) {
						Position source = ((ChessPiece)p).getChessPosition().toPosition();
						Position target = new Position(i, h);
						Piece capturedPiece = makeMove(source, target);
						boolean testCheck = testCheck(color);
						undoMove(source, target, capturedPiece);
						if(!testCheck) {
							return false;
						}
						
					}
				}
			}
		}
		return true;
	}
	
	
	public void PlaceNewPiece(char column, int row, ChessPiece piece) {
		board.PlacePiece(piece, new ChessPosition(column, row).toPosition());
		piecesOnBoard.add(piece);
	}
	
	public void InitialSetup() {
		PlaceNewPiece('a', 1, new Rook(board, Color.WHITE));
		PlaceNewPiece('b', 1, new Knight(board, Color.WHITE));
		PlaceNewPiece('c', 1, new Bishop(board, Color.WHITE));
		PlaceNewPiece('d', 1, new Queen(board, Color.WHITE));
		PlaceNewPiece('e', 1, new King(board, Color.WHITE, this));
		PlaceNewPiece('f', 1, new Bishop(board, Color.WHITE));
		PlaceNewPiece('g', 1, new Knight(board, Color.WHITE));
        PlaceNewPiece('h', 1, new Rook(board, Color.WHITE));
        PlaceNewPiece('a', 2, new Pawn(board, Color.WHITE, this));
        PlaceNewPiece('b', 2, new Pawn(board, Color.WHITE, this));
        PlaceNewPiece('c', 2, new Pawn(board, Color.WHITE, this));
        PlaceNewPiece('d', 2, new Pawn(board, Color.WHITE, this));
        PlaceNewPiece('e', 2, new Pawn(board, Color.WHITE, this));
        PlaceNewPiece('f', 2, new Pawn(board, Color.WHITE, this));
        PlaceNewPiece('g', 2, new Pawn(board, Color.WHITE, this));
        PlaceNewPiece('h', 2, new Pawn(board, Color.WHITE, this));
        
        PlaceNewPiece('a', 8, new Rook(board, Color.BLACK));
		PlaceNewPiece('b', 8, new Knight(board, Color.BLACK));
		PlaceNewPiece('c', 8, new Bishop(board, Color.BLACK));
		PlaceNewPiece('d', 8, new Queen(board, Color.BLACK));
        PlaceNewPiece('e', 8, new King(board, Color.BLACK, this));
		PlaceNewPiece('f', 8, new Bishop(board, Color.BLACK));
		PlaceNewPiece('g', 8, new Knight(board, Color.BLACK));
        PlaceNewPiece('h', 8, new Rook(board, Color.BLACK));
        PlaceNewPiece('a', 7, new Pawn(board, Color.BLACK, this));
        PlaceNewPiece('b', 7, new Pawn(board, Color.BLACK, this));
        PlaceNewPiece('c', 7, new Pawn(board, Color.BLACK, this));
        PlaceNewPiece('d', 7, new Pawn(board, Color.BLACK, this));
        PlaceNewPiece('e', 7, new Pawn(board, Color.BLACK, this));
        PlaceNewPiece('f', 7, new Pawn(board, Color.BLACK, this));
        PlaceNewPiece('g', 7, new Pawn(board, Color.BLACK, this));
        PlaceNewPiece('h', 7, new Pawn(board, Color.BLACK, this));
     
	}

}
