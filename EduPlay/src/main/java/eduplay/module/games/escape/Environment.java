package eduplay.module.games.escape;

public abstract class Environment {
	private Position position;
	
	public Environment(Position position) {
		this.position = position;
	}

	public int getPositionRow() {
		return position.row;
	}

	public void setPositionRow(int positionRow) {
		this.position.row = positionRow;
	}

	public int getPositionColumn() {
		return position.column;
	}

	public void setPositionColumn(int positionColumn) {
		this.position.column = positionColumn;
	}
}
