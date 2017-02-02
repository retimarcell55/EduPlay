package module.games.escape;

import java.util.ArrayList;
import java.util.List;

public abstract class Entity {
	protected int posrow;
	protected int poscolumn;
	protected String name;
	
	public Entity(String name,int posrow, int poscolumn) {
		this.setPosrow(posrow);
		this.setPoscolumn(poscolumn);
		this.setName(name);
	}

	public int getPosrow() {
		return posrow;
	}

	public void setPosrow(int posrow) {
		this.posrow = posrow;
	}

	public int getPoscolumn() {
		return poscolumn;
	}

	public void setPoscolumn(int poscolumn) {
		this.poscolumn = poscolumn;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	protected static List<Position> getNeighbour(Position p, int row, int col) {
		List<Position> neighours = new ArrayList<Position>();

		Position posLeft = p.getLeft();
		if (posLeft.row >= 0 && posLeft.row < row && posLeft.col >= 0 && posLeft.col < col)
			neighours.add(posLeft);
		Position posRight = p.getRight();
		if (posRight.row >= 0 && posRight.row < row && posRight.col >= 0 && posRight.col < col)
			neighours.add(posRight);
		Position posUp = p.getUp();
		if (posUp.row >= 0 && posUp.row < row && posUp.col >= 0 && posUp.col < col)
			neighours.add(posUp);
		Position posDown = p.getBottom();
		if (posDown.row >= 0 && posDown.row < row && posDown.col >= 0 && posDown.col < col)
			neighours.add(posDown);
		Position posUpLeft = p.getUpLeft();
		if (posUpLeft.row >= 0 && posUpLeft.row < row && posUpLeft.col >= 0 && posUpLeft.col < col)
			neighours.add(posUpLeft);
		Position posUpRight = p.getUpRight();
		if (posUpRight.row >= 0 && posUpRight.row < row && posUpRight.col >= 0 && posUpRight.col < col)
			neighours.add(posUpRight);
		Position posBottomLeft = p.getBottomLeft();
		if (posBottomLeft.row >= 0 && posBottomLeft.row < row && posBottomLeft.col >= 0 && posBottomLeft.col < col)
			neighours.add(posBottomLeft);
		Position posBottomRight = p.getBottomRight();
		if (posBottomRight.row >= 0 && posBottomRight.row < row && posBottomRight.col >= 0 && posBottomRight.col < col)
			neighours.add(posBottomRight);

		return neighours;
	}
	
	public class Position {
		public int row;
		public int col;

		public Position(int row, int col) {
			this.row = row;
			this.col = col;
		}

		public Position getLeft() {
			return new Position(row, col - 1);
		}

		public Position getRight() {
			return new Position(row, col + 1);
		}

		public Position getBottom() {
			return new Position(row + 1, col);
		}

		public Position getUp() {
			return new Position(row - 1, col);
		}

		public Position getUpLeft() {
			return new Position(row - 1, col - 1);
		}

		public Position getUpRight() {
			return new Position(row - 1, col + 1);
		}

		public Position getBottomLeft() {
			return new Position(row + 1, col - 1);
		}

		public Position getBottomRight() {
			return new Position(row + 1, col + 1);
		}
	}
	
}
