package eduplay.module.games.escape;

public abstract class Environment {
	private int positionRow;
	private int positionColumn;
	
	public Environment(int positionRow, int positionColumn) {
		this.setPositionRow(positionRow);
		this.setPositionColumn(positionColumn);
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
}
