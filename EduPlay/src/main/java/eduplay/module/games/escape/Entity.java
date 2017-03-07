package eduplay.module.games.escape;

public abstract class Entity {
	protected Position position;
	protected String name;
	
	public Entity(String name, Position position) {
		this.position = position;
		this.setName(name);
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
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

}
