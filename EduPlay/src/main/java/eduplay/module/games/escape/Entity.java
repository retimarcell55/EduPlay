package eduplay.module.games.escape;

import java.util.ArrayList;
import java.util.List;

public abstract class Entity {
	protected int positionRow;
	protected int positionColumn;
	protected String name;
	
	public Entity(String name, int positionRow, int positionColumn) {
		this.setPositionRow(positionRow);
		this.setPositionColumn(positionColumn);
		this.setName(name);
	}

	public int getPositionRow() {
		return positionRow;
	}

	public void setPositionRow(int positionRow) {
		this.positionRow = positionRow;
	}

	public int getPositionColumn() {
		return positionColumn;
	}

	public void setPositionColumn(int positionColumn) {
		this.positionColumn = positionColumn;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	protected static List<Position> getNeighbour(Position position, int row, int col) {
		List<Position> neighbours = new ArrayList<Position>();

		Position posLeft = position.getLeft();
		if (posLeft.row >= 0 && posLeft.row < row && posLeft.column >= 0 && posLeft.column < col)
			neighbours.add(posLeft);
		Position posRight = position.getRight();
		if (posRight.row >= 0 && posRight.row < row && posRight.column >= 0 && posRight.column < col)
			neighbours.add(posRight);
		Position posUp = position.getUp();
		if (posUp.row >= 0 && posUp.row < row && posUp.column >= 0 && posUp.column < col)
			neighbours.add(posUp);
		Position posDown = position.getBottom();
		if (posDown.row >= 0 && posDown.row < row && posDown.column >= 0 && posDown.column < col)
			neighbours.add(posDown);
		Position posUpLeft = position.getUpLeft();
		if (posUpLeft.row >= 0 && posUpLeft.row < row && posUpLeft.column >= 0 && posUpLeft.column < col)
			neighbours.add(posUpLeft);
		Position posUpRight = position.getUpRight();
		if (posUpRight.row >= 0 && posUpRight.row < row && posUpRight.column >= 0 && posUpRight.column < col)
			neighbours.add(posUpRight);
		Position posBottomLeft = position.getBottomLeft();
		if (posBottomLeft.row >= 0 && posBottomLeft.row < row && posBottomLeft.column >= 0 && posBottomLeft.column < col)
			neighbours.add(posBottomLeft);
		Position posBottomRight = position.getBottomRight();
		if (posBottomRight.row >= 0 && posBottomRight.row < row && posBottomRight.column >= 0 && posBottomRight.column < col)
			neighbours.add(posBottomRight);

		return neighbours;
	}
	
	public class Position {
		public int row;
		public int column;

		public Position(int row, int column) {
			this.row = row;
			this.column = column;
		}

		public Position getLeft() {
			return new Position(row, column - 1);
		}

		public Position getRight() {
			return new Position(row, column + 1);
		}

		public Position getBottom() {
			return new Position(row + 1, column);
		}

		public Position getUp() {
			return new Position(row - 1, column);
		}

		public Position getUpLeft() {
			return new Position(row - 1, column - 1);
		}

		public Position getUpRight() {
			return new Position(row - 1, column + 1);
		}

		public Position getBottomLeft() {
			return new Position(row + 1, column - 1);
		}

		public Position getBottomRight() {
			return new Position(row + 1, column + 1);
		}
	}
	
}
